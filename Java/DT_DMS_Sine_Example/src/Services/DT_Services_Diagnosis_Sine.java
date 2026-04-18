package Services;

import Data.DT_Data;
import Data.DT_Data_SineValues;
import Models.DT_Models_Descr_CubicBSpline;
import Visualization.DT_ChartPanel;


import java.util.concurrent.ThreadLocalRandom;
/**
 * Diagnosis service for sine wave data.
 * Performs random validation checks by comparing raw sine values with model predictions.
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 1.0
 */




public class DT_Services_Diagnosis_Sine extends DT_Services_Diagnostic {

    /** Reference to the Cubic B-Spline model used for comparison */
    protected DT_Models_Descr_CubicBSpline _CS_model;

    /** Reference to the latest data source (used for index and raw values) */
    private DT_Data_SineValues latestData;

    /** Live visualization chart panel */
    private final DT_ChartPanel chartPanel = new DT_ChartPanel();

    /** Threshold for anomaly detection (error between model and actual value) */
    private static final double ANOMALY_THRESHOLD = 0.15;

    /**
     * Constructor - links this service with the model.
     *
     * @param model The Cubic B-Spline model instance
     */
    public DT_Services_Diagnosis_Sine(DT_Models_Descr_CubicBSpline model) {
        this._CS_model = model;
    }

    /**
     * Sets the reference to the latest data source.
     * This is needed for chart updates and accessing raw sine values.
     *
     * @param data The sine data source
     */
    public void setLatestData(DT_Data_SineValues data) {
        this.latestData = data;
    }

    @Override
    public void update(DT_Data dat) {
        if (dat instanceof DT_Data_SineValues) {
            performRandomCheck(5);
        }
    }

    /**
     * Called by the model when new data is processed.
     * Performs diagnosis, anomaly detection, and updates the live chart.
     */
    public void updateServiceWithModel() {
        performRandomCheck(10);
        System.out.println("=== SineDiagnosis_Service::updateServiceWithModel ===");

        if (latestData == null || latestData.getSineDataValues().isEmpty()) {
            return;
        }

        // Get latest values for chart and diagnosis
        int index = latestData.getCurrentIndex();
        double x = index * _CS_model.getStep();
        double rawSineValue = latestData.getSineDataValues().get(index - 1);
        double modelValue = _CS_model.getSineModelValue(x);

        // Update live chart
        chartPanel.updateChart(index, rawSineValue, modelValue);

        // Perform diagnosis - detect anomaly
        detectAnomaly(index, rawSineValue, modelValue);
    }

    /**
     * Performs random validation checks (original behavior).
     *
     * @param count Number of random checks to perform
     */
    protected void performRandomCheck(int count) {
        for (int i = 0; i < count; i++) {
            double x = ThreadLocalRandom.current().nextDouble(0, 5.0);
            System.out.printf("      sin(%.4f) = %.4f | spline = %.4f%n",
                    x, Math.sin(x), _CS_model.getSineModelValue(x));
        }
    }

    /**
     * Detects anomalies by comparing raw sine value with model prediction.
     * If error exceeds threshold, it prints an alert (can be extended to log, notify, etc.).
     *
     * @param index Current sample index
     * @param actual Actual value from physical data
     * @param expected Expected value from the model
     */
    private void detectAnomaly(int index, double actual, double expected) {
        double error = Math.abs(actual - expected);

        if (error > ANOMALY_THRESHOLD) {
            System.out.println("*** ANOMALY DETECTED! *** Index=" + index 
                             + " | Actual=" + actual 
                             + " | Expected=" + expected 
                             + " | Error=" + error);
            // TODO: Add alert, email, shutdown logic, or trigger recovery here
        }
    }
}