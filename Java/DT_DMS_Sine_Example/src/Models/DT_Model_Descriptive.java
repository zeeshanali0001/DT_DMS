package Models;

/**
 * Represents a descriptive model ({@code m ∈ M_desc}) of the Digital Twin.
 * <p>
 * A descriptive model describes what happened or what is happening in the ToI: it summarizes and
 * interprets the historical and real-time data it has assimilated.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_Model_Descriptive<T> extends DT_Model<T> {

    /** The source of the data described by the model. */
    protected String dataSource;

    /** The total number of data points assimilated by the model. */
    protected int dataPointCount;

    /** A summary of the data assimilated by the model. */
    protected String dataSummary = "no data";

    /** The smallest numeric value assimilated. */
    protected double minimum = Double.POSITIVE_INFINITY;

    /** The largest numeric value assimilated. */
    protected double maximum = Double.NEGATIVE_INFINITY;

    /** The sum of the numeric values assimilated (used for the mean). */
    protected double sum;

    /**
     * Constructor to initialize a descriptive model with its attributes.
     *
     * @param modelName  the name of the descriptive model.
     * @param dataSource the source of the data (e.g., "Sensor A", "Database X").
     */
    public DT_Model_Descriptive(String modelName, String dataSource) {
        super(modelName);
        this.dataSource = dataSource;
    }

    /**
     * Updates the description with a new datum: counts it and, if it is a number, updates the
     * minimum, maximum and mean.
     *
     * @param updatedData the new datum.
     */
    @Override
    public void update(T updatedData) {
        dataPointCount++;
        double v = numericValue(updatedData);
        if (!Double.isNaN(v)) {
            minimum = Math.min(minimum, v);
            maximum = Math.max(maximum, v);
            sum += v;
            dataSummary = "min=" + minimum + ", max=" + maximum + ", mean=" + getMean();
        } else {
            dataSummary = "last=" + updatedData;
        }
    }

    /**
     * Executes the descriptive model: prints the description of the data assimilated so far.
     */
    @Override
    public void executeModel() {
        System.out.println("Executing descriptive model: " + modelName);
        System.out.println("  Data source: " + dataSource);
        System.out.println("  Number of data points analyzed: " + dataPointCount);
        System.out.println("  Data summary: " + dataSummary);
    }

    /** @return the mean of the numeric values assimilated ({@code NaN} if none). */
    public double getMean() {
        return dataPointCount == 0 ? Double.NaN : sum / dataPointCount;
    }

    /** @return the source of the data. */
    public String getDataSource() {
        return dataSource;
    }

    /** @return the total number of data points assimilated. */
    public int getDataPointCount() {
        return dataPointCount;
    }

    /** @return the summary of the data assimilated. */
    public String getDataSummary() {
        return dataSummary;
    }

    /**
     * Provides a string representation of the descriptive model, including its attributes.
     *
     * @return a string describing the descriptive model.
     */
    @Override
    public String toString() {
        return "DT_Model_Descriptive [modelName=" + modelName + ", dataSource=" + dataSource
                + ", dataPointCount=" + dataPointCount + ", dataSummary=" + dataSummary + "]";
    }
}
