package Services;

/**
 * Class representing an Optimization Service in the Digital Twin framework.
 * An Optimization Service is responsible for enhancing system performance,
 * resource allocation, or other metrics by applying optimization algorithms.
 * This class extends the base Service class and adds optimization-specific attributes
 * and behaviors.
 */
public class DT_Services_Optimization<T> extends DT_Services<T> {

    @Override
    public void update(T data) {
        System.out.println("   → OPTIMIZATION SERVICE: Received new data for optimization: " + data);
        // Here you can add logic to process the new data and update the optimization results accordingly.
    }   

    /**
     * The optimization algorithm used by the service.
     * Examples include "Genetic Algorithm", "Linear Programming", or "Particle Swarm Optimization".
     */
    protected String optimizationAlgorithm;

    /**
     * Parameters for the optimization process.
     * This could include constraints, objective functions, and other settings.
     */
    protected String[] optimizationParameters;

    /**
     * The result of the optimization process.
     * This is generated after executing the optimization logic.
     */
    protected String optimizationResult;

    /**
     * Constructor to initialize the Optimization Service with necessary attributes.
     *
     * @param serviceName        the name of the service
     * @param serviceType        the type of the service (e.g., "Optimization")
     * @param serviceDescription a brief description of the service
     * @param optimizationAlgorithm the name of the optimization algorithm used
     * @param optimizationParameters the parameters required for the optimization process
     */
    public DT_Services_Optimization(String serviceName, String serviceType, String serviceDescription,
                               String optimizationAlgorithm, String[] optimizationParameters) {
        super(serviceName, serviceType, serviceDescription);
        this.optimizationAlgorithm = optimizationAlgorithm;
        this.optimizationParameters = optimizationParameters;
        this.optimizationResult = "No optimization performed yet."; // Default initial result
    }

    /**
     * Gets the optimization algorithm used by the service.
     *
     * @return the name of the optimization algorithm
     */
    public String getOptimizationAlgorithm() {
        return optimizationAlgorithm;
    }

    /**
     * Sets the optimization algorithm used by the service.
     *
     * @param optimizationAlgorithm the new optimization algorithm
     */
    public void setOptimizationAlgorithm(String optimizationAlgorithm) {
        this.optimizationAlgorithm = optimizationAlgorithm;
    }

    /**
     * Gets the parameters for the optimization process.
     *
     * @return the optimization parameters array
     */
    public String[] getOptimizationParameters() {
        return optimizationParameters;
    }

    /**
     * Sets the parameters for the optimization process.
     *
     * @param optimizationParameters the new optimization parameters array
     */
    public void setOptimizationParameters(String[] optimizationParameters) {
        this.optimizationParameters = optimizationParameters;
    }

    /**
     * Gets the result of the optimization process.
     *
     * @return the optimization result
     */
    public String getOptimizationResult() {
        return optimizationResult;
    }

    /**
     * Sets the result of the optimization process.
     *
     * @param optimizationResult the new optimization result
     */
    public void setOptimizationResult(String optimizationResult) {
        this.optimizationResult = optimizationResult;
    }


    /**
     * Executes the optimization logic.
     * This method applies the specified optimization algorithm to achieve
     * the desired objectives based on the provided parameters.
     */
    @Override
    public void executeService() {
        if (optimizationParameters == null || optimizationParameters.length == 0) {
            System.out.println("No optimization parameters provided. Unable to perform optimization.");
            optimizationResult = "Optimization failed due to lack of parameters.";
            return;
        }

        // Simplified logic for demonstration purposes.
        // In real scenarios, this could involve advanced optimization algorithms.
        StringBuilder resultBuilder = new StringBuilder("Optimization Result:\n");
        resultBuilder.append("Algorithm: ").append(optimizationAlgorithm).append("\n");

        // Simulated optimization process
        for (String parameter : optimizationParameters) {
            resultBuilder.append("Processed parameter: ").append(parameter).append("\n");
        }

        optimizationResult = resultBuilder.toString();
        System.out.println("Optimization completed successfully.");
    }

    /**
     * Provides a string representation of the Optimization Service.
     *
     * @return a string containing the details of the optimization service
     */
    @Override
    public String toString() {
        return super.toString() +
                ", OptimizationAlgorithm=" + optimizationAlgorithm +
                ", OptimizationResult=" + optimizationResult +
                ", OptimizationParameters=" + java.util.Arrays.toString(optimizationParameters) + "]";
    }

    /**
     * Checks whether this OptimizationService is equal to another object.
     *
     * @param obj the object to compare with
     * @return true if the optimization services are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;

        DT_Services_Optimization<?> other = (DT_Services_Optimization<?>) obj;
        return optimizationAlgorithm.equals(other.optimizationAlgorithm) &&
                optimizationResult.equals(other.optimizationResult) &&
                java.util.Arrays.equals(optimizationParameters, other.optimizationParameters);
    }

    /**
     * Generates a hash code for the Optimization Service.
     *
     * @return the hash code of the optimization service
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + optimizationAlgorithm.hashCode();
        result = 31 * result + optimizationResult.hashCode();
        result = 31 * result + java.util.Arrays.hashCode(optimizationParameters);
        return result;
    }


}
