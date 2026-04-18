package Services;

/**
 * Class representing a Control Service in the Digital Twin framework.
 * A Control Service is responsible for managing, regulating, or controlling
 * operations within a system based on feedback or predefined logic.
 * This class extends the base Service class and adds control-specific attributes
 * and behaviors.
 */
public class DT_Services_Control<T> extends DT_Services<T> {

    @Override
    public void update(T data) {
        System.out.println("   → CONTROL SERVICE: Received new data for control: " + data);
        // Here you can add logic to process the new data and update the control action result accordingly.
    }


    /**
     * The control strategy applied by the service.
     * Examples include "PID Control", "Feedforward Control", or "Rule-Based Control".
     */
    protected String controlStrategy;

    /**
     * Input data used for the control process.
     * This data acts as feedback or input for the control algorithm.
     */
    protected String[] controlInputs;

    /**
     * The result of the control action.
     * This output is generated after executing the control logic.
     */
    protected String controlActionResult;

    /**
     * Constructor to initialize the Control Service with necessary attributes.
     *
     * @param serviceName        the name of the service
     * @param serviceType        the type of the service (e.g., "Control")
     * @param serviceDescription a brief description of the service
     * @param controlStrategy    the control strategy applied by the service
     * @param controlInputs      the input data used for the control process
     */
    public DT_Services_Control(String serviceName, String serviceType, String serviceDescription,
                          String controlStrategy, String[] controlInputs) {
        super(serviceName, serviceType, serviceDescription);
        this.controlStrategy = controlStrategy;
        this.controlInputs = controlInputs;
        this.controlActionResult = "No control action performed yet."; // Default initial result
    }

    /**
     * Gets the control strategy applied by the service.
     *
     * @return the name of the control strategy
     */
    public String getControlStrategy() {
        return controlStrategy;
    }

    /**
     * Sets the control strategy applied by the service.
     *
     * @param controlStrategy the new control strategy
     */
    public void setControlStrategy(String controlStrategy) {
        this.controlStrategy = controlStrategy;
    }

    /**
     * Gets the input data used for the control process.
     *
     * @return the control inputs array
     */
    public String[] getControlInputs() {
        return controlInputs;
    }

    /**
     * Sets the input data used for the control process.
     *
     * @param controlInputs the new control inputs array
     */
    public void setControlInputs(String[] controlInputs) {
        this.controlInputs = controlInputs;
    }

    /**
     * Gets the result of the control action.
     *
     * @return the control action result
     */
    public String getControlActionResult() {
        return controlActionResult;
    }

    /**
     * Sets the result of the control action.
     *
     * @param controlActionResult the new control action result
     */
    public void setControlActionResult(String controlActionResult) {
        this.controlActionResult = controlActionResult;
    }

    /**
     * Executes the control logic.
     * This method applies the specified control strategy using the provided input data
     * to generate a control action result.
     */
    @Override
    public void executeService() {
        if (controlInputs == null || controlInputs.length == 0) {
            System.out.println("No control inputs provided. Unable to perform control action.");
            controlActionResult = "Control action failed due to lack of inputs.";
            return;
        }

        // Simplified logic for demonstration purposes.
        // In real scenarios, this could involve advanced control algorithms.
        StringBuilder resultBuilder = new StringBuilder("Control Action Result:\n");
        resultBuilder.append("Strategy: ").append(controlStrategy).append("\n");

        // Simulated control process
        for (String input : controlInputs) {
            resultBuilder.append("Processed input: ").append(input).append("\n");
        }

        controlActionResult = resultBuilder.toString();
        System.out.println("Control action executed successfully.");
    }

    /**
     * Provides a string representation of the Control Service.
     *
     * @return a string containing the details of the control service
     */
    @Override
    public String toString() {
        return super.toString() +
                ", ControlStrategy=" + controlStrategy +
                ", ControlActionResult=" + controlActionResult +
                ", ControlInputs=" + java.util.Arrays.toString(controlInputs) + "]";
    }

    /**
     * Checks whether this ControlService is equal to another object.
     *
     * @param obj the object to compare with
     * @return true if the control services are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;

        DT_Services_Control<?> other = (DT_Services_Control<?> ) obj;
        return controlStrategy.equals(other.controlStrategy) &&
                controlActionResult.equals(other.controlActionResult) &&
                java.util.Arrays.equals(controlInputs, other.controlInputs);
    }


    /**
     * Generates a hash code for the Control Service.
     *
     * @return the hash code of the control service
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + controlStrategy.hashCode();
        result = 31 * result + controlActionResult.hashCode();
        result = 31 * result + java.util.Arrays.hashCode(controlInputs);
        return result;
    }



}

