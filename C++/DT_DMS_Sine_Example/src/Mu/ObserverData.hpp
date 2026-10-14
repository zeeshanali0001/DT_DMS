// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file ObserverData.hpp
/// \brief Observer of the data observer pattern.

#ifndef DT_DMS_OBSERVER_DATA_HPP
#define DT_DMS_OBSERVER_DATA_HPP

namespace Mu {

/// \brief Observer of the data observer pattern: an object that wants to be told when new data arrives.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class ObserverData {
 public:
  /// Destructor
  virtual ~ObserverData() = default;

  /// \brief Called when new data of type T arrives.
  /// \param updatedData the new datum.
  virtual void update(const T& updatedData) = 0;
};

}  // namespace Mu

#endif  // DT_DMS_OBSERVER_DATA_HPP
