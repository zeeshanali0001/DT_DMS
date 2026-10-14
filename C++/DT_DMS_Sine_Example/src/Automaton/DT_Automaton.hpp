//
//     Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
//
//     This program is free software: you can redistribute it and/or modify
//     it under the terms of the GNU General Public License as published by
//     the Free Software Foundation, either version 3 of the License, or
//     (at your option) any later version.
//
//     This program is distributed in the hope that it will be useful,
//     but WITHOUT ANY WARRANTY; without even the implied warranty of
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//     GNU General Public License for more details.
//
//     You should have received a copy of the GNU General Public License
//     along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
/// \file DT_Automaton.hpp
/// \brief The class of automata `𝒜` of the DT-DMS framework.

#ifndef DT_DMS_AUTOMATON_HPP
#define DT_DMS_AUTOMATON_HPP

#include <any>
#include <cmath>
#include <functional>
#include <limits>
#include <map>
#include <set>
#include <sstream>
#include <string>
#include <type_traits>
#include <vector>

/// \brief The class of automata (`𝒜`) of the DT-DMS framework.
///
/// DT models and DT services are specified with the same formalism because both belong to the same class of
/// automata: `M ⊆ 𝒜` and `S ⊆ 𝒜`. An automaton, whether discrete, continuous or hybrid, is specified with
/// HiLLS (based on DEVS) as the 7-tuple `⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`.
namespace Automaton {

/// A state `q ∈ Q`: assigns a value to every state variable `σ ∈ Σ` (the domain `dom(σ)` is the C++ type of the value).
using State = std::map<std::string, std::any>;

/// \brief Converts a number to text as Java does: the shortest text that reads back the same value, with at
/// least one decimal (`35.0`, `0.05`, `1.0E-4`), `NaN`, `Infinity` or `-Infinity`.
std::string formatDouble(double value);

/// Converts a value held by a `std::any` to text (numbers, booleans and strings; other types give `?`).
std::string anyToString(const std::any& value);

/// Converts a state, a parameter set or any map of values to text: `{name=value, ...}`.
std::string mapToString(const State& values);

/// \brief Converts a datum to a number, when it is one.
/// \param d the datum.
/// \return its numeric value, or `NaN` if the datum is not a number.
template <typename V>
double numericValue(const V& d) {
  if constexpr (std::is_arithmetic_v<V>)
    return static_cast<double>(d);
  else
    return std::numeric_limits<double>::quiet_NaN();
}

/// \brief Converts a datum to text with `operator<<` when it has one.
template <typename V>
std::string valueToString(const V& d) {
  if constexpr (std::is_same_v<V, std::any>) {
    return anyToString(d);
  } else if constexpr (std::is_floating_point_v<V>) {
    return formatDouble(static_cast<double>(d));
  } else {
    std::ostringstream out;
    out << d;
    return out.str();
  }
}

/// \brief Abstract base class representing the class of automata `𝒜` of the DT-DMS framework.
///
/// DT models and DT services belong to the same class of automata, i.e., `M ⊆ 𝒜` and `S ⊆ 𝒜`. Therefore
/// both `DT_Model` and `DT_Service` inherit from this class. Each automaton, whether discrete, continuous or
/// hybrid, is specified with HiLLS (based on DEVS) as the 7-tuple (written `ℳ` for a model and `𝒮` for a
/// service):
///
///     ℳ = ⟨ Λ, X, Y, Σ, Φ, Δ, Δ* ⟩        𝒮 = ⟨ Λ, X, Y, Σ, Φ, Δ, Δ* ⟩
///
/// Mapping of the 7-tuple to this class:
/// - `Λ`: #parameters, the parameter set (context assumptions).
/// - `X`: #inputs, the input set (what is received from the ToI).
/// - `Y`: #outputs, the output set (what is provided to, or influences, the ToI).
/// - `Σ`: #state, the state variables; its current content is the state `q ∈ Q`.
/// - `Φ`: #phases, the phase set, each phase being a Phase.
/// - `Δ`: #transitions, the phase-to-phase transition set, each being a Transition.
/// - `Δ*`: #initialization, the initialization Initialization.
///
/// A state `q ∈ Q` is represented as a State (a map from the variable names to `std::any` values).
/// `NIL` components of the formalism are represented by empty `std::function` objects.
class DT_Automaton {

 public:
  /// \brief An event `(port, v)`: an input event of `X_V` or an output event of `Y_V`, where `port ∈ X`
  /// (resp. `Y`) and `v ∈ dom(port)`.
  struct Event {
    /// The input or output port name (`x ∈ X` or `y ∈ Y`).
    std::string port;
    /// The value carried by the event (`v ∈ dom(port)`).
    std::any value;

    /// \param port  the port name.
    /// \param value the value carried by the event.
    Event(std::string port, std::any value) : port(std::move(port)), value(std::move(value)) {}

    /// \return `(port, value)`.
    std::string toString() const { return "(" + port + ", " + anyToString(value) + ")"; }
  };

  /// `Cond_φ`: a predicate on `Q` (an empty function is NIL).
  using Condition = std::function<bool(const State&)>;
  /// `Action_φ : Q → Q` or `Update_0 : Q → Q` (an empty function is NIL).
  using StateAction = std::function<State(State)>;
  /// `Send`: the output events, given as a function of the state (an empty function is NIL).
  using SendFunction = std::function<std::vector<Event>(const State&)>;
  /// `Update`: `Q × X_V → Q` (the event is `nullptr` for an internal transition; an empty function is NIL).
  using UpdateFunction = std::function<State(State, const Event*)>;

  /// \brief A phase `Φ_φ = ⟨Cond_φ, Action_φ, Delay_φ⟩`: a stable discrete or continuous step of the automaton.
  struct Phase {
    /// The phase name `φ`.
    std::string name;
    /// `Cond_φ`: the predicate on `Q` the state satisfies while in the phase (empty = NIL).
    Condition cond;
    /// `Action_φ : Q → Q`: what is computed during the phase (empty = NIL).
    StateAction action;
    /// `Delay_φ`: lifetime of the phase; `infinity()` for a passive phase.
    double delay;

    /// \param name   the phase name.
    /// \param cond   the phase condition, or `nullptr` (NIL).
    /// \param action the phase action, or `nullptr` (NIL).
    /// \param delay  the phase lifetime (`≥ 0`, or `infinity()`).
    /// \throw std::invalid_argument if the delay is negative.
    Phase(std::string name, Condition cond, StateAction action, double delay);
  };

  /// The three kinds of phase-to-phase transitions of `Δ`.
  enum class TransitionKind {
    In,     ///< External transition `Δ^in = ⟨In, Guard, Update⟩`, triggered when a specific input arrives.
    Time,   ///< Internal transition `Δ^time = ⟨Guard, Send, Update⟩`, triggered when `Delay_i` has elapsed.
    InTime  ///< Confluent transition `Δ^{in,time} = ⟨In, Guard, Send, Update⟩`, input arrives exactly as `Delay_i` elapses.
  };

  /// \brief A phase-to-phase transition `Δ_{i → j}` of kind TransitionKind.
  /// Components not used by a kind are empty (e.g., `in` for `Time`, `send` for `In`).
  struct Transition {
    /// The kind of the transition.
    TransitionKind kind;
    /// The source phase `i`.
    std::string from;
    /// The target phase `j`.
    std::string to;
    /// `In ⊆ X_V`: here given by the input ports whose events (with any value) trigger the transition.
    std::set<std::string> in;
    /// `Guard ∈ ℘(Q)`: the condition the state must also satisfy (empty = NIL, always true).
    Condition guard;
    /// `Send ∈ ℘(Y_V)`: the output events emitted, as a function of the state (empty = NIL).
    SendFunction send;
    /// `Update`: how the state changes; the event is `nullptr` for `Time` (empty = NIL).
    UpdateFunction update;

    /// \param kind   the transition kind.
    /// \param from   the source phase `i`.
    /// \param to     the target phase `j`.
    /// \param in     the triggering input ports (for `In`/`InTime`), else `{}`.
    /// \param guard  the guard, or `nullptr`.
    /// \param send   the output events (for `Time`/`InTime`), or `nullptr`.
    /// \param update the state update, or `nullptr`.
    Transition(TransitionKind kind, std::string from, std::string to, std::set<std::string> in,
               Condition guard, SendFunction send, UpdateFunction update)
        : kind(kind), from(std::move(from)), to(std::move(to)), in(std::move(in)),
          guard(std::move(guard)), send(std::move(send)), update(std::move(update)) {}
  };

  /// \brief The initialization `Δ* = ⟨φ_0, Update_0, τ_0⟩`.
  struct Initialization {
    /// `φ_0 ∈ Φ`: the starting phase.
    std::string phase0;
    /// `Update_0 : Q → Q`: the state-setting action run once at startup (empty = NIL).
    StateAction update0;
    /// `τ_0 ∈ [0, Delay_{φ_0}]`: the time already elapsed in `φ_0` at the start (usually 0).
    double tau0 = 0.0;

    /// \param phase0  the starting phase.
    /// \param update0 the startup state setting, or `nullptr`.
    /// \param tau0    the initial elapsed time.
    Initialization(std::string phase0, StateAction update0, double tau0)
        : phase0(std::move(phase0)), update0(std::move(update0)), tau0(tau0) {}
  };

  /// \return `+∞`, the delay of a passive phase.
  static double infinity() { return std::numeric_limits<double>::infinity(); }

  /// Destructor
  virtual ~DT_Automaton() = default;

  // ---------------------------------------------------------------- specification (building the 7-tuple)

  /// Adds a parameter `λ ∈ Λ` with its value.
  void addParameter(const std::string& name, std::any value);
  /// Adds an input port `x ∈ X`.
  void addInput(const std::string& port);
  /// Adds an output port `y ∈ Y`.
  void addOutput(const std::string& port);
  /// Adds a state variable `σ ∈ Σ` with its initial value.
  void addStateVariable(const std::string& name, std::any initialValue);
  /// Adds a phase `φ ∈ Φ`.
  void addPhase(const Phase& phase);
  /// \brief Adds a transition to `Δ`; its source and target phases must already be in `Φ`.
  /// \throw std::invalid_argument if a phase is not in `Φ`.
  void addTransition(const Transition& transition);
  /// Sets the initialization `Δ*`.
  void setInitialization(const Initialization& initialization);

  // ---------------------------------------------------------------- behaviour (DEVS-like execution)

  /// \brief Runs the initialization `Δ*`: enters `φ_0`, applies `Update_0` and sets the elapsed time to `τ_0`.
  /// \throw std::logic_error if `Δ*` is missing, `φ_0 ∉ Φ` or `τ_0 ∉ [0, Delay_φ0]`.
  void initialize();

  /// \return the time remaining before an internal transition must occur in the current phase:
  /// `Delay_φ − elapsed` (`+∞` for a passive phase).
  double timeAdvance() const;

  /// \brief External transition `Δ^in`: an input event `x` arrives after `e` time units in the current phase.
  /// The first matching transition (source phase, `In`, `Guard`) is fired.
  /// \param x the input event.
  /// \param e the time elapsed since the last transition.
  /// \return `true` if a transition fired, `false` if the input was ignored.
  /// \throw std::invalid_argument if `x.port ∉ X`.
  bool externalTransition(const Event& x, double e);

  /// \brief Internal transition `Δ^time`: fired when `Delay_i` of the current phase has elapsed.
  /// \return the output events emitted (`Send`); empty if no transition matched.
  std::vector<Event> internalTransition();

  /// \brief Confluent transition `Δ^{in,time}`: an input event `x` arrives exactly as `Delay_i` elapses.
  /// \param x the input event.
  /// \return the output events emitted (`Send`); empty if no transition matched.
  std::vector<Event> confluentTransition(const Event& x);

  /// \return `true` if the current state satisfies `Cond_φ` of the current phase (NIL = true).
  bool conditionHolds() const;

  /// \return the current phase `φ`.
  const std::string& getCurrentPhase() const { return currentPhase; }
  /// \return the current state `q`.
  const State& getState() const { return state; }
  /// \return the parameters `Λ`.
  const State& getParameters() const { return parameters; }

  /// \return the outputs as text: `[(port, value), ...]`.
  static std::string eventsToString(const std::vector<Event>& events);

 protected:
  /// `Λ`: the parameter set, each parameter `λ` with its chosen value in `dom(λ)`.
  State parameters;
  /// `X`: the input set (input port names).
  std::set<std::string> inputs;
  /// `Y`: the output set (output port names).
  std::set<std::string> outputs;
  /// `Σ`: the state variables with their current values, i.e., the current state `q ∈ Q`.
  State state;
  /// `Φ`: the phase set, indexed by phase name.
  std::map<std::string, Phase> phases;
  /// `Δ`: the phase-to-phase transition set.
  std::vector<Transition> transitions;
  /// `Δ*`: the initialization (empty phase name until it is set).
  Initialization initialization{"", nullptr, 0.0};
  /// The current phase `φ ∈ Φ`.
  std::string currentPhase;
  /// The time elapsed in the current phase.
  double elapsed = 0.0;

 private:
  /// Finds the first transition of the given kind from the current phase that accepts `x` and whose guard holds.
  const Transition* find(TransitionKind kind, const Event* x) const;
  /// Fires `t`: computes `Send`, applies `Update`, enters phase `j` and runs its `Action`.
  std::vector<Event> fire(const Transition& t, const Event* x);
  /// Replaces the state with `f(q)`.
  void applyToState(const StateAction& f);
  /// Checks that `x.port ∈ X`.
  void checkInput(const Event& x) const;
};

}  // namespace Automaton

#endif  // DT_DMS_AUTOMATON_HPP
