package Services;

/**
 * Class representing a Diagnosis Service in the Digital Twin framework.
 * A Diagnosis Service is responsible for identifying faults, anomalies, or inefficiencies
 * in systems or processes based on diagnostic models and input data.
 * This class extends the base Service class and adds diagnosis-specific attributes
 * and behaviors.
 */
public class DT_Services_Diagnosis<T> extends DT_Services<T> {


    @Override
    public void update(T data) { 
        System.out.println("   → DIAGNOSIS SERVICE: Received new data for diagnosis: " + data);
        // Here you can add logic to process the new data and update the diagnosis report accordingly.
    }   


    /**
     * The diagnostic model used to perform fault detection or anomaly identification.
     * This could be a rule-based system, machine learning model, or any other diagnostic algorithm.
     */
    protected String diagnosticModel;

    /**
     * Input data used for performing diagnostics.
     * This data could include sensor readings, performance metrics, or operational logs.
     */
    protected double[] diagnosticData;

    /**
     * A detailed report containing the results of the diagnosis process.
     * This is generated after executing the diagnosis logic.
     */
    protected String diagnosisReport;

    /**
     * Constructor to initialize the Diagnosis Service with necessary attributes.
     *
     * @param serviceName        the name of the service
     * @param serviceType        the type of the service (e.g., "Diagnosis")
     * @param serviceDescription a brief description of the service
     * @param diagnosticModel    the model or algorithm used for diagnosis
     * @param diagnosticData     the input data used for diagnostic analysis
     */
    public DT_Services_Diagnosis(String serviceName, String serviceType, String serviceDescription,
                            String diagnosticModel, double[] diagnosticData) {
        super(serviceName, serviceType, serviceDescription);
        this.diagnosticModel = diagnosticModel;
        this.diagnosticData = diagnosticData;
        this.diagnosisReport = "No diagnosis performed yet."; // Default initial report
    }


    /**
     * Gets the name of the diagnostic model used.
     *
     * @return the name of the diagnostic model
     */
    public String getDiagnosticModel() {
        return diagnosticModel;
    }

    /**
     * Sets the name of the diagnostic model used.
     *
     * @param diagnosticModel the new name of the diagnostic model
     */
    public void setDiagnosticModel(String diagnosticModel) {
        this.diagnosticModel = diagnosticModel;
    }

    /**
     * Gets the input data used for diagnostics.
     *
     * @return the diagnostic data array
     */
    public double[] getDiagnosticData() {
        return diagnosticData;
    }

    /**
     * Sets the input data for diagnostics.
     *
     * @param diagnosticData the new diagnostic data array
     */
    public void setDiagnosticData(double[] diagnosticData) {
        this.diagnosticData = diagnosticData;
    }

    /**
     * Gets the report generated after diagnosis.
     *
     * @return the diagnosis report
     */
    public String getDiagnosisReport() {
        return diagnosisReport;
    }

    /**
     * Sets the diagnosis report.
     *
     * @param diagnosisReport the new diagnosis report
     */
    public void setDiagnosisReport(String diagnosisReport) {
        this.diagnosisReport = diagnosisReport;
    }


    /**
     * Executes the diagnosis logic.
     * This method analyzes the diagnostic data using the diagnostic model to identify
     * faults, anomalies, or inefficiencies, and generates a diagnosis report.
     */
    @Override
    public void executeService() {
        if (diagnosticData == null || diagnosticData.length == 0) {
            System.out.println("No diagnostic data available. Unable to perform diagnosis.");
            diagnosisReport = "Diagnosis failed due to lack of data.";
            return;
        }

        // Simplified logic for demonstration purposes.
        // In real scenarios, this could involve complex anomaly detection algorithms.
        boolean anomalyDetected = false;
        StringBuilder reportBuilder = new StringBuilder("Diagnosis Report:\n");

        // Basic anomaly detection: Identify values outside a hypothetical threshold.
        for (int i = 0; i < diagnosticData.length; i++) {
            if (diagnosticData[i] < 0 || diagnosticData[i] > 100) { // Example threshold
                anomalyDetected = true;
                reportBuilder.append("Anomaly detected at index ").append(i)
                        .append(" with value: ").append(diagnosticData[i]).append("\n");
            }
        }

        if (anomalyDetected) {
            diagnosisReport = reportBuilder.toString();
            System.out.println("Anomalies found. Diagnosis completed.");
        } else {
            diagnosisReport = "No anomalies detected. System functioning within normal parameters.";
            System.out.println("No anomalies detected. Diagnosis completed.");
        }
    }

    /**
     * Provides a string representation of the Diagnosis Service.
     *
     * @return a string containing the details of the diagnosis service
     */
    @Override
    public String toString() {
        return super.toString() +
                ", DiagnosticModel=" + diagnosticModel +
                ", DiagnosisReport=" + diagnosisReport +
                ", DiagnosticData=" + java.util.Arrays.toString(diagnosticData) + "]";
    }

    /**
     * Checks whether this DiagnosisService is equal to another object.
     *
     * @param obj the object to compare with
     * @return true if the diagnosis services are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;

        DT_Services_Diagnosis<?> other = (DT_Services_Diagnosis<?>) obj;
        return diagnosticModel.equals(other.diagnosticModel) &&
                diagnosisReport.equals(other.diagnosisReport) &&
                java.util.Arrays.equals(diagnosticData, other.diagnosticData);
    }

    /**
     * Generates a hash code for the Diagnosis Service.
     *
     * @return the hash code of the diagnosis service
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + diagnosticModel.hashCode();
        result = 31 * result + diagnosisReport.hashCode();
        result = 31 * result + java.util.Arrays.hashCode(diagnosticData);
        return result;
    }


}
