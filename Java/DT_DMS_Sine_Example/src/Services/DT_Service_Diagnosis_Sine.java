package Services;

import Models.DT_Model_Descr_CubicBSpline;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Diagnosis service of the sine example ({@code s ∈ S_diag}): it detects the anomalies of the measured
 * sine wave.
 * <p>
 * The service is coupled to the Cubic B-Spline model and consumes the latest measured value:
 * {@code s(m, ω)} compares the value {@code d} of {@code ω} with the value expected by the model at the
 * same time; the difference is the residual. An anomaly is reported when the residual exceeds the threshold.
 * </p>
 *
 * <p>The service is specified as the automaton {@code 𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩}:
 * <ul>
 *   <li>{@code Λ} = {threshold}: the largest residual considered normal.</li>
 *   <li>{@code X} = {data}: a measured sine value.</li>
 *   <li>{@code Y} = {anomaly}: the residual of an anomalous value.</li>
 *   <li>{@code Σ} = {residual}: the residual of the last value checked.</li>
 *   <li>{@code Φ} = {IDLE ⟨residual ≥ 0, NIL, ∞⟩, CHECK ⟨residual ≥ 0, NIL, 0⟩}.</li>
 *   <li>{@code Δ^in}: IDLE → CHECK on {@code (data, d)}, Update: {@code residual := |d − m(t)|}.</li>
 *   <li>{@code Δ^time}: CHECK → IDLE, Guard: {@code residual > threshold}, Send: {@code (anomaly, residual)}.</li>
 *   <li>{@code Δ^time}: CHECK → IDLE, Guard: {@code residual ≤ threshold}, no output.</li>
 *   <li>{@code Δ*} = ⟨IDLE, NIL, 0⟩.</li>
 * </ul>
 * </p>
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 2.0
 */
public class DT_Service_Diagnosis_Sine extends DT_Service_Diagnosis<Double> {

    /** Threshold for anomaly detection (residual between the measured value and the model). */
    protected double threshold;

    /** The times at which an anomaly was detected. */
    protected final List<Double> anomalyTimes = new ArrayList<>();

    /**
     * Constructor of the sine diagnosis service.
     *
     * @param threshold the largest residual considered normal.
     */
    public DT_Service_Diagnosis_Sine(double threshold) {
        super("Sine diagnosis", "Detects the anomalies of the measured sine wave", -1.0, 1.0);
        this.threshold = threshold;
        specifyAutomaton();
    }

    /** Specifies the 7-tuple {@code 𝒮} of the service and runs its initialization {@code Δ*}. */
    private void specifyAutomaton() {
        addParameter("threshold", threshold);                                         // Λ
        addInput("data");                                                             // X
        addOutput("anomaly");                                                         // Y
        addStateVariable("residual", 0.0);                                            // Σ
        addPhase(new Phase("IDLE", q -> (Double) q.get("residual") >= 0, null, Double.POSITIVE_INFINITY)); // Φ
        addPhase(new Phase("CHECK", q -> (Double) q.get("residual") >= 0, null, 0.0));
        addTransition(new Transition(TransitionKind.IN, "IDLE", "CHECK",              // Δ^in
                Set.of("data"), null, null,
                (q, x) -> {
                    q.put("residual", residualOf((Double) x.value));
                    return q;
                }));
        addTransition(new Transition(TransitionKind.TIME, "CHECK", "IDLE",            // Δ^time (anomaly)
                null,
                q -> (Double) q.get("residual") > threshold,
                q -> List.of(new Event("anomaly", q.get("residual"))),
                null));
        addTransition(new Transition(TransitionKind.TIME, "CHECK", "IDLE",            // Δ^time (normal)
                null,
                q -> (Double) q.get("residual") <= threshold,
                null, null));
        setInitialization(new Initialization("IDLE", null, 0.0));                     // Δ*
        initialize();
    }

    /**
     * Computes the residual of a measured value: its distance to the value expected by the coupled
     * Cubic B-Spline model at the time of the last value assimilated.
     *
     * @param d the measured value.
     * @return the residual {@code |d − m(t)|} (0 if the service is not coupled to a Cubic B-Spline model).
     */
    protected double residualOf(double d) {
        if (model instanceof DT_Model_Descr_CubicBSpline spline) {
            return Math.abs(d - spline.getSineModelValue(spline.getLastTime()));
        }
        return 0.0;
    }

    /**
     * Executes the diagnosis on the latest value consumed: the automaton goes IDLE → CHECK → IDLE and an
     * anomaly is reported if it emits the output {@code (anomaly, residual)}.
     *
     * @return the diagnosis report.
     */
    @Override
    public Object executeService() {
        if (consumedData.isEmpty() || !(model instanceof DT_Model_Descr_CubicBSpline spline)) {
            diagnosisReport = "Diagnosis failed: no data or no Cubic B-Spline model.";
            return diagnosisReport;
        }
        double d = consumedData.get(consumedData.size() - 1);
        externalTransition(new Event("data", d), spline.getStep());
        List<Event> outputs = internalTransition();
        if (outputs.isEmpty()) {
            diagnosisReport = "OK";
        } else {
            double t = Math.round(spline.getLastTime() * 1000.0) / 1000.0;
            anomalyTimes.add(t);
            diagnosisReport = "ANOMALY DETECTED at t=" + t + ": actual=" + d
                    + ", expected=" + spline.getSineModelValue(spline.getLastTime())
                    + ", residual=" + outputs.get(0).value;
        }
        return diagnosisReport;
    }

    /** @return the threshold for anomaly detection. */
    public double getThreshold() {
        return threshold;
    }

    /** @return the times at which an anomaly was detected. */
    public List<Double> getAnomalyTimes() {
        return anomalyTimes;
    }
}
