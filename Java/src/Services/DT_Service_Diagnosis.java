package Services;

/**
 * Class representing a Diagnosis Service ({@code s ∈ S_diag}) of the Digital Twin.
 * <p>
 * A Diagnosis Service identifies faults, anomalies or inefficiencies of the ToI from the data it
 * consumes: here, the values that fall outside a normal range.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_Service_Diagnosis<T> extends DT_Service<T> {

    /** The lower bound of the normal range. */
    protected double normalMinimum;

    /** The upper bound of the normal range. */
    protected double normalMaximum;

    /** The report with the results of the last diagnosis. */
    protected String diagnosisReport = "No diagnosis performed yet.";

    /**
     * Constructor to initialize the Diagnosis Service with necessary attributes.
     *
     * @param serviceName        the name of the service.
     * @param serviceDescription a brief description of the service.
     * @param normalMinimum      the lower bound of the normal range.
     * @param normalMaximum      the upper bound of the normal range.
     */
    public DT_Service_Diagnosis(String serviceName, String serviceDescription,
                                double normalMinimum, double normalMaximum) {
        super(serviceName, serviceDescription);
        this.normalMinimum = normalMinimum;
        this.normalMaximum = normalMaximum;
    }

    /**
     * Executes the diagnosis logic: reports every consumed value outside the normal range.
     *
     * @return the diagnosis report.
     */
    @Override
    public Object executeService() {
        if (consumedData.isEmpty()) {
            diagnosisReport = "Diagnosis failed due to lack of data.";
            return diagnosisReport;
        }
        StringBuilder report = new StringBuilder();
        for (int i = 0; i < consumedData.size(); i++) {
            double v = numericValue(consumedData.get(i));
            if (v < normalMinimum || v > normalMaximum) {
                report.append("Anomaly at index ").append(i).append(" with value ").append(v).append(". ");
            }
        }
        diagnosisReport = report.length() == 0
                ? "No anomalies detected. System functioning within normal parameters."
                : report.toString().trim();
        return diagnosisReport;
    }

    /** @return the report of the last diagnosis. */
    public String getDiagnosisReport() {
        return diagnosisReport;
    }
}
