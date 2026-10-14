// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Service_Prediction.hpp
/// \brief A prediction service `s ∈ S_pred`.

#ifndef DT_DMS_SERVICE_PREDICTION_HPP
#define DT_DMS_SERVICE_PREDICTION_HPP

#include <cmath>
#include <limits>
#include <memory>
#include <string>

#include "Models/DT_Model_Predictive.hpp"
#include "Services/DT_Service.hpp"

namespace Services {

/// \brief Class representing a Prediction Service (`s ∈ S_pred`) of the Digital Twin.
///
/// A Prediction Service forecasts future states of the ToI. It uses the predictive model it is coupled to;
/// without such a model it falls back to the mean of the data it has consumed.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Service_Prediction : public DT_Service<T> {

 public:
  /// \brief Constructor to initialize the Prediction Service.
  /// \param serviceName        the name of the service.
  /// \param serviceDescription a brief description of the service.
  DT_Service_Prediction(std::string serviceName, std::string serviceDescription)
      : DT_Service<T>(std::move(serviceName), std::move(serviceDescription)) {}

  /// \brief Executes the prediction logic: asks the coupled predictive model, or else averages the consumed data.
  /// \return the predicted value (a `double`; `NaN` if nothing can be predicted).
  typename DT_Service<T>::Output executeService() override {
    if (auto predictive = std::dynamic_pointer_cast<Models::DT_Model_Predictive<T>>(this->model)) {
      predictedValue = predictive->predict();
    } else {
      double sum = 0.0;
      int count = 0;
      for (const T& d : this->consumedData) {
        double v = Automaton::numericValue(d);
        if (!std::isnan(v)) {
          sum += v;
          count++;
        }
      }
      predictedValue = count == 0 ? std::numeric_limits<double>::quiet_NaN() : sum / count;
    }
    return predictedValue;
  }

  /// \return the last predicted value.
  double getPredictedValue() const { return predictedValue; }

 protected:
  /// The predicted value, computed by executeService() (`NaN` if none).
  double predictedValue = std::numeric_limits<double>::quiet_NaN();
};

}  // namespace Services

#endif  // DT_DMS_SERVICE_PREDICTION_HPP
