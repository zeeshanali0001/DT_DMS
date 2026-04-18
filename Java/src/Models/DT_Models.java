package Models;
import DataObserver.*;


/**
 * Abstract base class representing a generic model in the Digital Twin framework.
 * <p>
 * This class defines common properties and methods that all specific models
 * (e.g., DescriptiveModel, PredictiveModel, PrescriptiveModel) should inherit and implement.
 * </p>
 *
 */
public abstract class DT_Models<T> implements ObserverData<T> {
    /** 
     * The name of the model, which uniquely identifies it.
     */
    protected String modelName;

    @Override 
    public void update(T updatedData) {

        // Default implementation can be overridden by subclasses
        // Add more model logic here (e.g., update database, recalculate forecasts, etc.)
        System.out.println("Model " + modelName + " received updated data: " + updatedData);
    }

    /**
     * Constructor to initialize the model's name and type.
     *
     * @param modelName the name of the model.
     */
    public DT_Models(String modelName) {
        this.modelName = modelName;

    }

    /**
     * Retrieves the name of the model.
     *
     * @return the model name.
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sets the name of the model.
     *
     * @param modelName the new model name.
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }



    /**
     * Abstract method to execute the core logic of the model.
     * <p>
     * Each subclass must provide its specific implementation for this method.
     * </p>
     */
    public abstract void executeModel();



    /**
     * Provides a string representation of the model, useful for debugging or logging.
     *
     * @return a string describing the model's name and type.
     */
    @Override
    public String toString() {
        return "Model [modelName=" + modelName + "]";
    }

    /**
     * Checks if this model is equal to another object.
     * <p>
     * Two models are considered equal if they have the same name and type.
     * </p>
     *
     * @param obj the object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
        {return true;}
        if (obj == null || getClass() != obj.getClass())
        {return false;}

        DT_Models<?> other = (DT_Models<?>) obj;
        return modelName.equals(other.modelName) ;
    }

    /**
     * Computes the hash code for this model based on its name and type.
     *
     * @return the hash code of the model.
     */
    @Override
    public int hashCode() {
        int result = modelName.hashCode();
        return result;
    }
}
