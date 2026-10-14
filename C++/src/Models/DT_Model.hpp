// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Model.hpp
/// \brief Abstract base class of the DT models `m ∈ M`.

#ifndef DT_DMS_MODEL_HPP
#define DT_DMS_MODEL_HPP

#include <algorithm>
#include <limits>
#include <memory>
#include <string>
#include <vector>

#include "Automaton/DT_Automaton.hpp"
#include "Data/DT_DataSegment.hpp"
#include "Mu/ObserverData.hpp"
#include "Mu/ObserverModel.hpp"

/// \brief The Models (`M`) of the DT-DMS framework.
///
/// `M` is the set of DT models that represent the ToI virtually: `M = M_desc ∪ M_pred ∪ M_presc`. Every model
/// belongs to the class of automata `𝒜` (`M ⊆ 𝒜`) and is specified by the 7-tuple `ℳ = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`.
/// - DT_Model: the abstract class, built on Automaton::DT_Automaton. It handles generic DT models.
/// - DT_Model_Descriptive: Class inherited from DT_Model. It describes what happened or is happening.
/// - DT_Model_Predictive: Class inherited from DT_Model. It predicts what is likely to happen.
/// - DT_Model_Prescriptive: Class inherited from DT_Model. It recommends what should be done.
namespace Models {

/// \brief Abstract base class representing a generic DT model `m ∈ M` of `DT = ⟨D, M, S, μ⟩`.
///
/// `M` is the set of DT models that represent the ToI virtually: `M = M_desc ∪ M_pred ∪ M_presc`, the
/// descriptive (DT_Model_Descriptive), predictive (DT_Model_Predictive) and prescriptive
/// (DT_Model_Prescriptive) models.
///
/// Every model belongs to the class of automata `𝒜` (`M ⊆ 𝒜`); hence this class extends
/// Automaton::DT_Automaton and inherits the 7-tuple `ℳ = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`. A model is updated by the
/// data assimilation function `μ` through assimilate(). This abstract class is the basis of implementation of
/// the Strategy design pattern.
///
/// A model is also the subject of the model observer pattern: the services coupled to it
/// (addServiceToBeUpdated()) are notified after each assimilation (notifyServices()).
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Model : public Automaton::DT_Automaton, public Mu::ObserverData<T> {

 public:
  /// \brief Constructor to initialize the model's name.
  /// \param modelName the name of the model.
  explicit DT_Model(std::string modelName) : modelName(std::move(modelName)) {}

  /// Destructor
  ~DT_Model() override = default;

  /// \brief Assimilates a data segment into this model: the model side of `μ(ω, m) = m'`.
  ///
  /// The model is updated in place, so `m'` keeps the type of `m`. By default each datum of `ω` is passed to
  /// update() in time order, then the coupled services are notified (notifyServices()); subclasses can
  /// override it (e.g., to re-train a predictive model on a whole historical segment) and should then call
  /// notifyServices() too.
  /// \param omega the data segment `ω`.
  virtual void assimilate(const Data::DT_DataSegment<T>& omega) {
    for (const auto& value : omega.getValues()) this->update(value.second);
    notifyServices();
  }

  /// Executes the core logic of the model. Each subclass provides its specific implementation.
  virtual void executeModel() = 0;

  /// \brief Returns the value of the model at time `t`, `m(t)`, to be compared with the datum `ω(t)` measured
  /// at the same time (`DT_DMS::checkModelsVersusData`).
  ///
  /// By default a model gives no value in time and this method returns `NaN`; a model that describes or
  /// predicts the measured signal (e.g., an interpolation) overrides it.
  /// \param t the time.
  /// \return `m(t)`, or `NaN` if the model gives no value at `t`.
  virtual double evaluate(double t) const {
    (void)t;
    return std::numeric_limits<double>::quiet_NaN();
  }

  // ---------------------------------------------------------------- model observer pattern (subject)

  /// \brief Adds a service to be notified each time this model is updated.
  ///
  /// The model keeps a weak reference: the service is owned by the Digital Twin, not by the model.
  /// \param service the service (or any model observer).
  void addServiceToBeUpdated(const std::shared_ptr<Mu::ObserverModel<T>>& service) {
    for (const auto& known : servicesToBeUpdated)
      if (known.lock() == service) return;
    servicesToBeUpdated.push_back(service);
  }

  /// \brief Stops notifying a service when this model is updated.
  /// \param service the service to remove.
  void removeServiceToBeUpdated(const std::shared_ptr<Mu::ObserverModel<T>>& service) {
    servicesToBeUpdated.erase(std::remove_if(servicesToBeUpdated.begin(), servicesToBeUpdated.end(),
                                             [&service](const auto& known) {
                                               auto locked = known.lock();
                                               return !locked || locked == service;
                                             }),
                              servicesToBeUpdated.end());
  }

  /// Notifies the services coupled to this model that it has been updated.
  void notifyServices() {
    // Safe copy to avoid issues if a service removes itself during the update
    std::vector<std::weak_ptr<Mu::ObserverModel<T>>> copy = servicesToBeUpdated;
    for (const auto& service : copy)
      if (auto locked = service.lock()) locked->updateWithModel(*this);
  }

  /// \return the number of services notified when this model is updated.
  std::size_t getNumberOfServicesToBeUpdated() const { return servicesToBeUpdated.size(); }

  /// \return the model name.
  const std::string& getModelName() const { return modelName; }
  /// \param value the new model name.
  void setModelName(const std::string& value) { modelName = value; }

  /// \brief Provides a string representation of the model, useful for debugging or logging.
  /// \return a string describing the model's name.
  virtual std::string toString() const { return "DT_Model [modelName=" + modelName + "]"; }

 protected:
  /// The name of the model, which uniquely identifies it.
  std::string modelName;

 private:
  /// The services to be notified when the model is updated (model observer pattern).
  std::vector<std::weak_ptr<Mu::ObserverModel<T>>> servicesToBeUpdated;
};

}  // namespace Models

#endif  // DT_DMS_MODEL_HPP
