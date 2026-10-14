// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Service.hpp
/// \brief Abstract base class of the DT services `s ∈ S`.

#ifndef DT_DMS_SERVICE_HPP
#define DT_DMS_SERVICE_HPP

#include <any>
#include <memory>
#include <string>
#include <vector>

#include "Automaton/DT_Automaton.hpp"
#include "Data/DT_DataSegment.hpp"
#include "Models/DT_Model.hpp"
#include "Mu/ObserverData.hpp"
#include "Mu/ObserverModel.hpp"

/// \brief The Services (`S`) of the DT-DMS framework.
///
/// `S` is the set of DT services: `S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont`. A service is parameterized
/// by a model and/or the data segments it consumes, `s : M × Ω_D → Y_s`. Every service belongs to the class of
/// automata `𝒜` (`S ⊆ 𝒜`) and is specified by the 7-tuple `𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`.
/// - DT_Service: the abstract class, built on Automaton::DT_Automaton. It handles generic DT services.
/// - DT_Service_Monitoring: Class inherited from DT_Service. It monitors the states of the ToI in real time.
/// - DT_Service_Prediction: Class inherited from DT_Service. It predicts the future states of the ToI.
/// - DT_Service_Diagnosis: Class inherited from DT_Service. It identifies the anomalies of the ToI.
/// - DT_Service_Optimization: Class inherited from DT_Service. It finds the best solution for the ToI.
/// - DT_Service_Control: Class inherited from DT_Service. It provides the feedback control on the ToI.
namespace Services {

/// \brief Abstract base class representing a generic DT service `s ∈ S` of `DT = ⟨D, M, S, μ⟩`.
///
/// `S` is the set of DT services: `S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont`, the monitoring
/// (DT_Service_Monitoring), prediction (DT_Service_Prediction), diagnosis (DT_Service_Diagnosis), optimization
/// (DT_Service_Optimization) and control (DT_Service_Control) services.
///
/// A service is parameterized by a model `m ∈ M` and/or the data segment(s) it consumes `ω ∈ Ω_D`:
/// `s : M × Ω_D → Y_s`, where `Y_s` is the output produced by `s` (execute()). Services are specified with the
/// same formalism as models because both belong to the class of automata `𝒜` (`S ⊆ 𝒜`); hence this class
/// extends Automaton::DT_Automaton and inherits the 7-tuple `𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`. This abstract class
/// is the basis of implementation of the Strategy design pattern.
///
/// A service observes the data (Mu::ObserverData) and the model it is coupled to (Mu::ObserverModel):
/// updateWithModel() is called each time `μ` has updated that model.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Service : public Automaton::DT_Automaton, public Mu::ObserverData<T>, public Mu::ObserverModel<T> {

 public:
  /// The output `y ∈ Y_s` of a service: a `std::string` report or a `double` value (see outputToString()).
  using Output = std::any;

  /// \brief Constructor to initialize the service with a name and an optional description.
  /// \param serviceName        the name of the service.
  /// \param serviceDescription a brief description of the service (empty for none).
  DT_Service(std::string serviceName, std::string serviceDescription)
      : serviceName(std::move(serviceName)),
        serviceDescription(serviceDescription.empty() ? "No description provided." : std::move(serviceDescription)) {}

  /// Destructor
  ~DT_Service() override = default;

  /// \brief Called when new data arrives: the datum is added to the data consumed by the service.
  /// \param data the new datum.
  void update(const T& data) override { consumedData.push_back(data); }

  /// \brief Called each time the model the service is coupled to has been updated by `μ`. By default it does
  /// nothing; a service that must react to every new version of its model (e.g., re-check an interpolation)
  /// overrides it.
  /// \param m the updated model `m'`.
  void updateWithModel(Models::DT_Model<T>& m) override { (void)m; }

  /// \brief Runs the service, `s(m, ω) ∈ Y_s`: couples it to `m`, consumes the data of `ω` in time order, then
  /// runs executeService().
  /// \param m     the model `m` (may be `nullptr` for a data-driven service).
  /// \param omega the data segment `ω` (may be `nullptr` for a model-driven service).
  /// \return the output of the service, an element of `Y_s`.
  Output execute(std::shared_ptr<Models::DT_Model<T>> m, const Data::DT_DataSegment<T>* omega) {
    model = std::move(m);
    if (omega != nullptr) {
      consumedData.clear();
      for (const auto& value : omega->getValues()) update(value.second);
    }
    return executeService();
  }

  /// \brief Executes the service logic on the coupled model and/or the consumed data.
  /// Each subclass defines the specific behaviour of the service.
  /// \return the output of the service, an element of `Y_s`.
  virtual Output executeService() = 0;

  /// \return the model `m` the service is coupled to, or `nullptr`.
  const std::shared_ptr<Models::DT_Model<T>>& getModel() const { return model; }
  /// \param m the model `m` to couple the service to.
  void setModel(std::shared_ptr<Models::DT_Model<T>> m) { model = std::move(m); }

  /// \return the name of the service.
  const std::string& getServiceName() const { return serviceName; }
  /// \param value the new name of the service.
  void setServiceName(const std::string& value) { serviceName = value; }

  /// \return the description of the service.
  const std::string& getServiceDescription() const { return serviceDescription; }
  /// \param value the new description of the service.
  void setServiceDescription(const std::string& value) { serviceDescription = value; }

  /// \return the data consumed by the service.
  const std::vector<T>& getConsumedData() const { return consumedData; }

  /// \return an output of a service as text.
  static std::string outputToString(const Output& output) { return Automaton::anyToString(output); }

  /// \brief Provides a string representation of the service.
  /// \return a string containing the service's name and description.
  virtual std::string toString() const {
    return "DT_Service [serviceName=" + serviceName + ", serviceDescription=" + serviceDescription + "]";
  }

 protected:
  /// The name of the service (e.g., "Temperature Monitoring").
  std::string serviceName;
  /// A description of the service, providing additional details about its purpose.
  std::string serviceDescription;
  /// The model `m ∈ M` the service is coupled to (`nullptr` for a purely data-driven service).
  std::shared_ptr<Models::DT_Model<T>> model;
  /// The data consumed by the service, in the order received.
  std::vector<T> consumedData;
};

}  // namespace Services

#endif  // DT_DMS_SERVICE_HPP
