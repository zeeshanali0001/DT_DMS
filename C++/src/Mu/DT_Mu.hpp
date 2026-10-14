// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Mu.hpp
/// \brief The model inference or data assimilation function `μ : Ω_D × M → M`.

#ifndef DT_DMS_MU_HPP
#define DT_DMS_MU_HPP

#include <algorithm>
#include <memory>
#include <stdexcept>
#include <vector>

#include "Data/DT_Data.hpp"
#include "Data/DT_DataSegment.hpp"
#include "Models/DT_Model.hpp"
#include "Mu/ObserverData.hpp"

/// \brief The model inference or data assimilation function (`μ`) of the DT-DMS framework.
///
/// `μ : Ω_D × M → M` updates a model with a data segment: `μ(ω, m) = m'`, where `m'` keeps the type of `m`.
/// Real-time data is assimilated synchronously and historical data asynchronously; a mixed segment is split
/// into `ω_hist ⊕ ω_rt` or assimilated as a whole.
/// - DT_Mu: the data assimilation function. It observes the data sources and updates the models.
/// - ObserverData: the observer of the data observer pattern (models, services and `μ`).
/// - ObserverDataManager: the subject of the data observer pattern (the data sources).
/// - ObserverModel: the observer of the model observer pattern (the services notified when the model they
///   are coupled to is updated).
namespace Mu {

/// \brief Class representing the model inference or data assimilation function `μ : Ω_D × M → M` of
/// `DT = ⟨D, M, S, μ⟩`.
///
/// Given a segment `ω ∈ Ω_D` and a model `m ∈ M`, `μ(ω, m) = m'` is the model `m` updated with `ω`. The model
/// is updated in place, so `m'` is the same object as `m` and keeps the type of `m`
/// (e.g., `m ∈ M_pred ⇒ m' ∈ M_pred`).
///
/// Key features of the class include:
/// - **Data observer pattern**: it observes the data sources (ObserverDataManager) through one ObserverData
///   per source; each new datum of the ToI is turned into a segment `ω`.
/// - **Synchronous assimilation** of real-time segments (`Ω_D^rt`): applied at once.
/// - **Asynchronous assimilation** of historical segments (`Ω_D^hist`): queued until flush().
/// - **Mixed segments** (`Ω_D^mix`) are handled according to the MixedPolicy: split into `ω_hist ⊕ ω_rt` and
///   assimilated part by part (default), or assimilated as a whole like a real-time or like a historical
///   segment.
///
/// `μ` must be owned by a `std::shared_ptr` (it is created by `DT_DMS`), because each data source it
/// observes keeps a weak reference to it.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Mu : public std::enable_shared_from_this<DT_Mu<T>> {

 public:
  /// How a mixed segment `ω ∈ Ω_D^mix(t_now)` is assimilated, depending on the DT requirements.
  enum class MixedPolicy {
    SPLIT,         ///< Part by part: `ω_rt` synchronously and `ω_hist` asynchronously.
    AS_REAL_TIME,  ///< As a whole, synchronously, like a real-time segment.
    AS_HISTORICAL  ///< As a whole, asynchronously, like a historical segment.
  };

  /// \brief Constructs the data assimilation function.
  /// \param delta the freshness threshold `δ`.
  /// \param clock gives the current time `t_now`.
  /// \throw std::invalid_argument if `δ < 0`.
  DT_Mu(double delta, Data::Clock clock) : delta(delta), clock(std::move(clock)) {
    if (delta < 0) throw std::invalid_argument("δ must be in ℝ+.");
  }

  // ---------------------------------------------------------------- what μ observes and updates

  /// \brief Adds a model to the set of models updated by `μ`.
  /// \param m the model.
  void addModel(const std::shared_ptr<Models::DT_Model<T>>& m) {
    if (std::find(models.begin(), models.end(), m) == models.end()) models.push_back(m);
  }

  /// \brief Starts observing a data source: every new datum of `source` will be assimilated.
  /// \param source the data source.
  void observe(const std::shared_ptr<Data::DT_Data<T>>& source) {
    if (std::find(sources.begin(), sources.end(), source) == sources.end()) {
      sources.push_back(source);
      source->registerObserver(std::make_shared<Assimilator>(this->weak_from_this(), source.get()));
    }
  }

  // ---------------------------------------------------------------- μ

  /// \brief The data assimilation function `μ(ω, m) = m'`.
  /// \param omega the data segment `ω`.
  /// \param m     the model `m`.
  /// \return the updated model `m'` (same object and type as `m`).
  Models::DT_Model<T>& mu(const Data::DT_DataSegment<T>& omega, Models::DT_Model<T>& m) {
    m.assimilate(omega);
    return m;
  }

  /// \brief Assimilates a segment according to its kind at `t_now`: real-time → synchronously,
  /// historical → asynchronously, mixed → according to the MixedPolicy.
  /// \param omega the data segment `ω`.
  /// \throw std::invalid_argument if `ω` is empty or has a datum in the future.
  void assimilate(const Data::DT_DataSegment<T>& omega) {
    double tNow = clock();
    switch (omega.kind(tNow, delta)) {
      case Data::DT_DataSegment<T>::Kind::REAL_TIME:
        assimilateSynchronously(omega);
        break;
      case Data::DT_DataSegment<T>::Kind::HISTORICAL:
        assimilateAsynchronously(omega);
        break;
      case Data::DT_DataSegment<T>::Kind::MIXED:
        switch (mixedPolicy) {
          case MixedPolicy::SPLIT:
            // ω = ω_hist ⊕ ω_rt, split at t_now − δ
            assimilateAsynchronously(omega.historicalPart(tNow, delta));
            assimilateSynchronously(omega.realTimePart(tNow, delta));
            break;
          case MixedPolicy::AS_REAL_TIME:
            assimilateSynchronously(omega);
            break;
          case MixedPolicy::AS_HISTORICAL:
            assimilateAsynchronously(omega);
            break;
        }
        break;
    }
  }

  /// \brief Synchronous assimilation: applies `μ(ω, m)` at once to every model `m ∈ M`.
  /// \param omega the data segment `ω`.
  void assimilateSynchronously(const Data::DT_DataSegment<T>& omega) {
    for (const auto& m : models) mu(omega, *m);
  }

  /// \brief Asynchronous assimilation: queues the segment; `μ` is applied when flush() is called.
  /// \param omega the data segment `ω`.
  void assimilateAsynchronously(const Data::DT_DataSegment<T>& omega) { pending.push_back(omega); }

  /// Applies `μ` to all queued segments, in the order they were queued.
  void flush() {
    std::vector<Data::DT_DataSegment<T>> toAssimilate;
    toAssimilate.swap(pending);
    for (const auto& omega : toAssimilate) assimilateSynchronously(omega);
  }

  // ---------------------------------------------------------------- accessors

  /// \return the freshness threshold `δ`.
  double getDelta() const { return delta; }
  /// \return the number of segments waiting for flush().
  std::size_t getPendingCount() const { return pending.size(); }
  /// \return how mixed segments are assimilated.
  MixedPolicy getMixedPolicy() const { return mixedPolicy; }
  /// \param policy how mixed segments are to be assimilated.
  void setMixedPolicy(MixedPolicy policy) { mixedPolicy = policy; }
  /// \return the models updated by `μ`.
  const std::vector<std::shared_ptr<Models::DT_Model<T>>>& getModels() const { return models; }
  /// \return the observed data sources.
  const std::vector<std::shared_ptr<Data::DT_Data<T>>>& getSources() const { return sources; }

 private:
  /// \brief Observer registered on one data source: forwards every new datum of that source to `μ`.
  class Assimilator : public ObserverData<T> {
   public:
    /// \param mu     the data assimilation function (weak reference: `μ` owns nothing of the source).
    /// \param source the observed data source (it owns this observer, so it outlives it).
    Assimilator(std::weak_ptr<DT_Mu<T>> mu, const Data::DT_Data<T>* source) : mu(std::move(mu)), source(source) {}

    /// Called when new data arrives from #source: the datum `d = ω(t)` is assimilated.
    void update(const T& updatedData) override {
      if (auto locked = mu.lock()) locked->assimilate(Data::DT_DataSegment<T>(source->getCurrentTime(), updatedData));
    }

   private:
    /// The data assimilation function.
    std::weak_ptr<DT_Mu<T>> mu;
    /// The observed data source.
    const Data::DT_Data<T>* source;
  };

  /// The freshness threshold `δ ∈ ℝ+`: the data scope during which data is considered real-time.
  double delta;
  /// Gives the current time `t_now ∈ T_U`.
  Data::Clock clock;
  /// The models `M` updated by `μ`.
  std::vector<std::shared_ptr<Models::DT_Model<T>>> models;
  /// The observed data sources.
  std::vector<std::shared_ptr<Data::DT_Data<T>>> sources;
  /// Segments waiting for asynchronous assimilation.
  std::vector<Data::DT_DataSegment<T>> pending;
  /// How mixed segments are assimilated.
  MixedPolicy mixedPolicy = MixedPolicy::SPLIT;
};

}  // namespace Mu

#endif  // DT_DMS_MU_HPP
