// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Service_Diagnosis.hpp
/// \brief A diagnosis service `s ∈ S_diag`.

#ifndef DT_DMS_SERVICE_DIAGNOSIS_HPP
#define DT_DMS_SERVICE_DIAGNOSIS_HPP

#include <string>

#include "Services/DT_Service.hpp"

namespace Services {

/// \brief Class representing a Diagnosis Service (`s ∈ S_diag`) of the Digital Twin.
///
/// A Diagnosis Service identifies faults, anomalies or inefficiencies of the ToI from the data it consumes:
/// here, the values that fall outside a normal range.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Service_Diagnosis : public DT_Service<T> {

 public:
  /// \brief Constructor to initialize the Diagnosis Service with necessary attributes.
  /// \param serviceName        the name of the service.
  /// \param serviceDescription a brief description of the service.
  /// \param normalMinimum      the lower bound of the normal range.
  /// \param normalMaximum      the upper bound of the normal range.
  DT_Service_Diagnosis(std::string serviceName, std::string serviceDescription, double normalMinimum,
                       double normalMaximum)
      : DT_Service<T>(std::move(serviceName), std::move(serviceDescription)), normalMinimum(normalMinimum),
        normalMaximum(normalMaximum) {}

  /// \brief Executes the diagnosis logic: reports every consumed value outside the normal range.
  /// \return the diagnosis report.
  typename DT_Service<T>::Output executeService() override {
    if (this->consumedData.empty()) {
      diagnosisReport = "Diagnosis failed due to lack of data.";
      return diagnosisReport;
    }
    std::string report;
    for (std::size_t i = 0; i < this->consumedData.size(); ++i) {
      double v = Automaton::numericValue(this->consumedData[i]);
      if (v < normalMinimum || v > normalMaximum)
        report += "Anomaly at index " + std::to_string(i) + " with value " + Automaton::formatDouble(v) + ". ";
    }
    if (!report.empty()) report.pop_back();  // trailing space
    diagnosisReport = report.empty() ? "No anomalies detected. System functioning within normal parameters." : report;
    return diagnosisReport;
  }

  /// \return the report of the last diagnosis.
  const std::string& getDiagnosisReport() const { return diagnosisReport; }

 protected:
  /// The lower bound of the normal range.
  double normalMinimum;
  /// The upper bound of the normal range.
  double normalMaximum;
  /// The report with the results of the last diagnosis.
  std::string diagnosisReport = "No diagnosis performed yet.";
};

}  // namespace Services

#endif  // DT_DMS_SERVICE_DIAGNOSIS_HPP
