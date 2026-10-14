// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Service_Optimization.hpp
/// \brief An optimization service `s ∈ S_opti`.

#ifndef DT_DMS_SERVICE_OPTIMIZATION_HPP
#define DT_DMS_SERVICE_OPTIMIZATION_HPP

#include <memory>
#include <string>

#include "Models/DT_Model_Prescriptive.hpp"
#include "Services/DT_Service.hpp"

namespace Services {

/// \brief Class representing an Optimization Service (`s ∈ S_opti`) of the Digital Twin.
///
/// An Optimization Service finds the best solution for the ToI (performance, resource allocation, ...). It
/// relies on the prescriptive model it is coupled to for the recommendation.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Service_Optimization : public DT_Service<T> {

 public:
  /// \brief Constructor to initialize the Optimization Service with necessary attributes.
  /// \param serviceName           the name of the service.
  /// \param serviceDescription    a brief description of the service.
  /// \param optimizationAlgorithm the algorithm used for optimization.
  DT_Service_Optimization(std::string serviceName, std::string serviceDescription, std::string optimizationAlgorithm)
      : DT_Service<T>(std::move(serviceName), std::move(serviceDescription)),
        optimizationAlgorithm(std::move(optimizationAlgorithm)) {}

  /// \brief Executes the optimization logic: asks the coupled prescriptive model for its recommendation.
  /// \return the optimization result.
  typename DT_Service<T>::Output executeService() override {
    if (auto prescriptive = std::dynamic_pointer_cast<Models::DT_Model_Prescriptive<T>>(this->model))
      optimizationResult = "Optimization (" + optimizationAlgorithm + ") for objective '" +
                           prescriptive->getObjective() + "': " + prescriptive->recommend();
    else
      optimizationResult = "Optimization failed: no prescriptive model is coupled to the service.";
    return optimizationResult;
  }

  /// \return the optimization algorithm.
  const std::string& getOptimizationAlgorithm() const { return optimizationAlgorithm; }
  /// \return the result of the last optimization.
  const std::string& getOptimizationResult() const { return optimizationResult; }

 protected:
  /// The optimization algorithm used (e.g., "Genetic Algorithm", "Linear Programming").
  std::string optimizationAlgorithm;
  /// The result of the last optimization.
  std::string optimizationResult = "No optimization performed yet.";
};

}  // namespace Services

#endif  // DT_DMS_SERVICE_OPTIMIZATION_HPP
