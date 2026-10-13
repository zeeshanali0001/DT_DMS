package Models;

import java.util.ArrayList;
import java.util.List;

/**
 * Cubic B-Spline model of the sine example: a descriptive model ({@code m ∈ M_desc}) that represents the
 * measured sine wave by a smooth curve.
 * <p>
 * The assimilated values are the control points of a uniform cubic B-spline: the i-th value assimilated is
 * the control point of time {@code leftEndpoint + i × step}. The data must therefore be assimilated in
 * time order, one value per step.
 * </p>
 *
 * <p>The model is specified as the automaton {@code ℳ = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩}:
 * <ul>
 *   <li>{@code Λ} = {step, leftEndpoint}: the sampling assumptions.</li>
 *   <li>{@code X} = {data}: a measured sine value.</li>
 *   <li>{@code Y} = {updated}: the number of control points after an update.</li>
 *   <li>{@code Σ} = {count, last}: the number of control points and the last value assimilated.</li>
 *   <li>{@code Φ} = {WAIT ⟨NIL, NIL, ∞⟩, UPDATE ⟨NIL, NIL, 0⟩}.</li>
 *   <li>{@code Δ^in}: WAIT → UPDATE on {@code (data, d)}, Update: {@code count := count + 1; last := d}.</li>
 *   <li>{@code Δ^time}: UPDATE → WAIT, Send: {@code (updated, count)}.</li>
 *   <li>{@code Δ*} = ⟨WAIT, NIL, 0⟩.</li>
 * </ul>
 * </p>
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 2.0
 */
public class DT_Model_Descr_CubicBSpline extends DT_Model_Descriptive<Double> {

    /** Step size between two samples of the sine wave. */
    protected double step;

    /** Left endpoint of the domain: the time of the first sample (usually 0.0). */
    protected double leftEndpoint;

    /** The control points of the B-spline: the values assimilated, in time order. */
    protected final List<Double> controlPoints = new ArrayList<>();

    /**
     * Constructor for the Cubic B-Spline model.
     *
     * @param step         step size between samples.
     * @param leftEndpoint time of the first sample.
     */
    public DT_Model_Descr_CubicBSpline(double step, double leftEndpoint) {
        super("Cubic B-Spline", "Sine values");
        this.step = step;
        this.leftEndpoint = leftEndpoint;
        specifyAutomaton();
    }

    /** Specifies the 7-tuple {@code ℳ} of the model and runs its initialization {@code Δ*}. */
    private void specifyAutomaton() {
        addParameter("step", step);                                                   // Λ
        addParameter("leftEndpoint", leftEndpoint);
        addInput("data");                                                             // X
        addOutput("updated");                                                         // Y
        addStateVariable("count", 0);                                                 // Σ
        addStateVariable("last", Double.NaN);
        addPhase(new Phase("WAIT", null, null, Double.POSITIVE_INFINITY));            // Φ
        addPhase(new Phase("UPDATE", null, null, 0.0));
        addTransition(new Transition(TransitionKind.IN, "WAIT", "UPDATE",             // Δ^in
                java.util.Set.of("data"), null, null,
                (q, x) -> {
                    q.put("count", (Integer) q.get("count") + 1);
                    q.put("last", x.value);
                    return q;
                }));
        addTransition(new Transition(TransitionKind.TIME, "UPDATE", "WAIT",           // Δ^time
                null, null,
                q -> List.of(new Event("updated", q.get("count"))),
                null));
        setInitialization(new Initialization("WAIT", null, 0.0));                     // Δ*
        initialize();
    }

    /**
     * Updates the model with a new sine value: it becomes the next control point, and the automaton goes
     * WAIT → UPDATE → WAIT.
     *
     * @param updatedData the new sine value.
     */
    @Override
    public void update(Double updatedData) {
        super.update(updatedData);                      // descriptive statistics (count, min, max, mean)
        controlPoints.add(updatedData);
        externalTransition(new Event("data", updatedData), step);
        internalTransition();
    }

    /**
     * Returns the value of the model at position {@code x}: the uniform cubic B-spline of the control points.
     *
     * @param x the time.
     * @return the model value ({@code NaN} if no data has been assimilated).
     */
    public double getSineModelValue(double x) {
        if (controlPoints.isEmpty()) {
            return Double.NaN;
        }
        double s = (x - leftEndpoint) / step;
        int i = (int) Math.floor(s);
        double u = s - i;
        double u2 = u * u;
        double u3 = u2 * u;
        return ((1 - u) * (1 - u) * (1 - u) * point(i - 1)
                + (3 * u3 - 6 * u2 + 4) * point(i)
                + (-3 * u3 + 3 * u2 + 3 * u + 1) * point(i + 1)
                + u3 * point(i + 2)) / 6.0;
    }

    /** @return the control point of index {@code i}; the first and last points are repeated at the ends. */
    private double point(int i) {
        return controlPoints.get(Math.max(0, Math.min(controlPoints.size() - 1, i)));
    }

    /** @return the time of the last value assimilated ({@code NaN} if none). */
    public double getLastTime() {
        return controlPoints.isEmpty() ? Double.NaN : leftEndpoint + (controlPoints.size() - 1) * step;
    }

    /** @return the left endpoint of the domain. */
    public double getLeftEndpoint() {
        return leftEndpoint;
    }

    /** @return the step size between samples. */
    public double getStep() {
        return step;
    }
}
