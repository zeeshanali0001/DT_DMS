// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Service_Control.hpp
/// \brief A control service `s ∈ S_cont`.

#ifndef DT_DMS_SERVICE_CONTROL_HPP
#define DT_DMS_SERVICE_CONTROL_HPP

#include <memory>
#include <string>

#include "Models/DT_Model_Prescriptive.hpp"
#include "Services/DT_Service.hpp"

namespace Services {

/// \brief Class representing a Control Service (`s ∈ S_cont`) of the Digital Twin.
///
/// A Control Service acts back on the ToI (through actuators or other systems) based on feedback: it turns the
/// latest consumed value and the target of the coupled prescriptive model into a command.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Service_Control : public DT_Service<T> {

 public:
  /// \brief Constructor to initialize the Control Service with necessary attributes.
  /// \param serviceName        the name of the service.
  /// \param serviceDescription a brief description of the service.
  /// \param controlStrategy    the control strategy applied.
  /// \param gain               the gain applied to the error.
  DT_Service_Control(std::string serviceName, std::string serviceDescription, std::string controlStrategy, double gain)
      : DT_Service<T>(std::move(serviceName), std::move(serviceDescription)),
        controlStrategy(std::move(controlStrategy)), gain(gain) {}

  /// \brief Executes the control logic: command = gain × (target − latest consumed value).
  /// \return the control action result.
  typename DT_Service<T>::Output executeService() override {
    using Automaton::formatDouble;
    auto prescriptive = std::dynamic_pointer_cast<Models::DT_Model_Prescriptive<T>>(this->model);
    if (!prescriptive) {
      controlActionResult = "Control action failed: no prescriptive model is coupled to the service.";
    } else if (this->consumedData.empty()) {
      controlActionResult = "Control action failed due to lack of inputs.";
    } else {
      double measured = Automaton::numericValue(this->consumedData.back());
      double command = gain * (prescriptive->getTarget() - measured);
      controlActionResult = "Control action (" + controlStrategy + "): command = " + formatDouble(command) +
                            " for measured value " + formatDouble(measured) + " and target " +
                            formatDouble(prescriptive->getTarget()) + ".";
    }
    return controlActionResult;
  }

  /// \return the control strategy.
  const std::string& getControlStrategy() const { return controlStrategy; }
  /// \return the result of the last control action.
  const std::string& getControlActionResult() const { return controlActionResult; }

 protected:
  /// The control strategy applied (e.g., "Proportional", "PID Control", "Rule-Based Control").
  std::string controlStrategy;
  /// The gain applied to the error between the target and the measured value.
  double gain;
  /// The result of the last control action.
  std::string controlActionResult = "No control action performed yet.";
};

}  // namespace Services

#endif  // DT_DMS_SERVICE_CONTROL_HPP
