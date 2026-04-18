import Data.DT_Data_SineValues;
import Models.DT_Models_Descr_CubicBSpline;
import Services.DT_Services_Diagnosis_Sine;

/**
 * Main orchestrator class for the Sine Wave Cubic B-Spline Digital Twin example.
 * This class sets up the entire Digital Twin system:
 * <ul>
 *   <li>Creates the Data source (Physical Twin)</li>
 *   <li>Creates the Model (Digital Model)</li>
 *   <li>Creates the Diagnosis Service</li>
 *   <li>Links them using the Observer Pattern</li>
 *   <li>Runs the simulation by pushing sine values</li>
 * </ul>
 *
 * <p>This is a minimal "Hello World" Digital Twin that demonstrates:
 * Data → Model → Diagnosis with live charting and anomaly detection.</p>
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 1.0
 */
public class DT_DMS_SineCubicBP extends DT_DMS {

    /**
     * Default constructor.
     */
    public DT_DMS_SineCubicBP() {
        super();
    }

    /**
     * Orchestrates the complete setup and execution of the Digital Twin system.
     * 
     * <p>Execution flow:</p>
     * <ol>
     *   <li>Create Model (Cubic B-Spline)</li>
     *   <li>Create Diagnosis Service</li>
     *   <li>Create Data source (Sine Values)</li>
     *   <li>Register Model and Service as observers to the Data (Observer Pattern)</li>
     *   <li>Link Service with Data for chart updates and diagnosis</li>
     *   <li>Simulate incoming data by calling push_back() in a loop</li>
     * </ol>
     */
    public void execute() {
        System.out.println("Sine_DMS::execute() begin");

        // Step 1: Create the Digital Model (Cubic B-Spline)
        double step = 0.05;
        DT_Models_Descr_CubicBSpline cBSModel = new DT_Models_Descr_CubicBSpline(step);
        this.addModel(cBSModel);

        // Step 2: Create the Diagnosis Service
        DT_Services_Diagnosis_Sine svService = new DT_Services_Diagnosis_Sine(cBSModel);
        this.addService(svService);

        // Step 3: Create the Data Source (Physical Twin)
        DT_Data_SineValues svData = new DT_Data_SineValues("Sine values");
        this.addData(svData);

        // Step 4: Link using Observer Pattern
        // Both Model and Service will be automatically notified when data changes
        svData.registerObserver(cBSModel);
        svData.registerObserver(svService);

        // Step 5: Link the Service with the Data source
        // This is REQUIRED for chart updates and accessing raw values in diagnosis
        svService.setLatestData(svData);

        // Step 6: Connect model and service directly (kept from your original design)
        cBSModel.addService2BeUpdated(svService);

        System.out.println("Starting simulation - pushing 100 sine values...\n");

        int nbVal = 100;
        for (int index = 0; index < nbVal; ++index) {
            double xraw = index * step;
            double x = Math.round(xraw * 1000.0) / 1000.0;   // round to 3 decimal places
            double sineValue = Math.sin(x);

            // Push new value → This triggers the Observer Pattern every 10 values
            svData.push_back(sineValue);

            System.out.println("Data index " + index + " (x=" + x + "), sine value = " + sineValue);
        }

        System.out.println("\nSine_DMS::execute() end");
        System.out.println("Digital Twin simulation completed successfully.");
    }

    // ==================== Commented fields for documentation ====================
    // These are kept for Doxygen reference (you can remove them if not needed)

    /** Reference to the Cubic B-Spline model */
    DT_Models_Descr_CubicBSpline cBSModel = null;

    /** Reference to the sine data source */
    DT_Data_SineValues cBS = null;

    /** Reference to the diagnosis service */
    DT_Services_Diagnosis_Sine sineDiagnosisService = null;
    //        
}