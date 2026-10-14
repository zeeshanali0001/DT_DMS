// DT-DMS sine example (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Model_Descr_CubicBSpline.hpp
/// \brief Cubic B-Spline model of the sine example.

#ifndef DT_DMS_MODEL_DESCR_CUBIC_BSPLINE_HPP
#define DT_DMS_MODEL_DESCR_CUBIC_BSPLINE_HPP

#include <algorithm>
#include <any>
#include <cmath>
#include <limits>
#include <vector>

#include "Models/DT_Model_Descriptive.hpp"

namespace Models {

/// \brief Cubic B-Spline model of the sine example: a descriptive model (`m ∈ M_desc`) that represents the
/// measured sine wave by a smooth curve.
///
/// The assimilated values are the control points of a uniform cubic B-spline: the i-th value assimilated is
/// the control point of time `leftEndpoint + i × step`. The data must therefore be assimilated in time order,
/// one value per step. (The first C++ version used `boost::math::interpolators::cardinal_cubic_b_spline`; this
/// version needs no external library and gives the same model as the Java version.)
///
/// The model is specified as the automaton `ℳ = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`:
/// - `Λ` = {step, leftEndpoint}: the sampling assumptions.
/// - `X` = {data}: a measured sine value.
/// - `Y` = {updated}: the number of control points after an update.
/// - `Σ` = {count, last}: the number of control points and the last value assimilated.
/// - `Φ` = {WAIT ⟨count ≥ 0, NIL, ∞⟩, UPDATE ⟨count ≥ 1, NIL, 0⟩}.
/// - `Δ^in`: WAIT → UPDATE on `(data, d)`, Update: `count := count + 1; last := d`.
/// - `Δ^time`: UPDATE → WAIT, Send: `(updated, count)`.
/// - `Δ*` = ⟨WAIT, NIL, 0⟩.
class DT_Model_Descr_CubicBSpline : public DT_Model_Descriptive<double> {

 public:
  /// \brief Constructor for the Cubic B-Spline model.
  /// \param step         step size between samples.
  /// \param leftEndpoint time of the first sample.
  DT_Model_Descr_CubicBSpline(double step, double leftEndpoint)
      : DT_Model_Descriptive<double>("Cubic B-Spline", "Sine values"), step(step), leftEndpoint(leftEndpoint) {
    specifyAutomaton();
  }

  /// \brief Updates the model with a new sine value: it becomes the next control point, and the automaton goes
  /// WAIT → UPDATE → WAIT.
  /// \param updatedData the new sine value.
  void update(const double& updatedData) override {
    DT_Model_Descriptive<double>::update(updatedData);  // descriptive statistics (count, min, max, mean)
    controlPoints.push_back(updatedData);
    externalTransition(Event("data", updatedData), step);
    internalTransition();
  }

  /// \brief Returns the value of the model at position `x`: the uniform cubic B-spline of the control points.
  /// \param x the time.
  /// \return the model value (`NaN` if no data has been assimilated).
  double getSineModelValue(double x) const {
    if (controlPoints.empty()) return std::numeric_limits<double>::quiet_NaN();
    double s = (x - leftEndpoint) / step;
    int i = static_cast<int>(std::floor(s));
    double u = s - i;
    double u2 = u * u;
    double u3 = u2 * u;
    return ((1 - u) * (1 - u) * (1 - u) * point(i - 1) + (3 * u3 - 6 * u2 + 4) * point(i) +
            (-3 * u3 + 3 * u2 + 3 * u + 1) * point(i + 1) + u3 * point(i + 2)) /
           6.0;
  }

  /// \brief Returns the value of the model at time `t`: the B-spline, compared with the measured sine values by
  /// `DT_DMS::checkModelsVersusData`.
  /// \param t the time.
  /// \return getSineModelValue(t).
  double evaluate(double t) const override { return getSineModelValue(t); }

  /// \return the time of the last value assimilated (`NaN` if none).
  double getLastTime() const {
    return controlPoints.empty() ? std::numeric_limits<double>::quiet_NaN()
                                 : leftEndpoint + static_cast<double>(controlPoints.size() - 1) * step;
  }

  /// \return the left endpoint of the domain.
  double getLeftEndpoint() const { return leftEndpoint; }

  /// \return the step size between samples.
  double getStep() const { return step; }

 protected:
  /// Step size between two samples of the sine wave.
  double step;
  /// Left endpoint of the domain: the time of the first sample (usually 0.0).
  double leftEndpoint;
  /// The control points of the B-spline: the values assimilated, in time order.
  std::vector<double> controlPoints;

 private:
  /// Specifies the 7-tuple `ℳ` of the model and runs its initialization `Δ*`.
  void specifyAutomaton() {
    addParameter("step", step);                                                                   // Λ
    addParameter("leftEndpoint", leftEndpoint);
    addInput("data");                                                                             // X
    addOutput("updated");                                                                         // Y
    addStateVariable("count", 0);                                                                 // Σ
    addStateVariable("last", std::numeric_limits<double>::quiet_NaN());
    addPhase(Phase("WAIT", [](const Automaton::State& q) { return std::any_cast<int>(q.at("count")) >= 0; },  // Φ
                   nullptr, infinity()));
    addPhase(Phase("UPDATE", [](const Automaton::State& q) { return std::any_cast<int>(q.at("count")) >= 1; },
                   nullptr, 0.0));
    addTransition(Transition(TransitionKind::In, "WAIT", "UPDATE", {"data"}, nullptr, nullptr,  // Δ^in
                             [](Automaton::State q, const Event* x) {
                               q["count"] = std::any_cast<int>(q.at("count")) + 1;
                               q["last"] = x->value;
                               return q;
                             }));
    addTransition(Transition(TransitionKind::Time, "UPDATE", "WAIT", {}, nullptr,                // Δ^time
                             [](const Automaton::State& q) { return std::vector<Event>{{"updated", q.at("count")}}; },
                             nullptr));
    setInitialization(Initialization("WAIT", nullptr, 0.0));                                     // Δ*
    initialize();
  }

  /// \return the control point of index `i`; the first and last points are repeated at the ends.
  double point(int i) const {
    int last = static_cast<int>(controlPoints.size()) - 1;
    return controlPoints[static_cast<std::size_t>(std::max(0, std::min(last, i)))];
  }
};

}  // namespace Models

#endif  // DT_DMS_MODEL_DESCR_CUBIC_BSPLINE_HPP
