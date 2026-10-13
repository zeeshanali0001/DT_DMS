package Services;

import Models.DT_Model_Prescriptive;

/**
 * Class representing an Optimization Service ({@code s ∈ S_opti}) of the Digital Twin.
 * <p>
 * An Optimization Service finds the best solution for the ToI (performance, resource allocation, ...).
 * It relies on the prescriptive model it is coupled to for the recommendation.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_Service_Optimization<T> extends DT_Service<T> {

    /** The optimization algorithm used (e.g., "Genetic Algorithm", "Linear Programming"). */
    protected String optimizationAlgorithm;

    /** The result of the last optimization. */
    protected String optimizationResult = "No optimization performed yet.";

    /**
     * Constructor to initialize the Optimization Service with necessary attributes.
     *
     * @param serviceName           the name of the service.
     * @param serviceDescription    a brief description of the service.
     * @param optimizationAlgorithm the algorithm used for optimization.
     */
    public DT_Service_Optimization(String serviceName, String serviceDescription, String optimizationAlgorithm) {
        super(serviceName, serviceDescription);
        this.optimizationAlgorithm = optimizationAlgorithm;
    }

    /**
     * Executes the optimization logic: asks the coupled prescriptive model for its recommendation.
     *
     * @return the optimization result.
     */
    @Override
    public Object executeService() {
        if (model instanceof DT_Model_Prescriptive<T> prescriptive) {
            optimizationResult = "Optimization (" + optimizationAlgorithm + ") for objective '"
                    + prescriptive.getObjective() + "': " + prescriptive.recommend();
        } else {
            optimizationResult = "Optimization failed: no prescriptive model is coupled to the service.";
        }
        return optimizationResult;
    }

    /** @return the optimization algorithm. */
    public String getOptimizationAlgorithm() {
        return optimizationAlgorithm;
    }

    /** @return the result of the last optimization. */
    public String getOptimizationResult() {
        return optimizationResult;
    }
}
