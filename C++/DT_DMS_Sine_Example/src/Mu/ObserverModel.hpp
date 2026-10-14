// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file ObserverModel.hpp
/// \brief Observer of the model observer pattern.

#ifndef DT_DMS_OBSERVER_MODEL_HPP
#define DT_DMS_OBSERVER_MODEL_HPP

namespace Models {
template <typename T>
class DT_Model;
}

namespace Mu {

/// \brief Observer of the model observer pattern: an object (typically a service `s ∈ S`) that wants to be
/// told when a model `m ∈ M` has been updated by `μ`.
///
/// The subject is the model (`DT_Model::addServiceToBeUpdated`); after each assimilation `μ(ω, m) = m'`,
/// the model calls updateWithModel() on its observers.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class ObserverModel {
 public:
  /// Destructor
  virtual ~ObserverModel() = default;

  /// \brief Called when the model `m` has been updated.
  /// \param m the updated model `m'`.
  virtual void updateWithModel(Models::DT_Model<T>& m) = 0;
};

}  // namespace Mu

#endif  // DT_DMS_OBSERVER_MODEL_HPP
