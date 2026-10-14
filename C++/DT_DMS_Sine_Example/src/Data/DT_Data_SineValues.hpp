// DT-DMS sine example (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Data_SineValues.hpp
/// \brief Data source of the sine example: the ToI producing a sine wave.

#ifndef DT_DMS_DATA_SINE_VALUES_HPP
#define DT_DMS_DATA_SINE_VALUES_HPP

#include <cmath>
#include <limits>
#include <string>

#include "Data/DT_Data.hpp"

namespace Data {

/// \brief Data source of the sine example: it plays the role of the ToI (the "physical twin") and produces the
/// sine wave values measured over time.
///
/// Each call to measure() produces the datum `d = ω(t) = sin(t)` measured at time `t` and records it; the
/// observers (the data assimilation function `μ`) are notified at once. An anomaly can be injected to test the
/// diagnosis service.
class DT_Data_SineValues : public DT_Data<double> {

 public:
  /// \brief Constructor to initialize the sine data source.
  /// \param name descriptive name of this data source (e.g., "Sine values").
  explicit DT_Data_SineValues(const std::string& name) : DT_Data<double>(name, "Sine") {}

  /// \brief Measures the ToI at time `t`: produces `sin(t)` (plus the anomaly offset, if any at that time),
  /// records it and notifies the observers.
  /// \param t the measurement time.
  /// \return the measured value.
  double measure(double t) {
    double value = std::sin(t);
    if (std::fabs(t - anomalyTime) < 1e-9) value += anomalyOffset;
    updateData(t, value);
    return value;
  }

  /// \brief Injects an anomaly: the value measured at `time` will be shifted by `offset`.
  /// \param time   the measurement time of the anomaly.
  /// \param offset the offset added to the sine value.
  void injectAnomaly(double time, double offset) {
    anomalyTime = time;
    anomalyOffset = offset;
  }

 private:
  /// The time at which an anomaly is injected (`NaN` = no anomaly).
  double anomalyTime = std::numeric_limits<double>::quiet_NaN();
  /// The offset added to the sine value at #anomalyTime.
  double anomalyOffset = 0.0;
};

}  // namespace Data

#endif  // DT_DMS_DATA_SINE_VALUES_HPP
