// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file ObserverDataManager.hpp
/// \brief Subject of the data observer pattern.

#ifndef DT_DMS_OBSERVER_DATA_MANAGER_HPP
#define DT_DMS_OBSERVER_DATA_MANAGER_HPP

#include <memory>

#include "Mu/ObserverData.hpp"

namespace Mu {

/// \brief Subject of the data observer pattern: an object that produces data and manages its observers.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class ObserverDataManager {
 public:
  /// Destructor
  virtual ~ObserverDataManager() = default;

  /// \brief Registers an observer; it will be notified of every new datum.
  /// \param observer the observer to add.
  virtual void registerObserver(std::shared_ptr<ObserverData<T>> observer) = 0;

  /// \brief Removes an observer.
  /// \param observer the observer to remove.
  virtual void removeObserver(const std::shared_ptr<ObserverData<T>>& observer) = 0;

  /// Notifies all registered observers of the latest datum.
  virtual void notifyObservers() = 0;
};

}  // namespace Mu

#endif  // DT_DMS_OBSERVER_DATA_MANAGER_HPP
