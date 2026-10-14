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
/// \file DT_Automaton.cpp
/// \brief Implementation of the class of automata `𝒜`.

#include "Automaton/DT_Automaton.hpp"

#include <charconv>
#include <cstdint>
#include <stdexcept>

namespace Automaton {

std::string formatDouble(double value) {
  if (std::isnan(value)) return "NaN";
  if (std::isinf(value)) return value > 0 ? "Infinity" : "-Infinity";
  char buffer[64];
  // As Java: scientific notation below 10^-3 and from 10^7 (in absolute value), fixed otherwise.
  double magnitude = std::fabs(value);
  bool scientific = magnitude != 0.0 && (magnitude < 1e-3 || magnitude >= 1e7);
  auto result = std::to_chars(buffer, buffer + sizeof(buffer), value,
                              scientific ? std::chars_format::scientific : std::chars_format::fixed);
  std::string text(buffer, result.ptr);
  std::string::size_type e = text.find('e');
  if (e != std::string::npos) {
    // 1e-04 -> 1.0E-4
    std::string mantissa = text.substr(0, e);
    int exponent = std::stoi(text.substr(e + 1));
    if (mantissa.find('.') == std::string::npos) mantissa += ".0";
    return mantissa + "E" + std::to_string(exponent);
  }
  if (text.find('.') == std::string::npos) text += ".0";
  return text;
}

std::string anyToString(const std::any& value) {
  if (!value.has_value()) return "null";
  const std::type_info& type = value.type();
  if (type == typeid(double)) return formatDouble(std::any_cast<double>(value));
  if (type == typeid(float)) return formatDouble(std::any_cast<float>(value));
  if (type == typeid(int)) return std::to_string(std::any_cast<int>(value));
  if (type == typeid(unsigned int)) return std::to_string(std::any_cast<unsigned int>(value));
  if (type == typeid(long)) return std::to_string(std::any_cast<long>(value));
  if (type == typeid(long long)) return std::to_string(std::any_cast<long long>(value));
  if (type == typeid(unsigned long)) return std::to_string(std::any_cast<unsigned long>(value));
  if (type == typeid(unsigned long long)) return std::to_string(std::any_cast<unsigned long long>(value));
  if (type == typeid(bool)) return std::any_cast<bool>(value) ? "true" : "false";
  if (type == typeid(std::string)) return std::any_cast<std::string>(value);
  if (type == typeid(const char*)) return std::any_cast<const char*>(value);
  return "?";
}

std::string mapToString(const State& values) {
  std::string text = "{";
  for (auto it = values.begin(); it != values.end(); ++it) {
    if (it != values.begin()) text += ", ";
    text += it->first + "=" + anyToString(it->second);
  }
  return text + "}";
}

DT_Automaton::Phase::Phase(std::string name, Condition cond, StateAction action, double delay)
    : name(std::move(name)), cond(std::move(cond)), action(std::move(action)), delay(delay) {
  if (delay < 0) throw std::invalid_argument("Delay of phase " + this->name + " must be >= 0.");
}

// ---------------------------------------------------------------- specification (building the 7-tuple)

void DT_Automaton::addParameter(const std::string& name, std::any value) { parameters[name] = std::move(value); }

void DT_Automaton::addInput(const std::string& port) { inputs.insert(port); }

void DT_Automaton::addOutput(const std::string& port) { outputs.insert(port); }

void DT_Automaton::addStateVariable(const std::string& name, std::any initialValue) {
  state[name] = std::move(initialValue);
}

void DT_Automaton::addPhase(const Phase& phase) {
  phases.erase(phase.name);
  phases.emplace(phase.name, phase);
}

void DT_Automaton::addTransition(const Transition& transition) {
  if (phases.count(transition.from) == 0 || phases.count(transition.to) == 0)
    throw std::invalid_argument("Transition " + transition.from + " -> " + transition.to +
                                " uses a phase that is not in Φ.");
  transitions.push_back(transition);
}

void DT_Automaton::setInitialization(const Initialization& init) { initialization = init; }

// ---------------------------------------------------------------- behaviour (DEVS-like execution)

void DT_Automaton::initialize() {
  auto it = phases.find(initialization.phase0);
  if (initialization.phase0.empty() || it == phases.end())
    throw std::logic_error("Δ* is missing or φ_0 is not in Φ.");
  const Phase& phase0 = it->second;
  if (initialization.tau0 < 0 || initialization.tau0 > phase0.delay)
    throw std::logic_error("τ_0 must be in [0, Delay_φ0].");
  currentPhase = phase0.name;
  applyToState(initialization.update0);
  elapsed = initialization.tau0;
}

double DT_Automaton::timeAdvance() const { return phases.at(currentPhase).delay - elapsed; }

bool DT_Automaton::externalTransition(const Event& x, double e) {
  checkInput(x);
  elapsed += e;
  const Transition* t = find(TransitionKind::In, &x);
  if (t == nullptr) return false;
  fire(*t, &x);
  return true;
}

std::vector<DT_Automaton::Event> DT_Automaton::internalTransition() {
  const Transition* t = find(TransitionKind::Time, nullptr);
  if (t == nullptr) return {};
  return fire(*t, nullptr);
}

std::vector<DT_Automaton::Event> DT_Automaton::confluentTransition(const Event& x) {
  checkInput(x);
  const Transition* t = find(TransitionKind::InTime, &x);
  if (t == nullptr) return {};
  return fire(*t, &x);
}

bool DT_Automaton::conditionHolds() const {
  const Phase& phase = phases.at(currentPhase);
  return !phase.cond || phase.cond(state);
}

std::string DT_Automaton::eventsToString(const std::vector<Event>& events) {
  std::string text = "[";
  for (std::size_t i = 0; i < events.size(); ++i) {
    if (i > 0) text += ", ";
    text += events[i].toString();
  }
  return text + "]";
}

// ---------------------------------------------------------------- helpers

const DT_Automaton::Transition* DT_Automaton::find(TransitionKind kind, const Event* x) const {
  for (const Transition& t : transitions) {
    bool accepts = x == nullptr || t.in.count(x->port) > 0;
    if (t.kind == kind && t.from == currentPhase && accepts && (!t.guard || t.guard(state))) return &t;
  }
  return nullptr;
}

std::vector<DT_Automaton::Event> DT_Automaton::fire(const Transition& t, const Event* x) {
  std::vector<Event> sent = t.send ? t.send(state) : std::vector<Event>{};
  for (const Event& y : sent)
    if (outputs.count(y.port) == 0) throw std::logic_error("Output port " + y.port + " is not in Y.");
  if (t.update) {
    UpdateFunction update = t.update;
    applyToState([&update, x](State q) { return update(std::move(q), x); });
  }
  currentPhase = t.to;
  elapsed = 0;
  // Action_φ of the new phase (for a discrete phase it is applied on entry; a continuous flow
  // q' = f_φ(q) has to be integrated by the subclass).
  applyToState(phases.at(currentPhase).action);
  return sent;
}

void DT_Automaton::applyToState(const StateAction& f) {
  if (!f) return;
  state = f(state);
}

void DT_Automaton::checkInput(const Event& x) const {
  if (inputs.count(x.port) == 0) throw std::invalid_argument("Input port " + x.port + " is not in X.");
}

}  // namespace Automaton
