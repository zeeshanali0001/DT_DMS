package Services;

/**
 * Class representing a Prediction Service in the Digital Twin framework.
 * A Prediction Service uses data and models to forecast future states or events.
 * This class extends the base Service class and adds prediction-specific attributes
 * and behaviors, such as the predictive model, historical data, real-time data and prediction results.
 */
public class DT_Services_Prediction<T> extends DT_Services<T> {


    @Override
    public void update(T data) {    
        System.out.println("   → PREDICTION SERVICE: Received new data for prediction: " + data);
        // Here you can add logic to process the new data and update the prediction results accordingly.
    }



    /**
     * The name of the predictive model used by this service (e.g., "Linear Regression", "Neural Network").
     */
    protected String predictiveModel;

    /**
     * A dataset representing historical data used for making predictions.
     * This could be a list of numerical values, time series, or more complex data types.
     */
    protected double[] historicalData;

    /**
     * The predicted value based on the input data and predictive model.
     * This will be computed by the executeService method.
     */
    protected double predictedValue;

    /**
     * Constructor to initialize the Prediction Service with necessary attributes.
     *
     * @param serviceName        the name of the service
     * @param serviceType        the type of the service (e.g., "Prediction")
     * @param serviceDescription a brief description of the service
     * @param predictiveModel    the predictive model used for predictions
     * @param historicalData     the dataset of historical data for prediction
     */
    public DT_Services_Prediction(String serviceName, String serviceType, String serviceDescription,
                             String predictiveModel, double[] historicalData) {
        super(serviceName, serviceType, serviceDescription);
        this.predictiveModel = predictiveModel;
        this.historicalData = historicalData;
        this.predictedValue = 0.0; // Default initial prediction value
    }
    /**
     * Gets the name of the predictive model used.
     *
     * @return the name of the predictive model
     */
    public String getPredictiveModel() {
        return predictiveModel;
    }

    /**
     * Sets the name of the predictive model used.
     *
     * @param predictiveModel the new name of the predictive model
     */
    public void setPredictiveModel(String predictiveModel) {
        this.predictiveModel = predictiveModel;
    }

    /**
     * Gets the historical data used for making predictions.
     *
     * @return the historical data array
     */
    public double[] getHistoricalData() {
        return historicalData;
    }

    /**
     * Sets the historical data for prediction.
     *
     * @param historicalData the new historical data array
     */
    public void setHistoricalData(double[] historicalData) {
        this.historicalData = historicalData;
    }

    /**
     * Gets the predicted value calculated by the service.
     *
     * @return the predicted value
     */
    public double getPredictedValue() {
        return predictedValue;
    }

    /**
     * Sets the predicted value.
     *
     * @param predictedValue the new predicted value
     */
    public void setPredictedValue(double predictedValue) {
        this.predictedValue = predictedValue;
    }



    /**
     * Executes the prediction logic.
     * This method uses the historical data and the predictive model to make a forecast.
     * The predicted value is calculated based on the model and data provided.
     */
    @Override
    public void executeService() {
        // For simplicity, we're using a very basic prediction model (e.g., average of historical data).
        // In a real-world scenario, this would involve complex algorithms.
        if (historicalData == null || historicalData.length == 0) {
            System.out.println("No historical data available for prediction.");
            return;
        }

        double sum = 0.0;
        for (double dataPoint : historicalData) {
            sum += dataPoint;
        }

        // Simple average-based prediction (this could be replaced by a real model like Linear Regression, etc.)
        this.predictedValue = sum / historicalData.length;

        // Display the predicted value
        System.out.println("Prediction based on " + predictiveModel + ": " + predictedValue);
    }

    /**
     * Provides a string representation of the Prediction Service.
     *
     * @return a string containing the details of the prediction service
     */
    @Override
    public String toString() {
        return super.toString() +
                ", PredictiveModel=" + predictiveModel +
                ", PredictedValue=" + predictedValue +
                ", HistoricalData=" + java.util.Arrays.toString(historicalData) + "]";
    }

    /**
     * Checks whether this PredictionService is equal to another object.
     *
     * @param obj the object to compare with
     * @return true if the prediction services are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;

        DT_Services_Prediction<?> other = (DT_Services_Prediction<?>) obj;
        return predictiveModel.equals(other.predictiveModel) &&
                predictedValue == other.predictedValue &&
                java.util.Arrays.equals(historicalData, other.historicalData);
    }

    /**
     * Generates a hash code for the Prediction Service.
     *
     * @return the hash code of the prediction service
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + predictiveModel.hashCode();
        result = 31 * result + java.util.Arrays.hashCode(historicalData);
        long temp = Double.doubleToLongBits(predictedValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }


}
