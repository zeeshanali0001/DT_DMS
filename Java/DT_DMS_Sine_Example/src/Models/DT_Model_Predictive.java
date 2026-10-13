package Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a predictive model ({@code m ∈ M_pred}) of the Digital Twin.
 * <p>
 * A predictive model tells what is likely to happen in the ToI: it uses the historical and real-time data
 * it has assimilated to predict future states or behaviours.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_Model_Predictive<T> extends DT_Model<T> {

    /** The algorithm used for making predictions (e.g., "Moving average", "Linear Regression"). */
    protected String algorithm;

    /** The number of most recent values used for a prediction. */
    protected int window;

    /** The numeric values assimilated by the model, in assimilation order. */
    protected final List<Double> history = new ArrayList<>();

    /**
     * Constructor to initialize a predictive model with its attributes.
     *
     * @param modelName the name of the predictive model.
     * @param algorithm the algorithm used for prediction.
     * @param window    the number of most recent values used for a prediction (at least 1).
     */
    public DT_Model_Predictive(String modelName, String algorithm, int window) {
        super(modelName);
        this.algorithm = algorithm;
        this.window = Math.max(1, window);
    }

    /**
     * Updates the model with a new datum: numeric values are added to the history.
     *
     * @param updatedData the new datum.
     */
    @Override
    public void update(T updatedData) {
        double v = numericValue(updatedData);
        if (!Double.isNaN(v)) {
            history.add(v);
        }
    }

    /**
     * Predicts the next value as the mean of the last {@link #window} values (a simple placeholder
     * that subclasses can replace by a real algorithm).
     *
     * @return the predicted value, or {@code NaN} if no data has been assimilated.
     */
    public double predict() {
        if (history.isEmpty()) {
            return Double.NaN;
        }
        int from = Math.max(0, history.size() - window);
        double sum = 0.0;
        for (double v : history.subList(from, history.size())) {
            sum += v;
        }
        return sum / (history.size() - from);
    }

    /**
     * Executes the predictive model: prints its prediction.
     */
    @Override
    public void executeModel() {
        System.out.println("Executing predictive model: " + modelName);
        System.out.println("  Using algorithm: " + algorithm + " (window=" + window + ")");
        System.out.println("  Predicted value: " + predict());
    }

    /** @return the name of the algorithm. */
    public String getAlgorithm() {
        return algorithm;
    }

    /** @param algorithm the new algorithm name. */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /** @return the numeric values assimilated by the model. */
    public List<Double> getHistory() {
        return history;
    }

    /**
     * Provides a string representation of the predictive model, including its attributes.
     *
     * @return a string describing the predictive model.
     */
    @Override
    public String toString() {
        return "DT_Model_Predictive [modelName=" + modelName + ", algorithm=" + algorithm
                + ", window=" + window + ", assimilated=" + history.size() + "]";
    }
}
