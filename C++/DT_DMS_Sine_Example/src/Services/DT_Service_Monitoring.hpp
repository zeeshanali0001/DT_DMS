// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Service_Monitoring.hpp
/// \brief A monitoring service `s ∈ S_moni`.

#ifndef DT_DMS_SERVICE_MONITORING_HPP
#define DT_DMS_SERVICE_MONITORING_HPP

#include <cmath>
#include <limits>
#include <string>

#include "Services/DT_Service.hpp"

namespace Services {

/// \brief Class representing a Monitoring Service (`s ∈ S_moni`) of the Digital Twin.
///
/// A Monitoring Service observes the state of the ToI in real time: it compares the latest value of a monitored
/// entity against thresholds and raises an alert when it is out of bounds.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Service_Monitoring : public DT_Service<T> {

 public:
  /// \brief Constructor to initialize the Monitoring Service with necessary attributes.
  /// \param serviceName        the name of the service.
  /// \param serviceDescription a brief description of the service.
  /// \param monitoredEntity    the entity being monitored.
  /// \param lowerThreshold     the lower limit of the monitored parameter.
  /// \param upperThreshold     the upper limit of the monitored parameter.
  DT_Service_Monitoring(std::string serviceName, std::string serviceDescription, std::string monitoredEntity,
                        double lowerThreshold, double upperThreshold)
      : DT_Service<T>(std::move(serviceName), std::move(serviceDescription)),
        monitoredEntity(std::move(monitoredEntity)), upperThreshold(upperThreshold), lowerThreshold(lowerThreshold) {}

  /// \brief Called when new data arrives: a numeric datum becomes the current value of the monitored parameter.
  /// \param data the new datum.
  void update(const T& data) override {
    DT_Service<T>::update(data);
    double v = Automaton::numericValue(data);
    if (!std::isnan(v)) currentValue = v;
  }

  /// \brief Executes the monitoring logic: compares the current value with the thresholds.
  /// \return the monitoring status (an alert if the value is out of bounds).
  typename DT_Service<T>::Output executeService() override {
    using Automaton::formatDouble;
    if (std::isnan(currentValue)) return std::string("STATUS: no value received yet for " + monitoredEntity + ".");
    if (currentValue > upperThreshold)
      return std::string("ALERT: " + monitoredEntity + " = " + formatDouble(currentValue) +
                         " exceeds the upper threshold " + formatDouble(upperThreshold) + "!");
    if (currentValue < lowerThreshold)
      return std::string("ALERT: " + monitoredEntity + " = " + formatDouble(currentValue) +
                         " falls below the lower threshold " + formatDouble(lowerThreshold) + "!");
    return std::string("STATUS: " + monitoredEntity + " = " + formatDouble(currentValue) + " is within safe limits.");
  }

  /// \return the name of the monitored entity.
  const std::string& getMonitoredEntity() const { return monitoredEntity; }
  /// \return the upper threshold.
  double getUpperThreshold() const { return upperThreshold; }
  /// \param value the new upper threshold.
  void setUpperThreshold(double value) { upperThreshold = value; }
  /// \return the lower threshold.
  double getLowerThreshold() const { return lowerThreshold; }
  /// \param value the new lower threshold.
  void setLowerThreshold(double value) { lowerThreshold = value; }
  /// \return the current value of the monitored parameter.
  double getCurrentValue() const { return currentValue; }

 protected:
  /// The name of the entity being monitored (e.g., "Temperature Sensor", "Power Grid").
  std::string monitoredEntity;
  /// The upper threshold limit for the monitored parameter.
  double upperThreshold;
  /// The lower threshold limit for the monitored parameter.
  double lowerThreshold;
  /// The current value of the monitored parameter (`NaN` until a value is received).
  double currentValue = std::numeric_limits<double>::quiet_NaN();
};

}  // namespace Services

#endif  // DT_DMS_SERVICE_MONITORING_HPP
