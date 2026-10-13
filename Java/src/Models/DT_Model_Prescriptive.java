package Models;

/**
 * Represents a prescriptive model ({@code m ∈ M_presc}) of the Digital Twin.
 * <p>
 * A prescriptive model tells what should be done on the ToI: from the data it has assimilated, it
 * recommends the action to take in order to reach an objective.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_Model_Prescriptive<T> extends DT_Model<T> {

    /** The goal or objective of the prescriptive model (e.g., "Keep temperature at 22"). */
    protected String objective;

    /** The target value of the objective. */
    protected double target;

    /** The latest numeric value assimilated by the model. */
    protected double lastValue = Double.NaN;

    /** Any assumptions made while developing or executing the prescriptive model. */
    protected String assumptions;

    /**
     * Constructor to initialize a prescriptive model with its attributes.
     *
     * @param modelName   the name of the prescriptive model.
     * @param objective   the primary goal or objective of the model.
     * @param target      the target value of the objective.
     * @param assumptions the assumptions considered in the model.
     */
    public DT_Model_Prescriptive(String modelName, String objective, double target, String assumptions) {
        super(modelName);
        this.objective = objective;
        this.target = target;
        this.assumptions = assumptions;
    }

    /**
     * Updates the model with a new datum: keeps the latest numeric value.
     *
     * @param updatedData the new datum.
     */
    @Override
    public void update(T updatedData) {
        double v = numericValue(updatedData);
        if (!Double.isNaN(v)) {
            lastValue = v;
        }
    }

    /**
     * Recommends the action to take to reach the objective, from the latest value (a simple placeholder
     * that subclasses can replace by a real decision model).
     *
     * @return the recommended action.
     */
    public String recommend() {
        if (Double.isNaN(lastValue)) {
            return "No data assimilated: no recommendation.";
        }
        double gap = target - lastValue;
        if (gap > 0) {
            return "Increase by " + gap + " to reach the target " + target + ".";
        }
        return gap < 0 ? "Decrease by " + (-gap) + " to reach the target " + target + "."
                       : "Target " + target + " reached: no action.";
    }

    /**
     * Executes the prescriptive model: prints its recommendation.
     */
    @Override
    public void executeModel() {
        System.out.println("Executing prescriptive model: " + modelName);
        System.out.println("  Objective: " + objective);
        System.out.println("  Assumptions: " + assumptions);
        System.out.println("  Recommendation: " + recommend());
    }

    /** @return the objective of the model. */
    public String getObjective() {
        return objective;
    }

    /** @return the target value of the objective. */
    public double getTarget() {
        return target;
    }

    /** @param target the new target value of the objective. */
    public void setTarget(double target) {
        this.target = target;
    }

    /** @return the assumptions of the model. */
    public String getAssumptions() {
        return assumptions;
    }

    /**
     * Provides a string representation of the prescriptive model, including its attributes.
     *
     * @return a string describing the prescriptive model.
     */
    @Override
    public String toString() {
        return "DT_Model_Prescriptive [modelName=" + modelName + ", objective=" + objective
                + ", target=" + target + ", lastValue=" + lastValue + "]";
    }
}
