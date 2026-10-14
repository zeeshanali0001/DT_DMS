
import Data.DT_DataSegment;
import Data.DT_Data_SineValues;
import Models.DT_Model_Descr_CubicBSpline;
import Mu.DT_Mu;
import Services.DT_Service_Diagnosis_Sine;
import Visualization.DT_ChartPanel;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main class of the Sine Wave Cubic B-Spline Digital Twin example: a minimal "Hello World" Digital Twin
 * {@code DT = ⟨D, M, S, μ⟩} built with the DT-DMS framework.
 * <ul>
 *   <li>{@code D}: the sine values measured on the ToI ({@link DT_Data_SineValues}).</li>
 *   <li>{@code M}: one descriptive model, the Cubic B-Spline ({@link DT_Model_Descr_CubicBSpline}).</li>
 *   <li>{@code S}: one diagnosis service ({@link DT_Service_Diagnosis_Sine}), coupled to the model.</li>
 *   <li>{@code μ}: the data assimilation function of the framework; it observes the data and updates the model.</li>
 * </ul>
 *
 * <p>Information flow for each sample: Data → μ → Model → Diagnosis, with live charting and anomaly
 * detection. The model and the service are both specified as automata (see their classes).</p>
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 2.0
 */
public class DT_DMS_SineCubicBP extends DT_DMS<Double> {

    /** Step size between two samples. */
    static final double STEP = 0.05;

    /** Freshness threshold {@code δ}: data measured during the last time unit is real-time. */
    static final double DELTA = 1.0;

    /** Largest residual considered normal by the diagnosis service. */
    static final double ANOMALY_THRESHOLD = 0.05;

    /** The sine data source ({@code D}). */
    DT_Data_SineValues sineData;

    /** The Cubic B-Spline model ({@code M}). */
    DT_Model_Descr_CubicBSpline splineModel;

    /** The diagnosis service ({@code S}). */
    DT_Service_Diagnosis_Sine diagnosis;

    /** The data assimilation function ({@code μ}); the same object as {@code getMu()}. */
    DT_Mu<Double> assimilation;

    /** The live chart ({@code null} if not shown). */
    DT_ChartPanel chart;

    /** The simulated current time {@code t_now}. */
    private double now = 0.0;

    /**
     * Builds the Digital Twin: creates the data, the model and the service and connects them.
     *
     * @param showChart {@code true} to show the live chart.
     */
    public DT_DMS_SineCubicBP(boolean showChart) {
        super("Sine", DELTA);
        setClock(() -> now);
        assimilation = mu;

        sineData = new DT_Data_SineValues("Sine values");
        sineData.getCharacterization().setUnit("dimensionless");
        sineData.getCharacterization().setSignalType("digital");
        sineData.getCharacterization().setDataFlowType("periodic");
        splineModel = new DT_Model_Descr_CubicBSpline(STEP, 0.0);
        diagnosis = new DT_Service_Diagnosis_Sine(ANOMALY_THRESHOLD);

        addData(sineData);                      // μ observes the data
        addService(diagnosis, splineModel);     // adds the model to M and couples the service to it

        if (showChart) {
            chart = new DT_ChartPanel();
        }
    }

    /**
     * Runs the simulation: measures {@code nbVal} sine values, one per step. For each value:
     * <ol>
     *   <li>the data source measures the ToI at {@code t_now};</li>
     *   <li>{@code μ} assimilates the real-time datum synchronously into the model;</li>
     *   <li>the diagnosis service is run on the datum, {@code s(m, ω)};</li>
     *   <li>the chart is updated.</li>
     * </ol>
     *
     * @param nbVal the number of values to measure.
     */
    public void execute(int nbVal) {
        System.out.println("Sine_DMS::execute() begin");
        System.out.println(this);

        for (int index = 0; index < nbVal; ++index) {
            now = Math.round(index * STEP * 1000.0) / 1000.0;       // t_now, rounded to 3 decimal places

            double sineValue = sineData.measure(now);                // D, then μ(ω, m) = m'
            Object report = runService(diagnosis, new DT_DataSegment<>(now, sineValue));   // s(m, ω)

            if (index % 10 == 0 || !"OK".equals(report)) {
                System.out.println("Data index " + index + " (t=" + now + "), sine value = " + sineValue
                        + ", model = " + splineModel.getSineModelValue(now) + " → " + report);
            }
            if (chart != null) {
                chart.updateChart(index, sineValue, splineModel.getSineModelValue(now));
            }
        }

        System.out.println("\nSine_DMS::execute() end");
        splineModel.executeModel();
        System.out.println("Anomalies detected at t = " + diagnosis.getAnomalyTimes());
        System.out.println("D_rt   (t ≥ t_now − δ): " + sineData.getRealTimeData(DELTA).size() + " values");
        System.out.println("D_hist (t < t_now − δ): " + sineData.getHistoricalData(DELTA).size() + " values");
        System.out.println("Model automaton:   phase " + splineModel.getCurrentPhase() + ", state " + splineModel.getState());
        System.out.println("Service automaton: phase " + diagnosis.getCurrentPhase() + ", state " + diagnosis.getState());

        // check of the interpolation between the samples, against the exact sine function
        System.out.println("\nRandom check of the interpolation (10 points between the samples):");
        double maxDeviation = diagnosis.performRandomCheck(10, 42L, true);
        System.out.println("Largest deviation |sin(x) − m(x)| = " + maxDeviation);

        // validity of the DT
        System.out.println();
        System.out.println("Consistent: " + checkConsistency(System.out));
    }

    /**
     * Writes the Data-Models-Services diagram of the Digital Twin ({@code dumpGraphviz(String)}) in the
     * {@code docs} folder if it exists, else in the current folder.
     */
    public void writeGraphviz() {
        String dotFile = Files.isDirectory(Path.of("docs")) ? "docs/DT_DMS_Sine.dot" : "DT_DMS_Sine.dot";
        try {
            dumpGraphviz(dotFile);
            System.out.println("Written " + dotFile + " (render it with: dot -Tpng " + dotFile + " -o DT_DMS_Sine.png)");
        } catch (IOException e) {
            System.out.println("Cannot write " + dotFile + ": " + e.getMessage());
        }
    }

    /**
     * Runs the example. Pass the argument {@code nochart} to run without the live chart.
     *
     * @param args optional: {@code nochart}.
     */
    public static void main(String[] args) {
        boolean showChart = !(args.length > 0 && args[0].equalsIgnoreCase("nochart"))
                && !GraphicsEnvironment.isHeadless();

        DT_DMS_SineCubicBP dtSystem = new DT_DMS_SineCubicBP(showChart);
        dtSystem.sineData.injectAnomaly(3.0, 0.5);      // a spike on the ToI, to be found by the diagnosis
        dtSystem.execute(100);
        dtSystem.writeGraphviz();
    }
}
