package Data;

/**
 * Class representing the characterization of a data source of the ToI: the metadata that tell how the data
 * of {@code D} were produced and how they must be interpreted.
 * <p>
 * Every {@link DT_Data} holds one characterization. It is used by the Digital Twin to check the data
 * characteristics (formats, units, ...) with respect to the services it provides
 * ({@code DT_DMS.checkDataVersusServices}).
 * </p>
 *
 * <p>The characteristics are:
 * <ul>
 *   <li>the unit of measurement (e.g., Celsius, Pascal, meter);</li>
 *   <li>the operational status of the system providing the data (e.g., active, inactive);</li>
 *   <li>the signal type: analog (continuous) or digital (discrete);</li>
 *   <li>the data flow type: event-triggered, continuous or periodic;</li>
 *   <li>the precision (level of tolerance error) of the sensor;</li>
 *   <li>the physical location of the sensor;</li>
 *   <li>the calibration status of the sensor (e.g., calibrated, not_calibrated);</li>
 *   <li>the date of the last maintenance performed on the sensor.</li>
 * </ul>
 * </p>
 * A characteristic that is not known is {@code null}.
 */
public class DT_DataCharacterization {

    /** Unit of measurement for the data, e.g., Celsius, Pascal, meter. */
    protected String unit;

    /** Current operational status of the system providing the data, e.g., active, inactive. */
    protected String systemStatus;

    /** Analog vs digital data signals: analog signals are continuous, digital signals are discrete. */
    protected String signalType;

    /** Event-triggered vs continuous flow of data: event-triggered, continuous or periodic. */
    protected String dataFlowType;

    /** Level of tolerance error of the sensor. */
    protected String sensorPrecision;

    /** Physical location of the sensor, describing the place where it is installed. */
    protected String physicalLocationOfTheSensor;

    /** Tells whether the sensor has been calibrated or not to deliver the data, e.g., calibrated, not_calibrated. */
    protected String sensorCalibrationStatus;

    /** Date of the last maintenance performed on the sensor. */
    protected String sensorLastMaintenanceDate;

    /** Creates a characterization where no characteristic is known. */
    public DT_DataCharacterization() {
    }

    /**
     * Copy constructor.
     *
     * @param other the characterization to copy.
     */
    public DT_DataCharacterization(DT_DataCharacterization other) {
        this.unit = other.unit;
        this.systemStatus = other.systemStatus;
        this.signalType = other.signalType;
        this.dataFlowType = other.dataFlowType;
        this.sensorPrecision = other.sensorPrecision;
        this.physicalLocationOfTheSensor = other.physicalLocationOfTheSensor;
        this.sensorCalibrationStatus = other.sensorCalibrationStatus;
        this.sensorLastMaintenanceDate = other.sensorLastMaintenanceDate;
    }

    /** @return the unit of measurement for the data, e.g., Celsius, Pascal, meter. */
    public String getUnit() {
        return unit;
    }

    /** @param unit the unit of measurement for the data. */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /** @return the current operational status of the system providing the data, e.g., active, inactive. */
    public String getSystemStatus() {
        return systemStatus;
    }

    /** @param systemStatus the operational status of the system providing the data. */
    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
    }

    /** @return the signal type: analog (continuous) or digital (discrete). */
    public String getSignalType() {
        return signalType;
    }

    /** @param signalType the signal type: analog or digital. */
    public void setSignalType(String signalType) {
        this.signalType = signalType;
    }

    /** @return the data flow type: event-triggered, continuous or periodic. */
    public String getDataFlowType() {
        return dataFlowType;
    }

    /** @param dataFlowType the data flow type: event-triggered, continuous or periodic. */
    public void setDataFlowType(String dataFlowType) {
        this.dataFlowType = dataFlowType;
    }

    /** @return the level of tolerance error of the sensor. */
    public String getSensorPrecision() {
        return sensorPrecision;
    }

    /** @param sensorPrecision the level of tolerance error of the sensor. */
    public void setSensorPrecision(String sensorPrecision) {
        this.sensorPrecision = sensorPrecision;
    }

    /** @return the physical location of the sensor. */
    public String getPhysicalLocationOfTheSensor() {
        return physicalLocationOfTheSensor;
    }

    /** @param physicalLocationOfTheSensor the physical location of the sensor. */
    public void setPhysicalLocationOfTheSensor(String physicalLocationOfTheSensor) {
        this.physicalLocationOfTheSensor = physicalLocationOfTheSensor;
    }

    /** @return whether the sensor has been calibrated, e.g., calibrated, not_calibrated. */
    public String getSensorCalibrationStatus() {
        return sensorCalibrationStatus;
    }

    /** @param sensorCalibrationStatus the calibration status of the sensor. */
    public void setSensorCalibrationStatus(String sensorCalibrationStatus) {
        this.sensorCalibrationStatus = sensorCalibrationStatus;
    }

    /** @return the date of the last maintenance performed on the sensor. */
    public String getSensorLastMaintenanceDate() {
        return sensorLastMaintenanceDate;
    }

    /** @param sensorLastMaintenanceDate the date of the last maintenance performed on the sensor. */
    public void setSensorLastMaintenanceDate(String sensorLastMaintenanceDate) {
        this.sensorLastMaintenanceDate = sensorLastMaintenanceDate;
    }

    /**
     * Converts the characterization into a human-readable string format.
     *
     * @return the characteristics, {@code null} for the unknown ones.
     */
    @Override
    public String toString() {
        return "DT_DataCharacterization{unit=" + unit + ", systemStatus=" + systemStatus
                + ", signalType=" + signalType + ", dataFlowType=" + dataFlowType
                + ", sensorPrecision=" + sensorPrecision + ", location=" + physicalLocationOfTheSensor
                + ", calibration=" + sensorCalibrationStatus + ", lastMaintenance=" + sensorLastMaintenanceDate + "}";
    }
}
