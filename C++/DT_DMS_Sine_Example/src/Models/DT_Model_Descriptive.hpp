// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Model_Descriptive.hpp
/// \brief A descriptive model `m ∈ M_desc`.

#ifndef DT_DMS_MODEL_DESCRIPTIVE_HPP
#define DT_DMS_MODEL_DESCRIPTIVE_HPP

#include <algorithm>
#include <cmath>
#include <iostream>
#include <limits>
#include <string>

#include "Models/DT_Model.hpp"

namespace Models {

/// \brief Represents a descriptive model (`m ∈ M_desc`) of the Digital Twin.
///
/// A descriptive model describes what happened or what is happening in the ToI: it summarizes and interprets
/// the historical and real-time data it has assimilated.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Model_Descriptive : public DT_Model<T> {

 public:
  /// \brief Constructor to initialize a descriptive model with its attributes.
  /// \param modelName  the name of the descriptive model.
  /// \param dataSource the source of the data (e.g., "Sensor A", "Database X").
  DT_Model_Descriptive(std::string modelName, std::string dataSource)
      : DT_Model<T>(std::move(modelName)), dataSource(std::move(dataSource)) {}

  /// \brief Updates the description with a new datum: counts it and, if it is a number, updates the
  /// minimum, maximum and mean.
  /// \param updatedData the new datum.
  void update(const T& updatedData) override {
    dataPointCount++;
    double v = Automaton::numericValue(updatedData);
    if (!std::isnan(v)) {
      minimum = std::min(minimum, v);
      maximum = std::max(maximum, v);
      sum += v;
      dataSummary = "min=" + Automaton::formatDouble(minimum) + ", max=" + Automaton::formatDouble(maximum) +
                    ", mean=" + Automaton::formatDouble(getMean());
    } else {
      dataSummary = "last=" + Automaton::valueToString(updatedData);
    }
  }

  /// Executes the descriptive model: prints the description of the data assimilated so far.
  void executeModel() override {
    std::cout << "Executing descriptive model: " << this->modelName << "\n"
              << "  Data source: " << dataSource << "\n"
              << "  Number of data points analyzed: " << dataPointCount << "\n"
              << "  Data summary: " << dataSummary << std::endl;
  }

  /// \return the mean of the numeric values assimilated (`NaN` if none).
  double getMean() const {
    return dataPointCount == 0 ? std::numeric_limits<double>::quiet_NaN() : sum / dataPointCount;
  }

  /// \return the source of the data.
  const std::string& getDataSource() const { return dataSource; }

  /// \return the total number of data points assimilated.
  int getDataPointCount() const { return dataPointCount; }

  /// \return the summary of the data assimilated.
  const std::string& getDataSummary() const { return dataSummary; }

  /// \brief Provides a string representation of the descriptive model, including its attributes.
  /// \return a string describing the descriptive model.
  std::string toString() const override {
    return "DT_Model_Descriptive [modelName=" + this->modelName + ", dataSource=" + dataSource +
           ", dataPointCount=" + std::to_string(dataPointCount) + ", dataSummary=" + dataSummary + "]";
  }

 protected:
  /// The source of the data described by the model.
  std::string dataSource;
  /// The total number of data points assimilated by the model.
  int dataPointCount = 0;
  /// A summary of the data assimilated by the model.
  std::string dataSummary = "no data";
  /// The smallest numeric value assimilated.
  double minimum = std::numeric_limits<double>::infinity();
  /// The largest numeric value assimilated.
  double maximum = -std::numeric_limits<double>::infinity();
  /// The sum of the numeric values assimilated (used for the mean).
  double sum = 0.0;
};

}  // namespace Models

#endif  // DT_DMS_MODEL_DESCRIPTIVE_HPP
