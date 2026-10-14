// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Model_Prescriptive.hpp
/// \brief A prescriptive model `m ∈ M_presc`.

#ifndef DT_DMS_MODEL_PRESCRIPTIVE_HPP
#define DT_DMS_MODEL_PRESCRIPTIVE_HPP

#include <cmath>
#include <iostream>
#include <limits>
#include <string>

#include "Models/DT_Model.hpp"

namespace Models {

/// \brief Represents a prescriptive model (`m ∈ M_presc`) of the Digital Twin.
///
/// A prescriptive model tells what should be done on the ToI: from the data it has assimilated, it recommends
/// the action to take in order to reach an objective.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Model_Prescriptive : public DT_Model<T> {

 public:
  /// \brief Constructor to initialize a prescriptive model with its attributes.
  /// \param modelName   the name of the prescriptive model.
  /// \param objective   the primary goal or objective of the model.
  /// \param target      the target value of the objective.
  /// \param assumptions the assumptions considered in the model.
  DT_Model_Prescriptive(std::string modelName, std::string objective, double target, std::string assumptions)
      : DT_Model<T>(std::move(modelName)), objective(std::move(objective)), target(target),
        assumptions(std::move(assumptions)) {}

  /// \brief Updates the model with a new datum: keeps the latest numeric value.
  /// \param updatedData the new datum.
  void update(const T& updatedData) override {
    double v = Automaton::numericValue(updatedData);
    if (!std::isnan(v)) lastValue = v;
  }

  /// \brief Recommends the action to take to reach the objective, from the latest value (a simple placeholder
  /// that subclasses can replace by a real decision model).
  /// \return the recommended action.
  virtual std::string recommend() const {
    if (std::isnan(lastValue)) return "No data assimilated: no recommendation.";
    double gap = target - lastValue;
    if (gap > 0)
      return "Increase by " + Automaton::formatDouble(gap) + " to reach the target " + Automaton::formatDouble(target) + ".";
    return gap < 0 ? "Decrease by " + Automaton::formatDouble(-gap) + " to reach the target " +
                         Automaton::formatDouble(target) + "."
                   : "Target " + Automaton::formatDouble(target) + " reached: no action.";
  }

  /// Executes the prescriptive model: prints its recommendation.
  void executeModel() override {
    std::cout << "Executing prescriptive model: " << this->modelName << "\n"
              << "  Objective: " << objective << "\n"
              << "  Assumptions: " << assumptions << "\n"
              << "  Recommendation: " << recommend() << std::endl;
  }

  /// \return the objective of the model.
  const std::string& getObjective() const { return objective; }

  /// \return the target value of the objective.
  double getTarget() const { return target; }
  /// \param value the new target value of the objective.
  void setTarget(double value) { target = value; }

  /// \return the assumptions of the model.
  const std::string& getAssumptions() const { return assumptions; }

  /// \brief Provides a string representation of the prescriptive model, including its attributes.
  /// \return a string describing the prescriptive model.
  std::string toString() const override {
    return "DT_Model_Prescriptive [modelName=" + this->modelName + ", objective=" + objective +
           ", target=" + Automaton::formatDouble(target) + ", lastValue=" + Automaton::formatDouble(lastValue) + "]";
  }

 protected:
  /// The goal or objective of the prescriptive model (e.g., "Keep temperature at 22").
  std::string objective;
  /// The target value of the objective.
  double target;
  /// The latest numeric value assimilated by the model.
  double lastValue = std::numeric_limits<double>::quiet_NaN();
  /// Any assumptions made while developing or executing the prescriptive model.
  std::string assumptions;
};

}  // namespace Models

#endif  // DT_DMS_MODEL_PRESCRIPTIVE_HPP
