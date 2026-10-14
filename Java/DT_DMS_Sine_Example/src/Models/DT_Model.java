package Models;

import Automaton.DT_Automaton;
import Data.DT_DataSegment;
import Mu.ObserverData;
import Mu.ObserverModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class representing a generic DT model {@code m ∈ M} of {@code DT = ⟨D, M, S, μ⟩}.
 * <p>
 * {@code M} is the set of DT models that represent the ToI virtually:
 * {@code M = M_desc ∪ M_pred ∪ M_presc}, the descriptive ({@link DT_Model_Descriptive}), predictive
 * ({@link DT_Model_Predictive}) and prescriptive ({@link DT_Model_Prescriptive}) models.
 * </p>
 * <p>
 * Every model belongs to the class of automata {@code 𝒜} ({@code M ⊆ 𝒜}); hence this class extends
 * {@link DT_Automaton} and inherits the 7-tuple {@code ℳ = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩}. A model is updated by
 * the data assimilation function {@code μ} through {@link #assimilate}.
 * </p>
 * <p>
 * A model is also the subject of the model observer pattern: the services coupled to it
 * ({@link #addServiceToBeUpdated}) are notified after each assimilation ({@link #notifyServices}).
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public abstract class DT_Model<T> extends DT_Automaton implements ObserverData<T> {

    /** The name of the model, which uniquely identifies it. */
    protected String modelName;

    /** The services to be notified when the model is updated (model observer pattern). */
    private final List<ObserverModel<T>> servicesToBeUpdated = new ArrayList<>();

    /**
     * Constructor to initialize the model's name.
     *
     * @param modelName the name of the model.
     */
    public DT_Model(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Assimilates a data segment into this model: the model side of {@code μ(ω, m) = m'}.
     * <p>
     * The model is updated in place, so {@code m'} keeps the type of {@code m}. By default each datum of
     * {@code ω} is passed to {@link #update} in time order, then the coupled services are notified
     * ({@link #notifyServices}); subclasses can override it (e.g., to re-train a predictive model on a whole
     * historical segment) and should then call {@link #notifyServices} too.
     * </p>
     *
     * @param omega the data segment {@code ω}.
     */
    public void assimilate(DT_DataSegment<T> omega) {
        for (T d : omega.getImage()) {
            update(d);
        }
        notifyServices();
    }

    /**
     * Returns the value of the model at time {@code t}, {@code m(t)}, to be compared with the datum
     * {@code ω(t)} measured at the same time ({@code DT_DMS.checkModelsVersusData}).
     * <p>
     * By default a model gives no value in time and this method returns {@code NaN}; a model that describes
     * or predicts the measured signal (e.g., an interpolation) overrides it.
     * </p>
     *
     * @param t the time.
     * @return {@code m(t)}, or {@code NaN} if the model gives no value at {@code t}.
     */
    public double evaluate(double t) {
        return Double.NaN;
    }

    // ---------------------------------------------------------------- model observer pattern (subject)

    /**
     * Adds a service to be notified each time this model is updated.
     *
     * @param service the service (or any model observer).
     */
    public void addServiceToBeUpdated(ObserverModel<T> service) {
        if (!servicesToBeUpdated.contains(service)) {
            servicesToBeUpdated.add(service);
        }
    }

    /**
     * Stops notifying a service when this model is updated.
     *
     * @param service the service to remove.
     */
    public void removeServiceToBeUpdated(ObserverModel<T> service) {
        servicesToBeUpdated.remove(service);
    }

    /** Notifies the services coupled to this model that it has been updated. */
    public void notifyServices() {
        // Safe copy to avoid issues if a service removes itself during the update
        for (ObserverModel<T> service : new ArrayList<>(servicesToBeUpdated)) {
            service.updateWithModel(this);
        }
    }

    /** @return the services notified when this model is updated (read-only). */
    public List<ObserverModel<T>> getServicesToBeUpdated() {
        return Collections.unmodifiableList(servicesToBeUpdated);
    }

    /**
     * Executes the core logic of the model. Each subclass provides its specific implementation.
     */
    public abstract void executeModel();

    /** @return the model name. */
    public String getModelName() {
        return modelName;
    }

    /** @param modelName the new model name. */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Converts a datum to a number, when it is one.
     *
     * @param d the datum.
     * @return its numeric value, or {@code NaN} if the datum is not a number.
     */
    protected static double numericValue(Object d) {
        return d instanceof Number n ? n.doubleValue() : Double.NaN;
    }

    /**
     * Provides a string representation of the model, useful for debugging or logging.
     *
     * @return a string describing the model's name.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [modelName=" + modelName + "]";
    }

    /**
     * Two models are considered equal if they have the same class and the same name.
     *
     * @param obj the object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return Objects.equals(modelName, ((DT_Model<?>) obj).modelName);
    }

    /** @return the hash code of the model, based on its name. */
    @Override
    public int hashCode() {
        return Objects.hashCode(modelName);
    }
}
