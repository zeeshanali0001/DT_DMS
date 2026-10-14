// DT-DMS sine example (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Service_Diagnosis_Sine.hpp
/// \brief Diagnosis service of the sine example.

#ifndef DT_DMS_SERVICE_DIAGNOSIS_SINE_HPP
#define DT_DMS_SERVICE_DIAGNOSIS_SINE_HPP

#include <any>
#include <cmath>
#include <cstdio>
#include <iostream>
#include <limits>
#include <memory>
#include <random>
#include <string>
#include <vector>

#include "Models/DT_Model_Descr_CubicBSpline.hpp"
#include "Services/DT_Service_Diagnosis.hpp"

namespace Services {

/// \brief Diagnosis service of the sine example (`s ∈ S_diag`): it detects the anomalies of the measured sine
/// wave.
///
/// The service is coupled to the Cubic B-Spline model and consumes the latest measured value: `s(m, ω)`
/// compares the value `d` of `ω` with the value expected by the model at the same time; the difference is the
/// residual. An anomaly is reported when the residual exceeds the threshold.
///
/// It can also check the interpolation itself at random points between the samples (performRandomCheck()),
/// against the exact sine function.
///
/// The service is specified as the automaton `𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`:
/// - `Λ` = {threshold}: the largest residual considered normal.
/// - `X` = {data}: a measured sine value.
/// - `Y` = {anomaly}: the residual of an anomalous value.
/// - `Σ` = {residual}: the residual of the last value checked.
/// - `Φ` = {IDLE ⟨residual ≥ 0, NIL, ∞⟩, CHECK ⟨residual ≥ 0, NIL, 0⟩}.
/// - `Δ^in`: IDLE → CHECK on `(data, d)`, Update: `residual := |d − m(t)|`.
/// - `Δ^time`: CHECK → IDLE, Guard: `residual > threshold`, Send: `(anomaly, residual)`.
/// - `Δ^time`: CHECK → IDLE, Guard: `residual ≤ threshold`, no output.
/// - `Δ*` = ⟨IDLE, NIL, 0⟩.
class DT_Service_Diagnosis_Sine : public DT_Service_Diagnosis<double> {

 public:
  /// \brief Constructor of the sine diagnosis service.
  /// \param threshold the largest residual considered normal.
  explicit DT_Service_Diagnosis_Sine(double threshold)
      : DT_Service_Diagnosis<double>("Sine diagnosis", "Detects the anomalies of the measured sine wave", -1.0, 1.0),
        threshold(threshold) {
    specifyAutomaton();
  }

  /// The service holds itself in the lambdas of its automaton: it cannot be copied.
  DT_Service_Diagnosis_Sine(const DT_Service_Diagnosis_Sine&) = delete;
  /// The service holds itself in the lambdas of its automaton: it cannot be copied.
  DT_Service_Diagnosis_Sine& operator=(const DT_Service_Diagnosis_Sine&) = delete;

  /// \brief Executes the diagnosis on the latest value consumed: the automaton goes IDLE → CHECK → IDLE and an
  /// anomaly is reported if it emits the output `(anomaly, residual)`.
  /// \return the diagnosis report.
  Output executeService() override {
    auto spline = std::dynamic_pointer_cast<Models::DT_Model_Descr_CubicBSpline>(model);
    if (consumedData.empty() || !spline) {
      diagnosisReport = "Diagnosis failed: no data or no Cubic B-Spline model.";
      return diagnosisReport;
    }
    double d = consumedData.back();
    externalTransition(Event("data", d), spline->getStep());
    std::vector<Event> emitted = internalTransition();
    if (emitted.empty()) {
      diagnosisReport = "OK";
    } else {
      double t = std::round(spline->getLastTime() * 1000.0) / 1000.0;
      anomalyTimes.push_back(t);
      diagnosisReport = "ANOMALY DETECTED at t=" + Automaton::formatDouble(t) + ": actual=" + Automaton::formatDouble(d) +
                        ", expected=" + Automaton::formatDouble(spline->getSineModelValue(spline->getLastTime())) +
                        ", residual=" + Automaton::anyToString(emitted.front().value);
    }
    return diagnosisReport;
  }

  /// \brief Checks the interpolation of the sine function at random points: draws `numberOfPointsToCheck`
  /// abscissas `x` uniformly in the domain of the model and compares `sin(x)` with the value of the B-spline at
  /// `x`. Unlike executeService(), which checks the measured values, this check uses the exact sine function
  /// and points between the samples.
  /// \param numberOfPointsToCheck the number of random points.
  /// \param seed                  the seed of the random generator (the same seed gives the same points).
  /// \param verbose               `true` to print one line per point.
  /// \return the largest deviation `|sin(x) − m(x)|` found (`NaN` if no Cubic B-Spline model with data is
  ///         coupled to the service).
  double performRandomCheck(int numberOfPointsToCheck, unsigned int seed, bool verbose) const {
    auto spline = std::dynamic_pointer_cast<Models::DT_Model_Descr_CubicBSpline>(model);
    if (!spline || std::isnan(spline->getLastTime())) return std::numeric_limits<double>::quiet_NaN();
    std::mt19937 generator(seed);
    std::uniform_real_distribution<double> abscissa(spline->getLeftEndpoint(), spline->getLastTime());
    double maxDeviation = 0.0;
    for (int i = 0; i < numberOfPointsToCheck; ++i) {
      double x = abscissa(generator);
      double expected = std::sin(x);
      double interpolated = spline->getSineModelValue(x);
      maxDeviation = std::max(maxDeviation, std::fabs(expected - interpolated));
      if (verbose) {
        char line[160];
        std::snprintf(line, sizeof(line),
                      "     sin(%.6f) = %+.9f    spline interpolation gives %+.9f    delta = %.3e", x, expected,
                      interpolated, expected - interpolated);
        std::cout << line << "\n";
      }
    }
    return maxDeviation;
  }

  /// \return the threshold for anomaly detection.
  double getThreshold() const { return threshold; }

  /// \return the times at which an anomaly was detected.
  const std::vector<double>& getAnomalyTimes() const { return anomalyTimes; }

  /// \return the anomaly times as text: `[t1, t2, ...]`.
  std::string anomalyTimesToString() const {
    std::string text = "[";
    for (std::size_t i = 0; i < anomalyTimes.size(); ++i)
      text += (i > 0 ? ", " : "") + Automaton::formatDouble(anomalyTimes[i]);
    return text + "]";
  }

 protected:
  /// \brief Computes the residual of a measured value: its distance to the value expected by the coupled
  /// Cubic B-Spline model at the time of the last value assimilated.
  /// \param d the measured value.
  /// \return the residual `|d − m(t)|` (0 if the service is not coupled to a Cubic B-Spline model).
  double residualOf(double d) const {
    if (auto spline = std::dynamic_pointer_cast<Models::DT_Model_Descr_CubicBSpline>(model))
      return std::fabs(d - spline->getSineModelValue(spline->getLastTime()));
    return 0.0;
  }

  /// Threshold for anomaly detection (residual between the measured value and the model).
  double threshold;
  /// The times at which an anomaly was detected.
  std::vector<double> anomalyTimes;

 private:
  /// Specifies the 7-tuple `𝒮` of the service and runs its initialization `Δ*`.
  void specifyAutomaton() {
    auto residual = [](const Automaton::State& q) { return std::any_cast<double>(q.at("residual")); };
    addParameter("threshold", threshold);                                                          // Λ
    addInput("data");                                                                              // X
    addOutput("anomaly");                                                                          // Y
    addStateVariable("residual", 0.0);                                                             // Σ
    addPhase(Phase("IDLE", [residual](const Automaton::State& q) { return residual(q) >= 0; }, nullptr,  // Φ
                   infinity()));
    addPhase(Phase("CHECK", [residual](const Automaton::State& q) { return residual(q) >= 0; }, nullptr, 0.0));
    addTransition(Transition(TransitionKind::In, "IDLE", "CHECK", {"data"}, nullptr, nullptr,    // Δ^in
                             [this](Automaton::State q, const Event* x) {
                               q["residual"] = residualOf(std::any_cast<double>(x->value));
                               return q;
                             }));
    addTransition(Transition(TransitionKind::Time, "CHECK", "IDLE", {},                           // Δ^time (anomaly)
                             [this, residual](const Automaton::State& q) { return residual(q) > threshold; },
                             [](const Automaton::State& q) { return std::vector<Event>{{"anomaly", q.at("residual")}}; },
                             nullptr));
    addTransition(Transition(TransitionKind::Time, "CHECK", "IDLE", {},                           // Δ^time (normal)
                             [this, residual](const Automaton::State& q) { return residual(q) <= threshold; },
                             nullptr, nullptr));
    setInitialization(Initialization("IDLE", nullptr, 0.0));                                      // Δ*
    initialize();
  }
};

}  // namespace Services

#endif  // DT_DMS_SERVICE_DIAGNOSIS_SINE_HPP
