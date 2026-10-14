# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Automaton.py
@brief The class of automata `𝒜` of the DT-DMS framework.
"""

import math
from abc import ABCMeta
from decimal import Decimal
from typing import Any, Callable, Dict, List, Optional, Set

## A state `q ∈ Q`: assigns a value to every state variable `σ ∈ Σ`.
State = Dict[str, Any]


def format_double(value: float) -> str:
    """!
    @brief Converts a number to text as Java does: the shortest text that reads back the same value, with at
    least one decimal (`35.0`, `0.05`, `1.0E-4`), `NaN`, `Infinity` or `-Infinity`.
    @param value the number.
    @return the text.
    """
    value = float(value)
    if math.isnan(value):
        return "NaN"
    if math.isinf(value):
        return "Infinity" if value > 0 else "-Infinity"
    magnitude = abs(value)
    if magnitude != 0.0 and (magnitude < 1e-3 or magnitude >= 1e7):
        # scientific notation with the shortest digits: 4.4857977259977436E-4
        sign, digits, exponent = Decimal(repr(value)).as_tuple()
        text = "".join(str(d) for d in digits).rstrip("0") or "0"
        power = exponent + len(digits) - 1
        mantissa = text[0] + "." + (text[1:] or "0")
        return ("-" if sign else "") + mantissa + "E" + str(power)
    text = repr(value)
    if "e" in text:  # e.g. 1e+16 is not reached here, but keep a plain form
        text = format(Decimal(text), "f")
    return text if "." in text else text + ".0"


def value_to_string(value: Any) -> str:
    """!
    @brief Converts a value to text as Java does (`format_double` for numbers, `true`/`false`, `null`).
    @param value the value.
    @return the text.
    """
    if value is None:
        return "null"
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, float):
        return format_double(value)
    return str(value)


def map_to_string(values: Dict[str, Any]) -> str:
    """!
    @brief Converts a state, a parameter set or any map of values to text: `{name=value, ...}`.
    @param values the map.
    @return the text.
    """
    return "{" + ", ".join(k + "=" + value_to_string(v) for k, v in values.items()) + "}"


def numeric_value(d: Any) -> float:
    """!
    @brief Converts a datum to a number, when it is one.
    @param d the datum.
    @return its numeric value, or `NaN` if the datum is not a number.
    """
    if isinstance(d, bool) or not isinstance(d, (int, float)):
        return math.nan
    return float(d)


class Event:
    """!
    @brief An event `(port, v)`: an input event of `X_V` or an output event of `Y_V`, where `port ∈ X`
    (resp. `Y`) and `v ∈ dom(port)`.
    """

    def __init__(self, port: str, value: Any):
        """!
        @param port  the port name.
        @param value the value carried by the event.
        """
        ## The input or output port name (`x ∈ X` or `y ∈ Y`).
        self.port: str = port
        ## The value carried by the event (`v ∈ dom(port)`).
        self.value: Any = value

    def __repr__(self) -> str:
        return "(" + self.port + ", " + value_to_string(self.value) + ")"


class Phase:
    """!
    @brief A phase `Φ_φ = ⟨Cond_φ, Action_φ, Delay_φ⟩`: a stable discrete or continuous step of the automaton.
    """

    def __init__(self, name: str, cond: Optional[Callable[[State], bool]],
                 action: Optional[Callable[[State], State]], delay: float):
        """!
        @param name   the phase name.
        @param cond   the phase condition, or `None` (NIL).
        @param action the phase action, or `None` (NIL).
        @param delay  the phase lifetime (`≥ 0`, or `math.inf`).
        @exception ValueError if the delay is negative.
        """
        if delay < 0:
            raise ValueError("Delay of phase " + name + " must be >= 0.")
        ## The phase name `φ`.
        self.name: str = name
        ## `Cond_φ`: the predicate on `Q` the state satisfies while in the phase (`None` = NIL).
        self.cond = cond
        ## `Action_φ : Q → Q`: what is computed during the phase (`None` = NIL).
        self.action = action
        ## `Delay_φ`: lifetime of the phase; `math.inf` for a passive phase.
        self.delay: float = delay


class TransitionKind:
    """!
    @brief The three kinds of phase-to-phase transitions of `Δ`.
    """
    ## External transition `Δ^in = ⟨In, Guard, Update⟩`, triggered when a specific input arrives.
    IN = "IN"
    ## Internal transition `Δ^time = ⟨Guard, Send, Update⟩`, triggered when `Delay_i` has elapsed.
    TIME = "TIME"
    ## Confluent transition `Δ^{in,time} = ⟨In, Guard, Send, Update⟩`, input arrives exactly as `Delay_i` elapses.
    IN_TIME = "IN_TIME"


class Transition:
    """!
    @brief A phase-to-phase transition `Δ_{i → j}` of kind TransitionKind.
    Components not used by a kind are `None` (e.g., `in_ports` for `TIME`, `send` for `IN`).
    """

    def __init__(self, kind: str, from_phase: str, to_phase: str, in_ports: Optional[Set[str]],
                 guard: Optional[Callable[[State], bool]],
                 send: Optional[Callable[[State], List[Event]]],
                 update: Optional[Callable[[State, Optional[Event]], State]]):
        """!
        @param kind       the transition kind.
        @param from_phase the source phase `i`.
        @param to_phase   the target phase `j`.
        @param in_ports   the triggering input ports (for `IN`/`IN_TIME`), else `None`.
        @param guard      the guard, or `None`.
        @param send       the output events (for `TIME`/`IN_TIME`), or `None`.
        @param update     the state update `Q × X_V → Q` (the event is `None` for `TIME`), or `None`.
        """
        ## The kind of the transition.
        self.kind: str = kind
        ## The source phase `i`.
        self.from_phase: str = from_phase
        ## The target phase `j`.
        self.to_phase: str = to_phase
        ## `In ⊆ X_V`: here given by the input ports whose events (with any value) trigger the transition.
        self.in_ports = in_ports
        ## `Guard ∈ ℘(Q)`: the condition the state must also satisfy (`None` = NIL, always true).
        self.guard = guard
        ## `Send ∈ ℘(Y_V)`: the output events emitted, as a function of the state (`None` = NIL).
        self.send = send
        ## `Update`: how the state changes (`None` = NIL).
        self.update = update


class Initialization:
    """!
    @brief The initialization `Δ* = ⟨φ_0, Update_0, τ_0⟩`.
    """

    def __init__(self, phase0: str, update0: Optional[Callable[[State], State]], tau0: float):
        """!
        @param phase0  the starting phase.
        @param update0 the startup state setting, or `None`.
        @param tau0    the initial elapsed time.
        """
        ## `φ_0 ∈ Φ`: the starting phase.
        self.phase0: str = phase0
        ## `Update_0 : Q → Q`: the state-setting action run once at startup (`None` = NIL).
        self.update0 = update0
        ## `τ_0 ∈ [0, Delay_{φ_0}]`: the time already elapsed in `φ_0` at the start (usually 0).
        self.tau0: float = tau0


class DT_Automaton(metaclass=ABCMeta):
    """!
    @brief Abstract base class representing the class of automata `𝒜` of the DT-DMS framework.

    DT models and DT services belong to the same class of automata, i.e., `M ⊆ 𝒜` and `S ⊆ 𝒜`. Therefore
    both `DT_Model` and `DT_Service` inherit from this class. Each automaton, whether discrete, continuous or
    hybrid, is specified with HiLLS (based on DEVS) as the 7-tuple (written `ℳ` for a model and `𝒮` for a
    service):

        ℳ = ⟨ Λ, X, Y, Σ, Φ, Δ, Δ* ⟩        𝒮 = ⟨ Λ, X, Y, Σ, Φ, Δ, Δ* ⟩

    Mapping of the 7-tuple to this class:
    - `Λ`: #parameters, the parameter set (context assumptions).
    - `X`: #inputs, the input set (what is received from the ToI).
    - `Y`: #outputs, the output set (what is provided to, or influences, the ToI).
    - `Σ`: #state, the state variables; its current content is the state `q ∈ Q`.
    - `Φ`: #phases, the phase set, each phase being a Phase.
    - `Δ`: #transitions, the phase-to-phase transition set, each being a Transition.
    - `Δ*`: #initialization, the initialization Initialization.

    A state `q ∈ Q` is represented as a `dict` that assigns a value to every state variable. `NIL` components
    of the formalism are represented by `None`.
    """

    ## `+∞`, the delay of a passive phase.
    INFINITY = math.inf

    def __init__(self):
        ## `Λ`: the parameter set, each parameter `λ` with its chosen value in `dom(λ)`.
        self.parameters: Dict[str, Any] = {}
        ## `X`: the input set (input port names).
        self.inputs: Set[str] = set()
        ## `Y`: the output set (output port names).
        self.outputs: Set[str] = set()
        ## `Σ`: the state variables with their current values, i.e., the current state `q ∈ Q`.
        self.state: State = {}
        ## `Φ`: the phase set, indexed by phase name.
        self.phases: Dict[str, Phase] = {}
        ## `Δ`: the phase-to-phase transition set.
        self.transitions: List[Transition] = []
        ## `Δ*`: the initialization.
        self.initialization: Optional[Initialization] = None
        ## The current phase `φ ∈ Φ`.
        self.current_phase: Optional[str] = None
        ## The time elapsed in the current phase.
        self.elapsed: float = 0.0

    # ---------------------------------------------------------------- specification (building the 7-tuple)

    def add_parameter(self, name: str, value: Any) -> None:
        """! @brief Adds a parameter `λ ∈ Λ` with its value."""
        self.parameters[name] = value

    def add_input(self, port: str) -> None:
        """! @brief Adds an input port `x ∈ X`."""
        self.inputs.add(port)

    def add_output(self, port: str) -> None:
        """! @brief Adds an output port `y ∈ Y`."""
        self.outputs.add(port)

    def add_state_variable(self, name: str, initial_value: Any) -> None:
        """! @brief Adds a state variable `σ ∈ Σ` with its initial value."""
        self.state[name] = initial_value

    def add_phase(self, phase: Phase) -> None:
        """! @brief Adds a phase `φ ∈ Φ`."""
        self.phases[phase.name] = phase

    def add_transition(self, transition: Transition) -> None:
        """!
        @brief Adds a transition to `Δ`; its source and target phases must already be in `Φ`.
        @exception ValueError if a phase is not in `Φ`.
        """
        if transition.from_phase not in self.phases or transition.to_phase not in self.phases:
            raise ValueError("Transition " + transition.from_phase + " -> " + transition.to_phase
                             + " uses a phase that is not in Φ.")
        self.transitions.append(transition)

    def set_initialization(self, initialization: Initialization) -> None:
        """! @brief Sets the initialization `Δ*`."""
        self.initialization = initialization

    # ---------------------------------------------------------------- behaviour (DEVS-like execution)

    def initialize(self) -> None:
        """!
        @brief Runs the initialization `Δ*`: enters `φ_0`, applies `Update_0` and sets the elapsed time to `τ_0`.
        @exception RuntimeError if `Δ*` is missing, `φ_0 ∉ Φ` or `τ_0 ∉ [0, Delay_φ0]`.
        """
        if self.initialization is None or self.initialization.phase0 not in self.phases:
            raise RuntimeError("Δ* is missing or φ_0 is not in Φ.")
        phase0 = self.phases[self.initialization.phase0]
        if self.initialization.tau0 < 0 or self.initialization.tau0 > phase0.delay:
            raise RuntimeError("τ_0 must be in [0, Delay_φ0].")
        self.current_phase = phase0.name
        self._apply_to_state(self.initialization.update0)
        self.elapsed = self.initialization.tau0

    def time_advance(self) -> float:
        """!
        @return the time remaining before an internal transition must occur in the current phase:
        `Delay_φ − elapsed` (`+∞` for a passive phase).
        """
        return self.phases[self.current_phase].delay - self.elapsed

    def external_transition(self, x: Event, e: float) -> bool:
        """!
        @brief External transition `Δ^in`: an input event `x` arrives after `e` time units in the current phase.
        The first matching transition (source phase, `In`, `Guard`) is fired.
        @param x the input event.
        @param e the time elapsed since the last transition.
        @return `True` if a transition fired, `False` if the input was ignored.
        @exception ValueError if `x.port ∉ X`.
        """
        self._check_input(x)
        self.elapsed += e
        t = self._find(TransitionKind.IN, x)
        if t is None:
            return False
        self._fire(t, x)
        return True

    def internal_transition(self) -> List[Event]:
        """!
        @brief Internal transition `Δ^time`: fired when `Delay_i` of the current phase has elapsed.
        @return the output events emitted (`Send`); empty if no transition matched.
        """
        t = self._find(TransitionKind.TIME, None)
        return [] if t is None else self._fire(t, None)

    def confluent_transition(self, x: Event) -> List[Event]:
        """!
        @brief Confluent transition `Δ^{in,time}`: an input event `x` arrives exactly as `Delay_i` elapses.
        @param x the input event.
        @return the output events emitted (`Send`); empty if no transition matched.
        """
        self._check_input(x)
        t = self._find(TransitionKind.IN_TIME, x)
        return [] if t is None else self._fire(t, x)

    def condition_holds(self) -> bool:
        """! @return `True` if the current state satisfies `Cond_φ` of the current phase (NIL = true)."""
        phase = self.phases[self.current_phase]
        return phase.cond is None or phase.cond(self.state)

    def get_current_phase(self) -> Optional[str]:
        """! @return the current phase `φ`."""
        return self.current_phase

    def get_state(self) -> State:
        """! @return a copy of the current state `q`."""
        return dict(self.state)

    def get_parameters(self) -> Dict[str, Any]:
        """! @return a copy of the parameters `Λ`."""
        return dict(self.parameters)

    # ---------------------------------------------------------------- helpers

    def _find(self, kind: str, x: Optional[Event]) -> Optional[Transition]:
        """! Finds the first transition of the given kind from the current phase that accepts `x` and whose guard holds."""
        for t in self.transitions:
            accepts = x is None or (t.in_ports is not None and x.port in t.in_ports)
            if t.kind == kind and t.from_phase == self.current_phase and accepts \
                    and (t.guard is None or t.guard(self.state)):
                return t
        return None

    def _fire(self, t: Transition, x: Optional[Event]) -> List[Event]:
        """! Fires `t`: computes `Send`, applies `Update`, enters phase `j` and runs its `Action`."""
        sent = [] if t.send is None else list(t.send(dict(self.state)))
        for y in sent:
            if y.port not in self.outputs:
                raise RuntimeError("Output port " + y.port + " is not in Y.")
        if t.update is not None:
            self._apply_to_state(lambda q: t.update(q, x))
        self.current_phase = t.to_phase
        self.elapsed = 0.0
        # Action_φ of the new phase (for a discrete phase it is applied on entry; a continuous flow
        # q' = f_φ(q) has to be integrated by the subclass).
        self._apply_to_state(self.phases[self.current_phase].action)
        return sent

    def _apply_to_state(self, f: Optional[Callable[[State], State]]) -> None:
        """! Replaces the state with `f(q)`, working on a copy so that `f` may return a new or the same dict."""
        if f is None:
            return
        self.state = dict(f(dict(self.state)))

    def _check_input(self, x: Event) -> None:
        """! Checks that `x.port ∈ X`."""
        if x.port not in self.inputs:
            raise ValueError("Input port " + x.port + " is not in X.")


# The nested types of the Java version: DT_Automaton.Event, DT_Automaton.Phase, ...
DT_Automaton.Event = Event
DT_Automaton.Phase = Phase
DT_Automaton.TransitionKind = TransitionKind
DT_Automaton.Transition = Transition
DT_Automaton.Initialization = Initialization
