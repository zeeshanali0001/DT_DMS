package Models;

/**
 * Represents the predictive models in the Digital Twin framework.
 * <p>
 * A predictive model utilizes machine learning or statistical algorithms
 * to predict future outcomes or behaviors based on historical data.
 * </p>
 */
public class DT_Models_Predictive<T> extends DT_Models<T> {

    /**
     * The algorithm used for making predictions (e.g., "Linear Regression", "Neural Network").
     */
    private String algorithm;

    /**
     * A description of the dataset or data source used for training the predictive model.
     */
    private String dataDescription;

    /**
     * The accuracy of the predictive model, represented as a percentage (0-100).
     */
    private double accuracy;

    /**
     * Constructor to initialize a PredictiveModel with its attributes.
     *
     * @param modelName       the name of the predictive model.
     * @param algorithm       the algorithm used for prediction.
     * @param dataDescription a brief description of the dataset used for predictions.
     * @param accuracy        the accuracy of the model, represented as a percentage (0-100).
     */
    public DT_Models_Predictive(String modelName, String algorithm, String dataDescription, double accuracy) {
        super(modelName);
        this.algorithm = algorithm;
        this.dataDescription = dataDescription;
        this.accuracy = accuracy;
    }


    /**
     * Retrieves the algorithm used by the predictive model.
     *
     * @return the name of the algorithm.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the algorithm for the predictive model.
     *
     * @param algorithm the new algorithm name.
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Retrieves the description of the dataset or data source used.
     *
     * @return the dataset description.
     */
    public String getDataDescription() {
        return dataDescription;
    }

    /**
     * Sets the description of the dataset or data source used.
     *
     * @param dataDescription the new dataset description.
     */
    public void setDataDescription(String dataDescription) {
        this.dataDescription = dataDescription;
    }

    /**
     * Retrieves the accuracy of the predictive model.
     *
     * @return the accuracy as a percentage.
     */
    public double getAccuracy() {
        return accuracy;
    }

    /**
     * Sets the accuracy of the predictive model.
     *
     * @param accuracy the new accuracy percentage.
     */
    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    /**
     * Executes the predictive model using its algorithm.
     * <p>
     * This method simulates the execution process and provides output
     * based on the predictive algorithm.
     * </p>
     */
    @Override
    public void executeModel() {
        System.out.println("Executing predictive model: " + modelName);
        System.out.println("Using algorithm: " + algorithm);
        System.out.println("Model accuracy: " + accuracy + "%");
        // Additional logic for executing the predictive model can be added here.
    }





    /**
     * Provides a string representation of the predictive model, including its attributes.
     *
     * @return a string describing the predictive model.
     */
    @Override
    public String toString() {
        return "PredictiveModel [modelName=" + modelName +
                ", algorithm=" + algorithm +
                ", dataDescription=" + dataDescription +
                ", accuracy=" + accuracy + "]";
    }

    /**
     * Checks if this predictive model is equal to another object.
     * <p>
     * Two predictive models are considered equal if they have the same name,
     * type, algorithm, data description, and accuracy.
     * </p>
     *
     * @param obj the object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DT_Models_Predictive<?> other = (DT_Models_Predictive<?>) obj;
        return modelName.equals(other.modelName) &&
                algorithm.equals(other.algorithm) &&
                dataDescription.equals(other.dataDescription) &&
                Double.compare(other.accuracy, accuracy) == 0;
    }

    /**
     * Computes the hash code for this predictive model based on its attributes.
     *
     * @return the hash code of the predictive model.
     */
    @Override
    public int hashCode() {
        int result = modelName.hashCode();
        result = 31 * result + algorithm.hashCode();
        result = 31 * result + dataDescription.hashCode();
        result = 31 * result + Double.hashCode(accuracy);
        return result;
    }

    @Override
    public void update(T updatedData) {
        // Implement logic to update the predictive model based on new data
        System.out.println("Predictive model " + modelName + " received updated data: " + updatedData); 
        // Additional logic to process the updated data can be added here.
    }
}
