package Services;

/**
 * Class representing a Monitoring Service ({@code s ∈ S_moni}) of the Digital Twin.
 * <p>
 * A Monitoring Service observes the state of the ToI in real time: it compares the latest value of a
 * monitored entity against thresholds and raises an alert when it is out of bounds.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_Service_Monitoring<T> extends DT_Service<T> {

    /** The name of the entity being monitored (e.g., "Temperature Sensor", "Power Grid"). */
    protected String monitoredEntity;

    /** The upper threshold limit for the monitored parameter. */
    protected double upperThreshold;

    /** The lower threshold limit for the monitored parameter. */
    protected double lowerThreshold;

    /** The current value of the monitored parameter ({@code NaN} until a value is received). */
    protected double currentValue = Double.NaN;

    /**
     * Constructor to initialize the Monitoring Service with necessary attributes.
     *
     * @param serviceName        the name of the service.
     * @param serviceDescription a brief description of the service.
     * @param monitoredEntity    the entity being monitored.
     * @param lowerThreshold     the lower limit of the monitored parameter.
     * @param upperThreshold     the upper limit of the monitored parameter.
     */
    public DT_Service_Monitoring(String serviceName, String serviceDescription,
                                 String monitoredEntity, double lowerThreshold, double upperThreshold) {
        super(serviceName, serviceDescription);
        this.monitoredEntity = monitoredEntity;
        this.lowerThreshold = lowerThreshold;
        this.upperThreshold = upperThreshold;
    }

    /**
     * Called when new data arrives: a numeric datum becomes the current value of the monitored parameter.
     *
     * @param data the new datum.
     */
    @Override
    public void update(T data) {
        super.update(data);
        double v = numericValue(data);
        if (!Double.isNaN(v)) {
            currentValue = v;
        }
    }

    /**
     * Executes the monitoring logic: compares the current value with the thresholds.
     *
     * @return the monitoring status (an alert if the value is out of bounds).
     */
    @Override
    public Object executeService() {
        if (Double.isNaN(currentValue)) {
            return "STATUS: no value received yet for " + monitoredEntity + ".";
        }
        if (currentValue > upperThreshold) {
            return "ALERT: " + monitoredEntity + " = " + currentValue + " exceeds the upper threshold " + upperThreshold + "!";
        }
        if (currentValue < lowerThreshold) {
            return "ALERT: " + monitoredEntity + " = " + currentValue + " falls below the lower threshold " + lowerThreshold + "!";
        }
        return "STATUS: " + monitoredEntity + " = " + currentValue + " is within safe limits.";
    }

    /** @return the name of the monitored entity. */
    public String getMonitoredEntity() {
        return monitoredEntity;
    }

    /** @return the upper threshold. */
    public double getUpperThreshold() {
        return upperThreshold;
    }

    /** @param upperThreshold the new upper threshold. */
    public void setUpperThreshold(double upperThreshold) {
        this.upperThreshold = upperThreshold;
    }

    /** @return the lower threshold. */
    public double getLowerThreshold() {
        return lowerThreshold;
    }

    /** @param lowerThreshold the new lower threshold. */
    public void setLowerThreshold(double lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
    }

    /** @return the current value of the monitored parameter. */
    public double getCurrentValue() {
        return currentValue;
    }
}
