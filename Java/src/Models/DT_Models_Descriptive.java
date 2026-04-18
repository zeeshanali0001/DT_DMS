package Models;

/**
 * Represents a descriptive model in the Digital Twin framework.
 * <p>
 * A descriptive model provides insights into historical or current data,
 * focusing on summarizing and interpreting the information.
 * </p>
 */
public class DT_Models_Descriptive<T> extends DT_Models<T> {

    /**
     * A summary of the data analyzed by the descriptive model.
     */
    protected String dataSummary;

    /**
     * The total number of data points analyzed in the model.
     */
    protected int dataPointCount;

    /**
     * The source of the data analyzed by the model.
     */
    protected String dataSource;

    /**
     * Constructor to initialize a DescriptiveModel with its attributes.
     *
     * @param modelName      the name of the descriptive model.
     * @param dataSource     the source of the data (e.g., "Sensor A", "Database X")
     * @param dataPointCount the total number of data points analyzed.
     * @param dataSummary    a brief summary of the analyzed data.

     .
     */
    public DT_Models_Descriptive(String modelName, String dataSource, int dataPointCount, String dataSummary) {
        super(modelName);
        this.dataSource = dataSource;
        this.dataPointCount = dataPointCount;
        this.dataSummary = dataSummary;
    }


    /**
     * Executes the descriptive model to generate insights based on historical data.
     * <p>
     * This method simulates the process of analyzing and summarizing data.
     * </p>
     */
    @Override
    public void executeModel() {
        System.out.println("Executing descriptive model: " + modelName);
        System.out.println("DigitalTwin_DMS_Framework.Data source: " + dataSource);
        System.out.println("Number of data points analyzed: " + dataPointCount);
        System.out.println("DigitalTwin_DMS_Framework.Data summary: " + dataSummary);
        // Additional logic for descriptive analysis to implement here.
    }



    /**
     * Provides a string representation of the descriptive model, including its attributes.
     *
     * @return a string describing the descriptive model.
     */
    @Override
    public String toString() {
        return "DescriptiveModel [modelName=" + modelName +
                ", dataSummary=" + dataSummary +
                ", dataPointCount=" + dataPointCount +
                ", dataSource=" + dataSource + "]";
    }

    /**
     * Checks if this descriptive model is equal to another object.
     * <p>
     * Two descriptive models are considered equal if they have the same name,
     * type, data summary, data point count, and data source.
     * </p>
     *
     * @param obj the object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DT_Models_Descriptive<?> other = (DT_Models_Descriptive<?>) obj;
        return modelName.equals(other.modelName) &&
                dataSummary.equals(other.dataSummary) &&
                dataPointCount == other.dataPointCount &&
                dataSource.equals(other.dataSource);
    }

    /**
     * Computes the hash code for this descriptive model based on its attributes.
     *
     * @return the hash code of the descriptive model.
     */
    @Override
    public int hashCode() {
        int result = modelName.hashCode();
        result = 31 * result + dataSummary.hashCode();
        result = 31 * result + dataPointCount;
        result = 31 * result + dataSource.hashCode();
        return result;
    }

    @Override
    public void update(T updatedData) {
        // Implement logic to update the descriptive model based on new data
        System.out.println("Descriptive model " + modelName + " received updated data: " + updatedData);
        // Additional logic to process the updated data can be added here.  
    }
}
