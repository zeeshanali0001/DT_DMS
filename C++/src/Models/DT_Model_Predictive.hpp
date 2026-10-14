// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Model_Predictive.hpp
/// \brief A predictive model `m ∈ M_pred`.

#ifndef DT_DMS_MODEL_PREDICTIVE_HPP
#define DT_DMS_MODEL_PREDICTIVE_HPP

#include <algorithm>
#include <cmath>
#include <iostream>
#include <limits>
#include <string>
#include <vector>

#include "Models/DT_Model.hpp"

namespace Models {

/// \brief Represents a predictive model (`m ∈ M_pred`) of the Digital Twin.
///
/// A predictive model tells what is likely to happen in the ToI: it uses the historical and real-time data it
/// has assimilated to predict future states or behaviours.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Model_Predictive : public DT_Model<T> {

 public:
  /// \brief Constructor to initialize a predictive model with its attributes.
  /// \param modelName the name of the predictive model.
  /// \param algorithm the algorithm used for prediction.
  /// \param window    the number of most recent values used for a prediction (at least 1).
  DT_Model_Predictive(std::string modelName, std::string algorithm, int window)
      : DT_Model<T>(std::move(modelName)), algorithm(std::move(algorithm)), window(std::max(1, window)) {}

  /// \brief Updates the model with a new datum: numeric values are added to the history.
  /// \param updatedData the new datum.
  void update(const T& updatedData) override {
    double v = Automaton::numericValue(updatedData);
    if (!std::isnan(v)) history.push_back(v);
  }

  /// \brief Predicts the next value as the mean of the last #window values (a simple placeholder that
  /// subclasses can replace by a real algorithm).
  /// \return the predicted value, or `NaN` if no data has been assimilated.
  virtual double predict() const {
    if (history.empty()) return std::numeric_limits<double>::quiet_NaN();
    std::size_t from = history.size() > static_cast<std::size_t>(window) ? history.size() - window : 0;
    double sum = 0.0;
    for (std::size_t i = from; i < history.size(); ++i) sum += history[i];
    return sum / static_cast<double>(history.size() - from);
  }

  /// Executes the predictive model: prints its prediction.
  void executeModel() override {
    std::cout << "Executing predictive model: " << this->modelName << "\n"
              << "  Using algorithm: " << algorithm << " (window=" << window << ")\n"
              << "  Predicted value: " << Automaton::formatDouble(predict()) << std::endl;
  }

  /// \return the name of the algorithm.
  const std::string& getAlgorithm() const { return algorithm; }
  /// \param value the new algorithm name.
  void setAlgorithm(const std::string& value) { algorithm = value; }

  /// \return the numeric values assimilated by the model.
  const std::vector<double>& getHistory() const { return history; }

  /// \return the history as text: `[v1, v2, ...]`.
  std::string historyToString() const {
    std::string text = "[";
    for (std::size_t i = 0; i < history.size(); ++i) text += (i > 0 ? ", " : "") + Automaton::formatDouble(history[i]);
    return text + "]";
  }

  /// \brief Provides a string representation of the predictive model, including its attributes.
  /// \return a string describing the predictive model.
  std::string toString() const override {
    return "DT_Model_Predictive [modelName=" + this->modelName + ", algorithm=" + algorithm +
           ", window=" + std::to_string(window) + ", assimilated=" + std::to_string(history.size()) + "]";
  }

 protected:
  /// The algorithm used for making predictions (e.g., "Moving average", "Linear Regression").
  std::string algorithm;
  /// The number of most recent values used for a prediction.
  int window;
  /// The numeric values assimilated by the model, in assimilation order.
  std::vector<double> history;
};

}  // namespace Models

#endif  // DT_DMS_MODEL_PREDICTIVE_HPP
