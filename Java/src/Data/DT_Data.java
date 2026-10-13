package Data;

import Mu.ObserverData;
import Mu.ObserverDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.DoubleSupplier;

/**
 * Class representing a source of data collected from the ToI, i.e., a part of {@code D} in
 * {@code DT = ⟨D, M, S, μ⟩}.
 * <p>
 * The collected data is either historical or real-time, {@code D = D_hist ∪ D_rt}. The classification
 * depends only on the measurement time: a datum {@code d = ω(t)} is real-time iff {@code t ≥ t_now − δ},
 * and historical otherwise, where {@code t} is the measurement time, {@code t_now} the current time and
 * {@code δ} the data scope during which data is considered real-time.
 * </p>
 *
 * <p>Key features of the class include:
 * <ul>
 *   <li>Every datum is recorded with its measurement time in a segment {@code ω} ({@link DT_DataSegment}).</li>
 *   <li>{@link #getRealTimeData} and {@link #getHistoricalData} give {@code D_rt} and {@code D_hist} of the source.</li>
 *   <li>It is the subject of the observer pattern ({@link ObserverDataManager}): each new datum is pushed to
 *       the registered observers, in particular to the data assimilation function {@code μ}.</li>
 * </ul>
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_Data<T> implements ObserverDataManager<T> {

    /** The observers interested in this data source. */
    private final List<ObserverData<T>> observers = new ArrayList<>();

    /** The source of the data (e.g., "Sensor A", "API"). */
    protected String source;

    /** The type of the data (e.g., "Temperature", "Pressure"). */
    protected String dataType;

    /** The latest datum. */
    private T currentData;

    /** The measurement time {@code t ∈ T_U} of the latest datum, i.e., {@code currentData = ω(currentTime)}. */
    private double currentTime;

    /** All data collected from this source, as a segment {@code ω : T_U ⇀ D}. */
    private final DT_DataSegment<T> collectedData = new DT_DataSegment<>();

    /** Gives the current time {@code t_now} (default: system time in seconds). */
    private DoubleSupplier clock = () -> System.currentTimeMillis() / 1000.0;

    /**
     * Constructs a data source.
     *
     * @param source   the origin of the data (e.g., sensor name or system name).
     * @param dataType the type or category of the data (e.g., temperature).
     */
    public DT_Data(String source, String dataType) {
        this.source = source;
        this.dataType = dataType;
    }

    // ---------------------------------------------------------------- observer pattern (subject)

    @Override
    public void registerObserver(ObserverData<T> observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(ObserverData<T> observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        // Safe copy to avoid issues if an observer removes itself during update
        for (ObserverData<T> observer : new ArrayList<>(observers)) {
            observer.update(currentData);
        }
    }

    // ---------------------------------------------------------------- data collection

    /**
     * Call this method whenever new data arrives; the datum is time-stamped with the current time {@code t_now}.
     *
     * @param newData the new datum.
     */
    public void updateData(T newData) {
        updateData(clock.getAsDouble(), newData);
    }

    /**
     * Call this method whenever new data measured at time {@code t} arrives. The datum is recorded
     * ({@code ω(t) = newData}) and the observers are notified.
     *
     * @param t       the measurement time {@code t ∈ T_U}.
     * @param newData the new datum.
     * @throws IllegalArgumentException if {@code t > t_now}: no datum may lie in the future.
     */
    public void updateData(double t, T newData) {
        if (t > clock.getAsDouble()) {
            throw new IllegalArgumentException("No datum may lie in the future: t = " + t
                    + " > t_now = " + clock.getAsDouble());
        }
        this.currentData = newData;
        this.currentTime = t;
        collectedData.put(t, newData);
        notifyObservers();
    }

    /** @return the latest datum. */
    public T getCurrentData() {
        return currentData;
    }

    /** @return the measurement time of the latest datum. */
    public double getCurrentTime() {
        return currentTime;
    }

    /** @return all data collected from this source, as a segment {@code ω}. */
    public DT_DataSegment<T> getCollectedData() {
        return collectedData;
    }

    /**
     * @param delta the data scope {@code δ}.
     * @return {@code D_rt}: the collected data measured at {@code t ≥ t_now − δ}.
     */
    public DT_DataSegment<T> getRealTimeData(double delta) {
        return collectedData.realTimePart(clock.getAsDouble(), delta);
    }

    /**
     * @param delta the data scope {@code δ}.
     * @return {@code D_hist}: the collected data measured at {@code t < t_now − δ}.
     */
    public DT_DataSegment<T> getHistoricalData(double delta) {
        return collectedData.historicalPart(clock.getAsDouble(), delta);
    }

    /**
     * Replaces the clock giving {@code t_now} (set by the DT so that all its parts share the same time).
     *
     * @param clock the new clock.
     */
    public void setClock(DoubleSupplier clock) {
        this.clock = clock;
    }

    // ---------------------------------------------------------------- attributes

    /**
     * Validates the common attributes of the data source.
     *
     * @return true if the source and the data type are defined; false otherwise.
     */
    public boolean validateData() {
        return source != null && !source.isEmpty() && dataType != null && !dataType.isEmpty();
    }

    /** @return the source of the data. */
    public String getSource() {
        return source;
    }

    /** @param source the new source of the data. */
    public void setSource(String source) {
        this.source = source;
    }

    /** @return the type of the data. */
    public String getDataType() {
        return dataType;
    }

    /** @param dataType the new type of the data. */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Converts the data source into a human-readable string format.
     *
     * @return a string with the source, the data type and the number of collected data.
     */
    @Override
    public String toString() {
        return "DT_Data{source='" + source + "', dataType='" + dataType + "', collected=" + collectedData.size() + "}";
    }

    /**
     * Two data sources are considered equal if they have the same source and data type.
     *
     * @param obj the object to compare with.
     * @return {@code true} if the objects are equal; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DT_Data<?> other)) return false;
        return Objects.equals(source, other.source) && Objects.equals(dataType, other.dataType);
    }

    /** @return the hash code, based on the source and the data type. */
    @Override
    public int hashCode() {
        return Objects.hash(source, dataType);
    }
}
