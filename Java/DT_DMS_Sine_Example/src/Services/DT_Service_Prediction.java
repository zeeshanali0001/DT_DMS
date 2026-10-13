package Services;

import Models.DT_Model_Predictive;

/**
 * Class representing a Prediction Service ({@code s ∈ S_pred}) of the Digital Twin.
 * <p>
 * A Prediction Service forecasts future states of the ToI. It uses the predictive model it is coupled
 * to; without such a model it falls back to the mean of the data it has consumed.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_Service_Prediction<T> extends DT_Service<T> {

    /** The predicted value, computed by {@link #executeService()} ({@code NaN} if none). */
    protected double predictedValue = Double.NaN;

    /**
     * Constructor to initialize the Prediction Service.
     *
     * @param serviceName        the name of the service.
     * @param serviceDescription a brief description of the service.
     */
    public DT_Service_Prediction(String serviceName, String serviceDescription) {
        super(serviceName, serviceDescription);
    }

    /**
     * Executes the prediction logic: asks the coupled predictive model, or else averages the consumed data.
     *
     * @return the predicted value (a {@code Double}; {@code NaN} if nothing can be predicted).
     */
    @Override
    public Object executeService() {
        if (model instanceof DT_Model_Predictive<T> predictive) {
            predictedValue = predictive.predict();
        } else {
            double sum = 0.0;
            int count = 0;
            for (T d : consumedData) {
                double v = numericValue(d);
                if (!Double.isNaN(v)) {
                    sum += v;
                    count++;
                }
            }
            predictedValue = count == 0 ? Double.NaN : sum / count;
        }
        return predictedValue;
    }

    /** @return the last predicted value. */
    public double getPredictedValue() {
        return predictedValue;
    }
}
