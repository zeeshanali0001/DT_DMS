package Services;

/**
 * Class representing a Monitoring Service in the Digital Twin framework.
 * A Monitoring Service continuously observes specific entities, compares data
 * against thresholds, and raises alerts when necessary. This class extends
 * the base Service class and adds monitoring-specific attributes and behaviors.
 */
public class DT_Services_Monitoring<T> extends DT_Services<T> {


    @Override    public void update(T data) {
        System.out.println("   → MONITORING SERVICE: Received new data for monitoring: " + data);
        // Here you can add logic to process the new data and update the monitoring status accordingly.     
    }

    /**
     * The name of the entity being monitored (e.g., "Temperature Sensor", "Power Grid").
     */
    protected String monitoredEntity;

    /**
     * The upper threshold limit for the monitored parameter.
     */
    protected double upperThreshold;

    /**
     * The lower threshold limit for the monitored parameter.
     */
    protected double lowerThreshold;

    /**
     * The current value of the monitored parameter.
     */
    protected double currentValue;

    /**
     * Constructor to initialize the Monitoring Service with necessary attributes.
     *
     * @param serviceName        the name of the service
     * @param serviceType        the type of the service (e.g., "Monitoring")
     * @param serviceDescription a brief description of the service
     * @param monitoredEntity    the entity being monitored
     * @param upperThreshold     the upper limit of the monitored parameter
     * @param lowerThreshold     the lower limit of the monitored parameter
     */
    public DT_Services_Monitoring(String serviceName, String serviceType, String serviceDescription,
                             String monitoredEntity, double upperThreshold, double lowerThreshold) {
        super(serviceName, serviceType, serviceDescription);
        this.monitoredEntity = monitoredEntity;
        this.upperThreshold = upperThreshold;
        this.lowerThreshold = lowerThreshold;
        this.currentValue = 0.0; // Default initial value
    }

    /**
     * Gets the name of the monitored entity.
     *
     * @return the name of the monitored entity
     */
    public String getMonitoredEntity() {
        return monitoredEntity;
    }

    /**
     * Sets the name of the monitored entity.
     *
     * @param monitoredEntity the new name of the monitored entity
     */
    public void setMonitoredEntity(String monitoredEntity) {
        this.monitoredEntity = monitoredEntity;
    }

    /**
     * Gets the upper threshold limit.
     *
     * @return the upper threshold
     */
    public double getUpperThreshold() {
        return upperThreshold;
    }

    /**
     * Sets the upper threshold limit.
     *
     * @param upperThreshold the new upper threshold
     */
    public void setUpperThreshold(double upperThreshold) {
        this.upperThreshold = upperThreshold;
    }

    /**
     * Gets the lower threshold limit.
     *
     * @return the lower threshold
     */
    public double getLowerThreshold() {
        return lowerThreshold;
    }

    /**
     * Sets the lower threshold limit.
     *
     * @param lowerThreshold the new lower threshold
     */
    public void setLowerThreshold(double lowerThreshold) {
        this.lowerThreshold = lowerThreshold;
    }

    /**
     * Gets the current value of the monitored parameter.
     *
     * @return the current value
     */
    public double getCurrentValue() {
        return currentValue;
    }

    /**
     * Sets the current value of the monitored parameter.
     *
     * @param currentValue the new current value
     */
    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    /**
     * Executes the monitoring logic.
     * Compares the current value of the monitored entity with the thresholds and
     * raises an alert if the value is out of bounds.
     */
    @Override
    public void executeService() {
        if (currentValue > upperThreshold) {
            System.out.println("ALERT: " + monitoredEntity + " exceeds the upper threshold!");
        } else if (currentValue < lowerThreshold) {
            System.out.println("ALERT: " + monitoredEntity + " falls below the lower threshold!");
        } else {
            System.out.println("STATUS: " + monitoredEntity + " is within safe limits.");
        }
    }

    /**
     * Updates the current value of the monitored parameter.
     * This method is used to simulate real-time data changes in the monitored entity.
     *
     * @param currentValue the new value of the monitored parameter
     */
    public void updateCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    /**
     * Provides a string representation of the Monitoring Service.
     *
     * @return a string containing the details of the monitoring service
     */
    @Override
    public String toString() {
        return super.toString() +
                ", MonitoredEntity=" + monitoredEntity +
                ", UpperThreshold=" + upperThreshold +
                ", LowerThreshold=" + lowerThreshold +
                ", CurrentValue=" + currentValue + "]";
    }

    /**
     * Checks whether this MonitoringService is equal to another object.
     *
     * @param obj the object to compare with
     * @return true if the monitoring services are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;

        DT_Services_Monitoring<?> other = (DT_Services_Monitoring<?> ) obj;
        return monitoredEntity.equals(other.monitoredEntity) &&
                upperThreshold == other.upperThreshold &&
                lowerThreshold == other.lowerThreshold &&
                currentValue == other.currentValue;
    }

    /**
     * Generates a hash code for the Monitoring Service.
     *
     * @return the hash code of the monitoring service
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + monitoredEntity.hashCode();
        long temp;
        temp = Double.doubleToLongBits(upperThreshold);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lowerThreshold);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(currentValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }


}
