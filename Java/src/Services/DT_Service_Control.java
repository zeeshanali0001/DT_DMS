package Services;

import Models.DT_Model_Prescriptive;

/**
 * Class representing a Control Service ({@code s ∈ S_cont}) of the Digital Twin.
 * <p>
 * A Control Service acts back on the ToI (through actuators or other systems) based on feedback: it
 * turns the latest consumed value and the target of the coupled prescriptive model into a command.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_Service_Control<T> extends DT_Service<T> {

    /** The control strategy applied (e.g., "Proportional", "PID Control", "Rule-Based Control"). */
    protected String controlStrategy;

    /** The gain applied to the error between the target and the measured value. */
    protected double gain;

    /** The result of the last control action. */
    protected String controlActionResult = "No control action performed yet.";

    /**
     * Constructor to initialize the Control Service with necessary attributes.
     *
     * @param serviceName        the name of the service.
     * @param serviceDescription a brief description of the service.
     * @param controlStrategy    the control strategy applied.
     * @param gain               the gain applied to the error.
     */
    public DT_Service_Control(String serviceName, String serviceDescription, String controlStrategy, double gain) {
        super(serviceName, serviceDescription);
        this.controlStrategy = controlStrategy;
        this.gain = gain;
    }

    /**
     * Executes the control logic: command = gain × (target − latest consumed value).
     *
     * @return the control action result.
     */
    @Override
    public Object executeService() {
        if (!(model instanceof DT_Model_Prescriptive<T> prescriptive)) {
            controlActionResult = "Control action failed: no prescriptive model is coupled to the service.";
        } else if (consumedData.isEmpty()) {
            controlActionResult = "Control action failed due to lack of inputs.";
        } else {
            double measured = numericValue(consumedData.get(consumedData.size() - 1));
            double command = gain * (prescriptive.getTarget() - measured);
            controlActionResult = "Control action (" + controlStrategy + "): command = " + command
                    + " for measured value " + measured + " and target " + prescriptive.getTarget() + ".";
        }
        return controlActionResult;
    }

    /** @return the control strategy. */
    public String getControlStrategy() {
        return controlStrategy;
    }

    /** @return the result of the last control action. */
    public String getControlActionResult() {
        return controlActionResult;
    }
}
