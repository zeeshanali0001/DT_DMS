package Mu;

import Data.DT_Data;
import Data.DT_DataSegment;
import Models.DT_Model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * Class representing the model inference or data assimilation function {@code μ : Ω_D × M → M} of
 * {@code DT = ⟨D, M, S, μ⟩}.
 * <p>
 * Given a segment {@code ω ∈ Ω_D} and a model {@code m ∈ M}, {@code μ(ω, m) = m'} is the model {@code m}
 * updated with {@code ω}. The model is updated in place, so {@code m'} is the same object as {@code m} and
 * keeps the type of {@code m} (e.g., {@code m ∈ M_pred ⇒ m' ∈ M_pred}).
 * </p>
 *
 * <p>Key features of the class include:
 * <ul>
 *   <li><b>Data observer pattern</b>: it observes the data sources ({@link ObserverDataManager}) through one
 *       {@link ObserverData} per source; each new datum of the ToI is turned into a segment {@code ω}.</li>
 *   <li><b>Synchronous assimilation</b> of real-time segments ({@code Ω_D^rt}): applied at once.</li>
 *   <li><b>Asynchronous assimilation</b> of historical segments ({@code Ω_D^hist}): queued until {@link #flush()}.</li>
 *   <li><b>Mixed segments</b> ({@code Ω_D^mix}) are handled according to the {@link MixedPolicy}: split into
 *       {@code ω_hist ⊕ ω_rt} and assimilated part by part (default), or assimilated as a whole like a
 *       real-time or like a historical segment.</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_Mu<T> {

    /** How a mixed segment {@code ω ∈ Ω_D^mix(t_now)} is assimilated, depending on the DT requirements. */
    public enum MixedPolicy {
        /** Part by part: {@code ω_rt} synchronously and {@code ω_hist} asynchronously. */
        SPLIT,
        /** As a whole, synchronously, like a real-time segment. */
        AS_REAL_TIME,
        /** As a whole, asynchronously, like a historical segment. */
        AS_HISTORICAL
    }

    /**
     * Observer registered on one data source: forwards every new datum of that source to {@code μ}.
     */
    private class Assimilator implements ObserverData<T> {

        /** The observed data source. */
        private final DT_Data<T> source;

        /** @param source the observed data source. */
        Assimilator(DT_Data<T> source) {
            this.source = source;
        }

        /** Called when new data arrives from {@link #source}: the datum {@code d = ω(t)} is assimilated. */
        @Override
        public void update(T updatedData) {
            assimilate(new DT_DataSegment<>(source.getCurrentTime(), updatedData));
        }
    }

    /** The freshness threshold {@code δ ∈ ℝ+}: the data scope during which data is considered real-time. */
    private final double delta;

    /** Gives the current time {@code t_now ∈ T_U}. */
    private final DoubleSupplier clock;

    /** The models {@code M} updated by {@code μ}. */
    private final List<DT_Model<T>> models = new ArrayList<>();

    /** The observed data sources. */
    private final List<DT_Data<T>> sources = new ArrayList<>();

    /** Segments waiting for asynchronous assimilation. */
    private final List<DT_DataSegment<T>> pending = new ArrayList<>();

    /** How mixed segments are assimilated. */
    private MixedPolicy mixedPolicy = MixedPolicy.SPLIT;

    /**
     * Constructs the data assimilation function.
     *
     * @param delta the freshness threshold {@code δ}.
     * @param clock gives the current time {@code t_now}.
     */
    public DT_Mu(double delta, DoubleSupplier clock) {
        if (delta < 0) {
            throw new IllegalArgumentException("δ must be in ℝ+.");
        }
        this.delta = delta;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- what μ observes and updates

    /**
     * Adds a model to the set of models updated by {@code μ}.
     *
     * @param m the model.
     */
    public void addModel(DT_Model<T> m) {
        if (!models.contains(m)) {
            models.add(m);
        }
    }

    /**
     * Starts observing a data source: every new datum of {@code source} will be assimilated.
     *
     * @param source the data source.
     */
    public void observe(DT_Data<T> source) {
        if (!sources.contains(source)) {
            sources.add(source);
            source.registerObserver(new Assimilator(source));
        }
    }

    // ---------------------------------------------------------------- μ

    /**
     * The data assimilation function {@code μ(ω, m) = m'}.
     *
     * @param omega the data segment {@code ω}.
     * @param m     the model {@code m}.
     * @return the updated model {@code m'} (same object and type as {@code m}).
     */
    public DT_Model<T> mu(DT_DataSegment<T> omega, DT_Model<T> m) {
        m.assimilate(omega);
        return m;
    }

    /**
     * Assimilates a segment according to its kind at {@code t_now}: real-time → synchronously,
     * historical → asynchronously, mixed → according to the {@link MixedPolicy}.
     *
     * @param omega the data segment {@code ω}.
     * @throws IllegalArgumentException if {@code ω} is empty or has a datum in the future.
     */
    public void assimilate(DT_DataSegment<T> omega) {
        double tNow = clock.getAsDouble();
        switch (omega.kind(tNow, delta)) {
            case REAL_TIME -> assimilateSynchronously(omega);
            case HISTORICAL -> assimilateAsynchronously(omega);
            case MIXED -> {
                switch (mixedPolicy) {
                    case SPLIT -> {
                        // ω = ω_hist ⊕ ω_rt, split at t_now − δ
                        assimilateAsynchronously(omega.historicalPart(tNow, delta));
                        assimilateSynchronously(omega.realTimePart(tNow, delta));
                    }
                    case AS_REAL_TIME -> assimilateSynchronously(omega);
                    case AS_HISTORICAL -> assimilateAsynchronously(omega);
                }
            }
        }
    }

    /**
     * Synchronous assimilation: applies {@code μ(ω, m)} at once to every model {@code m ∈ M}.
     *
     * @param omega the data segment {@code ω}.
     */
    public void assimilateSynchronously(DT_DataSegment<T> omega) {
        for (DT_Model<T> m : models) {
            mu(omega, m);
        }
    }

    /**
     * Asynchronous assimilation: queues the segment; {@code μ} is applied when {@link #flush()} is called.
     *
     * @param omega the data segment {@code ω}.
     */
    public void assimilateAsynchronously(DT_DataSegment<T> omega) {
        pending.add(omega);
    }

    /**
     * Applies {@code μ} to all queued segments, in the order they were queued.
     */
    public void flush() {
        List<DT_DataSegment<T>> toAssimilate = new ArrayList<>(pending);
        pending.clear();
        for (DT_DataSegment<T> omega : toAssimilate) {
            assimilateSynchronously(omega);
        }
    }

    // ---------------------------------------------------------------- accessors

    /** @return the freshness threshold {@code δ}. */
    public double getDelta() {
        return delta;
    }

    /** @return the number of segments waiting for {@link #flush()}. */
    public int getPendingCount() {
        return pending.size();
    }

    /** @return how mixed segments are assimilated. */
    public MixedPolicy getMixedPolicy() {
        return mixedPolicy;
    }

    /** @param mixedPolicy how mixed segments are to be assimilated. */
    public void setMixedPolicy(MixedPolicy mixedPolicy) {
        this.mixedPolicy = mixedPolicy;
    }

    /** @return the models updated by {@code μ}. */
    public List<DT_Model<T>> getModels() {
        return models;
    }

    /** @return the observed data sources. */
    public List<DT_Data<T>> getSources() {
        return sources;
    }
}
