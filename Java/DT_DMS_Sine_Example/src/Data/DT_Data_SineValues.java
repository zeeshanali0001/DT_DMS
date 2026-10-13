package Data;

/**
 * Data source of the sine example: it plays the role of the ToI (the "physical twin") and produces the
 * sine wave values measured over time.
 * <p>
 * Each call to {@link #measure} produces the datum {@code d = ω(t) = sin(t)} measured at time {@code t} and
 * records it; the observers (the data assimilation function {@code μ}) are notified at once.
 * An anomaly can be injected to test the diagnosis service.
 * </p>
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 2.0
 */
public class DT_Data_SineValues extends DT_Data<Double> {

    /** The time at which an anomaly is injected ({@code NaN} = no anomaly). */
    private double anomalyTime = Double.NaN;

    /** The offset added to the sine value at {@link #anomalyTime}. */
    private double anomalyOffset = 0.0;

    /**
     * Constructor to initialize the sine data source.
     *
     * @param name descriptive name of this data source (e.g., "Sine values").
     */
    public DT_Data_SineValues(String name) {
        super(name, "Sine");
    }

    /**
     * Measures the ToI at time {@code t}: produces {@code sin(t)} (plus the anomaly offset, if any at that
     * time), records it and notifies the observers.
     *
     * @param t the measurement time.
     * @return the measured value.
     */
    public double measure(double t) {
        double value = Math.sin(t);
        if (Math.abs(t - anomalyTime) < 1e-9) {
            value += anomalyOffset;
        }
        updateData(t, value);
        return value;
    }

    /**
     * Injects an anomaly: the value measured at {@code time} will be shifted by {@code offset}.
     *
     * @param time   the measurement time of the anomaly.
     * @param offset the offset added to the sine value.
     */
    public void injectAnomaly(double time, double offset) {
        this.anomalyTime = time;
        this.anomalyOffset = offset;
    }
}
