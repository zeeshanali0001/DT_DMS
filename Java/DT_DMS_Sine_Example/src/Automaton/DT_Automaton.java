package Automaton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Abstract base class representing the class of automata {@code 𝒜} of the DT-DMS framework.
 * <p>
 * DT models and DT services belong to the same class of automata, i.e., {@code M ⊆ 𝒜} and
 * {@code S ⊆ 𝒜}. Therefore both {@code DT_Model} and {@code DT_Service} inherit from this class.
 * Each automaton, whether discrete, continuous or hybrid, is specified with HiLLS (based on DEVS) as the
 * 7-tuple (written {@code ℳ} for a model and {@code 𝒮} for a service):
 * </p>
 * <pre>
 *   ℳ = ⟨ Λ, X, Y, Σ, Φ, Δ, Δ* ⟩        𝒮 = ⟨ Λ, X, Y, Σ, Φ, Δ, Δ* ⟩
 * </pre>
 *
 * <p>Mapping of the 7-tuple to this class:
 * <ul>
 *   <li>{@code Λ}: {@link #parameters}, the parameter set (context assumptions).</li>
 *   <li>{@code X}: {@link #inputs}, the input set (what is received from the ToI).</li>
 *   <li>{@code Y}: {@link #outputs}, the output set (what is provided to, or influences, the ToI).</li>
 *   <li>{@code Σ}: {@link #state}, the state variables; its current content is the state {@code q ∈ Q}.</li>
 *   <li>{@code Φ}: {@link #phases}, the phase set, each phase being {@link Phase}.</li>
 *   <li>{@code Δ}: {@link #transitions}, the phase-to-phase transition set, each being {@link Transition}.</li>
 *   <li>{@code Δ*}: {@link #initialization}, the initialization {@link Initialization}.</li>
 * </ul>
 * </p>
 *
 * <p>
 * A state {@code q ∈ Q} is represented as a {@code Map<String, Object>} that assigns a value to every
 * state variable {@code σ ∈ Σ}. The domain {@code dom(σ)} of a variable is the Java type of its value.
 * {@code NIL} components of the formalism are represented by {@code null}.
 * </p>
 */
public abstract class DT_Automaton {

    /**
     * An event {@code (port, v)}: an input event of {@code X_V} or an output event of {@code Y_V},
     * where {@code port ∈ X} (resp. {@code Y}) and {@code v ∈ dom(port)}.
     */
    public static class Event {
        /** The input or output port name ({@code x ∈ X} or {@code y ∈ Y}). */
        public final String port;
        /** The value carried by the event ({@code v ∈ dom(port)}). */
        public final Object value;

        /**
         * @param port  the port name.
         * @param value the value carried by the event.
         */
        public Event(String port, Object value) {
            this.port = port;
            this.value = value;
        }

        @Override
        public String toString() {
            return "(" + port + ", " + value + ")";
        }
    }

    /**
     * A phase {@code Φ_φ = ⟨Cond_φ, Action_φ, Delay_φ⟩}: a stable discrete or continuous step of the automaton.
     */
    public static class Phase {
        /** The phase name {@code φ}. */
        public final String name;
        /** {@code Cond_φ}: the predicate on {@code Q} the state satisfies while in the phase ({@code null} = NIL). */
        public final Predicate<Map<String, Object>> cond;
        /** {@code Action_φ : Q → Q}: what is computed during the phase ({@code null} = NIL). */
        public final UnaryOperator<Map<String, Object>> action;
        /** {@code Delay_φ}: lifetime of the phase; {@code Double.POSITIVE_INFINITY} for a passive phase. */
        public final double delay;

        /**
         * @param name   the phase name.
         * @param cond   the phase condition, or {@code null} (NIL).
         * @param action the phase action, or {@code null} (NIL).
         * @param delay  the phase lifetime ({@code ≥ 0}, or {@code Double.POSITIVE_INFINITY}).
         */
        public Phase(String name, Predicate<Map<String, Object>> cond,
                     UnaryOperator<Map<String, Object>> action, double delay) {
            if (delay < 0) {
                throw new IllegalArgumentException("Delay of phase " + name + " must be >= 0.");
            }
            this.name = name;
            this.cond = cond;
            this.action = action;
            this.delay = delay;
        }
    }

    /** The three kinds of phase-to-phase transitions of {@code Δ}. */
    public enum TransitionKind {
        /** External transition {@code Δ^in = ⟨In, Guard, Update⟩}, triggered when a specific input arrives. */
        IN,
        /** Internal transition {@code Δ^time = ⟨Guard, Send, Update⟩}, triggered when {@code Delay_i} has elapsed. */
        TIME,
        /** Confluent transition {@code Δ^{in,time} = ⟨In, Guard, Send, Update⟩}, input arrives exactly as {@code Delay_i} elapses. */
        IN_TIME
    }

    /**
     * A phase-to-phase transition {@code Δ_{i → j}} of kind {@link TransitionKind}.
     * Components not used by a kind are {@code null} (e.g., {@code in} for {@code TIME}, {@code send} for {@code IN}).
     */
    public static class Transition {
        /** The kind of the transition. */
        public final TransitionKind kind;
        /** The source phase {@code i}. */
        public final String from;
        /** The target phase {@code j}. */
        public final String to;
        /** {@code In ⊆ X_V}: here given by the input ports whose events (with any value) trigger the transition. */
        public final Set<String> in;
        /** {@code Guard ∈ ℘(Q)}: the condition the state must also satisfy ({@code null} = NIL, always true). */
        public final Predicate<Map<String, Object>> guard;
        /**
         * {@code Send ∈ ℘(Y_V)}: the output events emitted. It is given as a function of the state so that output
         * values can be read from the state variables; a constant function gives a fixed set ({@code null} = NIL).
         */
        public final Function<Map<String, Object>, List<Event>> send;
        /**
         * {@code Update}: how the state changes. For {@code IN}/{@code IN_TIME} it is {@code Q × X_V → Q};
         * for {@code TIME} it is {@code Q → Q} and the event argument is {@code null} ({@code null} = NIL).
         */
        public final BiFunction<Map<String, Object>, Event, Map<String, Object>> update;

        /**
         * @param kind   the transition kind.
         * @param from   the source phase {@code i}.
         * @param to     the target phase {@code j}.
         * @param in     the triggering input ports (for {@code IN}/{@code IN_TIME}), else {@code null}.
         * @param guard  the guard, or {@code null}.
         * @param send   the output events (for {@code TIME}/{@code IN_TIME}), else {@code null}.
         * @param update the state update, or {@code null}.
         */
        public Transition(TransitionKind kind, String from, String to, Set<String> in,
                          Predicate<Map<String, Object>> guard,
                          Function<Map<String, Object>, List<Event>> send,
                          BiFunction<Map<String, Object>, Event, Map<String, Object>> update) {
            this.kind = kind;
            this.from = from;
            this.to = to;
            this.in = in;
            this.guard = guard;
            this.send = send;
            this.update = update;
        }
    }

    /**
     * The initialization {@code Δ* = ⟨φ_0, Update_0, τ_0⟩}.
     */
    public static class Initialization {
        /** {@code φ_0 ∈ Φ}: the starting phase. */
        public final String phase0;
        /** {@code Update_0 : Q → Q}: the state-setting action run once at startup ({@code null} = NIL). */
        public final UnaryOperator<Map<String, Object>> update0;
        /** {@code τ_0 ∈ [0, Delay_{φ_0}]}: the time already elapsed in {@code φ_0} at the start (usually 0). */
        public final double tau0;

        /**
         * @param phase0  the starting phase.
         * @param update0 the startup state setting, or {@code null}.
         * @param tau0    the initial elapsed time.
         */
        public Initialization(String phase0, UnaryOperator<Map<String, Object>> update0, double tau0) {
            this.phase0 = phase0;
            this.update0 = update0;
            this.tau0 = tau0;
        }
    }

    /** {@code Λ}: the parameter set, each parameter {@code λ} with its chosen value in {@code dom(λ)}. */
    protected final Map<String, Object> parameters = new LinkedHashMap<>();

    /** {@code X}: the input set (input port names). */
    protected final Set<String> inputs = new LinkedHashSet<>();

    /** {@code Y}: the output set (output port names). */
    protected final Set<String> outputs = new LinkedHashSet<>();

    /** {@code Σ}: the state variables with their current values, i.e., the current state {@code q ∈ Q}. */
    protected final Map<String, Object> state = new LinkedHashMap<>();

    /** {@code Φ}: the phase set, indexed by phase name. */
    protected final Map<String, Phase> phases = new LinkedHashMap<>();

    /** {@code Δ}: the phase-to-phase transition set. */
    protected final List<Transition> transitions = new ArrayList<>();

    /** {@code Δ*}: the initialization. */
    protected Initialization initialization;

    /** The current phase {@code φ ∈ Φ}. */
    protected String currentPhase;

    /** The time elapsed in the current phase. */
    protected double elapsed;

    // ---------------------------------------------------------------- specification (building the 7-tuple)

    /** Adds a parameter {@code λ ∈ Λ} with its value. */
    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }

    /** Adds an input port {@code x ∈ X}. */
    public void addInput(String port) {
        inputs.add(port);
    }

    /** Adds an output port {@code y ∈ Y}. */
    public void addOutput(String port) {
        outputs.add(port);
    }

    /** Adds a state variable {@code σ ∈ Σ} with its initial value. */
    public void addStateVariable(String name, Object initialValue) {
        state.put(name, initialValue);
    }

    /** Adds a phase {@code φ ∈ Φ}. */
    public void addPhase(Phase phase) {
        phases.put(phase.name, phase);
    }

    /** Adds a transition to {@code Δ}; its source and target phases must already be in {@code Φ}. */
    public void addTransition(Transition transition) {
        if (!phases.containsKey(transition.from) || !phases.containsKey(transition.to)) {
            throw new IllegalArgumentException("Transition " + transition.from + " -> " + transition.to
                    + " uses a phase that is not in Φ.");
        }
        transitions.add(transition);
    }

    /** Sets the initialization {@code Δ*}. */
    public void setInitialization(Initialization initialization) {
        this.initialization = initialization;
    }

    // ---------------------------------------------------------------- behaviour (DEVS-like execution)

    /**
     * Runs the initialization {@code Δ*}: enters {@code φ_0}, applies {@code Update_0} and sets the elapsed
     * time to {@code τ_0}.
     */
    public void initialize() {
        if (initialization == null || !phases.containsKey(initialization.phase0)) {
            throw new IllegalStateException("Δ* is missing or φ_0 is not in Φ.");
        }
        Phase phase0 = phases.get(initialization.phase0);
        if (initialization.tau0 < 0 || initialization.tau0 > phase0.delay) {
            throw new IllegalStateException("τ_0 must be in [0, Delay_φ0].");
        }
        currentPhase = phase0.name;
        applyToState(initialization.update0);
        elapsed = initialization.tau0;
    }

    /**
     * Returns the time remaining before an internal transition must occur in the current phase:
     * {@code Delay_φ − elapsed} ({@code Double.POSITIVE_INFINITY} for a passive phase).
     */
    public double timeAdvance() {
        return phases.get(currentPhase).delay - elapsed;
    }

    /**
     * External transition {@code Δ^in}: an input event {@code x} arrives after {@code e} time units in the
     * current phase. The first matching transition (source phase, {@code In}, {@code Guard}) is fired.
     *
     * @param x the input event.
     * @param e the time elapsed since the last transition.
     * @return {@code true} if a transition fired, {@code false} if the input was ignored.
     */
    public boolean externalTransition(Event x, double e) {
        checkInput(x);
        elapsed += e;
        Transition t = find(TransitionKind.IN, x);
        if (t == null) {
            return false;
        }
        fire(t, x);
        return true;
    }

    /**
     * Internal transition {@code Δ^time}: fired when {@code Delay_i} of the current phase has elapsed.
     *
     * @return the output events emitted ({@code Send}); empty if no transition matched.
     */
    public List<Event> internalTransition() {
        Transition t = find(TransitionKind.TIME, null);
        if (t == null) {
            return Collections.emptyList();
        }
        return fire(t, null);
    }

    /**
     * Confluent transition {@code Δ^{in,time}}: an input event {@code x} arrives exactly as {@code Delay_i} elapses.
     *
     * @param x the input event.
     * @return the output events emitted ({@code Send}); empty if no transition matched.
     */
    public List<Event> confluentTransition(Event x) {
        checkInput(x);
        Transition t = find(TransitionKind.IN_TIME, x);
        if (t == null) {
            return Collections.emptyList();
        }
        return fire(t, x);
    }

    /** @return {@code true} if the current state satisfies {@code Cond_φ} of the current phase (NIL = true). */
    public boolean conditionHolds() {
        Phase phase = phases.get(currentPhase);
        return phase.cond == null || phase.cond.test(state);
    }

    /** @return the current phase {@code φ}. */
    public String getCurrentPhase() {
        return currentPhase;
    }

    /** @return a read-only view of the current state {@code q}. */
    public Map<String, Object> getState() {
        return Collections.unmodifiableMap(state);
    }

    /** @return a read-only view of the parameters {@code Λ}. */
    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    // ---------------------------------------------------------------- helpers

    /** Finds the first transition of the given kind from the current phase that accepts {@code x} and whose guard holds. */
    private Transition find(TransitionKind kind, Event x) {
        for (Transition t : transitions) {
            boolean accepts = x == null || (t.in != null && t.in.contains(x.port));
            if (t.kind == kind && t.from.equals(currentPhase) && accepts
                    && (t.guard == null || t.guard.test(state))) {
                return t;
            }
        }
        return null;
    }

    /** Fires {@code t}: computes {@code Send}, applies {@code Update}, enters phase {@code j} and runs its {@code Action}. */
    private List<Event> fire(Transition t, Event x) {
        List<Event> sent = t.send == null ? Collections.emptyList() : t.send.apply(getState());
        for (Event y : sent) {
            if (!outputs.contains(y.port)) {
                throw new IllegalStateException("Output port " + y.port + " is not in Y.");
            }
        }
        applyToState(t.update == null ? null : q -> t.update.apply(q, x));
        currentPhase = t.to;
        elapsed = 0;
        // Action_φ of the new phase (for a discrete phase it is applied on entry; a continuous flow
        // q' = f_φ(q) has to be integrated by the subclass).
        Phase phase = phases.get(currentPhase);
        applyToState(phase.action);
        return sent;
    }

    /** Replaces the state with {@code f(q)}, working on a copy so that {@code f} may return a new or the same map. */
    private void applyToState(Function<Map<String, Object>, Map<String, Object>> f) {
        if (f == null) {
            return;
        }
        Map<String, Object> next = new LinkedHashMap<>(f.apply(new LinkedHashMap<>(state)));
        state.clear();
        state.putAll(next);
    }

    /** Checks that {@code x.port ∈ X}. */
    private void checkInput(Event x) {
        if (!inputs.contains(x.port)) {
            throw new IllegalArgumentException("Input port " + x.port + " is not in X.");
        }
    }
}
