package Models;

import java.util.List;

/**
 * Represents a perspective model in the Digital Twin framework.
 * <p>
 * A perspective model focuses on offering insights, recommendations, or
 * projections by integrating multiple viewpoints and data sources.
 * It is often used for decision-making and scenario analysis.
 * </p>
 */
public class DT_Models_Prescriptive<T> extends DT_Models<T> {

    /**
     * The goal or objective of the perspective model (e.g., "Optimize Energy Usage").
     */
    protected String objective;

    /**
     * A detailed description of the perspective or viewpoint offered by the model.
     */
    protected String PrescriptiveDescription;

    /**
     * A list of data sources considered in the model for generating insights.
     */
    protected List<String> dataSources;

    /**
     * The confidence level of the recommendations provided by the model, represented as a percentage.
     */
    protected double confidenceLevel;

    /**
     * Any assumptions made while developing or executing the perspective model.
     */
    protected String assumptions;

    /**
     * Constructor to initialize a PerspectiveModel with its attributes.
     *
     * @param modelName              the name of the perspective model.
     * @param objective              the primary goal or objective of the model.
     * @param perspectiveDescription a description of the perspective offered.
     * @param dataSources            a list of data sources used in the model.
     * @param confidenceLevel        the confidence level of the model's output.
     * @param assumptions            the assumptions considered in the model.
     */
    public DT_Models_Prescriptive(String modelName, String objective,
                            String perspectiveDescription, List<String> dataSources,
                            double confidenceLevel, String assumptions) {
        super(modelName);
        this.objective = objective;
        this.PrescriptiveDescription = perspectiveDescription;
        this.dataSources = dataSources;
        this.confidenceLevel = confidenceLevel;
        this.assumptions = assumptions;
    }


    /**
     * Executes the perspective model to generate insights and recommendations.
     * <p>
     * This method simulates the process of integrating data sources and generating
     * a perspective based on the model's objectives and assumptions.
     * </p>
     */
    @Override
    public void executeModel() {
        System.out.println("Executing perspective model: " + modelName);
        System.out.println("Objective: " + objective);
        System.out.println("Perspective: " + PrescriptiveDescription);
        System.out.println("DigitalTwin_DMS_Framework.Data sources used: " + String.join(", ", dataSources));
        System.out.println("Confidence level: " + confidenceLevel + "%");
        // Additional logic for generating insights can be added here.
    }


    /**
     * Provides a string representation of the perspective model, including its attributes.
     *
     * @return a string describing the perspective model.
     */
    @Override
    public String toString() {
        return "PerspectiveModel [modelName=" + modelName +
                ", objective=" + objective +
                ", perspectiveDescription=" + PrescriptiveDescription +
                ", dataSources=" + dataSources +
                ", confidenceLevel=" + confidenceLevel +
                ", assumptions=" + assumptions + "]";
    }

    /**
     * Checks if this perspective model is equal to another object.
     * <p>
     * Two perspective models are considered equal if they have the same name,
     * type, objective, perspective description, data sources, confidence level, and assumptions.
     * </p>
     *
     * @param obj the object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        if (obj instanceof DT_Models_Prescriptive) {
            DT_Models_Prescriptive<?> other = (DT_Models_Prescriptive<?>) obj;
            return modelName.equals(other.modelName) &&
                    objective.equals(other.objective) &&
                    PrescriptiveDescription.equals(other.PrescriptiveDescription) &&
                    dataSources.equals(other.dataSources) &&
                    confidenceLevel == other.confidenceLevel &&
                assumptions.equals(other.assumptions);
        }
        return false;
    }

    /**
     * Computes the hash code for this perspective model based on its attributes.
     *
     * @return the hash code of the perspective model.
     */
    @Override
    public int hashCode() {
        int result = modelName.hashCode();
        result = 31 * result + objective.hashCode();
        result = 31 * result + PrescriptiveDescription.hashCode();
        result = 31 * result + dataSources.hashCode();
        result = 31 * result + Double.hashCode(confidenceLevel);
        result = 31 * result + assumptions.hashCode();
        return result;
    }



    @Override
    public void update(T updatedData) {
        // Implement logic to update the model based on new data
        System.out.println("Perspective model " + modelName + " received updated data: " + updatedData);
        // Additional logic to process the updated data can be added here.
    }
}
