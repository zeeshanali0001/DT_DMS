// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_DataSegment.hpp
/// \brief A measured data segment `ω ∈ Ω_D`.

#ifndef DT_DMS_DATA_SEGMENT_HPP
#define DT_DMS_DATA_SEGMENT_HPP

#include <limits>
#include <map>
#include <stdexcept>
#include <string>
#include <vector>

#include "Automaton/DT_Automaton.hpp"

namespace Data {

/// \brief Class representing a measured data segment `ω ∈ Ω_D` of the Digital Twin.
///
/// A data segment is a partial function `ω : T_U ⇀ D` from universal time `T_U` to data, with
/// `Dom(ω) ⊆ T_U` and `Im(ω) ⊆ D`. Time is a `double` (e.g., seconds). Since a segment is measured, no datum
/// lies in the future: `sup Dom(ω) ≤ t_now`.
///
/// With `t_now` the current time and `δ` the freshness threshold (data scope), a segment is:
/// - **real-time**, `ω ∈ Ω_D^rt(t_now)`, iff `inf Dom(ω) ≥ t_now − δ ∧ sup Dom(ω) ≤ t_now`;
/// - **historical**, `ω ∈ Ω_D^hist(t_now)`, iff `sup Dom(ω) < t_now − δ`;
/// - **mixed**, `ω ∈ Ω_D^mix(t_now)`, iff `inf Dom(ω) < t_now − δ ∧ t_now − δ ≤ sup Dom(ω) ≤ t_now`.
///
/// so that `Ω_D = Ω_D^rt(t_now) ∪ Ω_D^hist(t_now) ∪ Ω_D^mix(t_now)`. A mixed segment can be split at
/// `t_now − δ` into `ω = ω_hist ⊕ ω_rt` with historicalPart() and realTimePart().
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_DataSegment {

 public:
  /// The kind of a segment at `t_now`: the set of `Ω_D` it belongs to.
  enum class Kind {
    REAL_TIME,   ///< `ω ∈ Ω_D^rt(t_now)`: all its data are real-time.
    HISTORICAL,  ///< `ω ∈ Ω_D^hist(t_now)`: all its data are historical.
    MIXED        ///< `ω ∈ Ω_D^mix(t_now)`: it contains both kinds of data.
  };

  /// \return the name of a kind: `REAL_TIME`, `HISTORICAL` or `MIXED`.
  static std::string kindName(Kind kind) {
    switch (kind) {
      case Kind::REAL_TIME: return "REAL_TIME";
      case Kind::HISTORICAL: return "HISTORICAL";
      default: return "MIXED";
    }
  }

  /// Creates the empty segment (`Dom(ω) = ∅`).
  DT_DataSegment() = default;

  /// \brief Creates a segment with one datum, `ω(t) = d`.
  /// \param t the measurement time.
  /// \param d the datum.
  DT_DataSegment(double t, const T& d) { values[t] = d; }

  /// \brief Adds the datum `d` measured at time `t`, i.e., sets `ω(t) = d`.
  /// \param t the measurement time.
  /// \param d the datum.
  void put(double t, const T& d) { values[t] = d; }

  /// \param t a time.
  /// \return a pointer to `ω(t)`, or `nullptr` if `t ∉ Dom(ω)`.
  const T* get(double t) const {
    auto it = values.find(t);
    return it == values.end() ? nullptr : &it->second;
  }

  /// \return `Dom(ω)`, in increasing order.
  std::vector<double> getDomain() const {
    std::vector<double> domain;
    for (const auto& value : values) domain.push_back(value.first);
    return domain;
  }

  /// \return `Im(ω)`, in time order.
  std::vector<T> getImage() const {
    std::vector<T> image;
    for (const auto& value : values) image.push_back(value.second);
    return image;
  }

  /// \return the pairs `(t, ω(t))`, ordered by time.
  const std::map<double, T>& getValues() const { return values; }

  /// \return `true` if `Dom(ω) = ∅`.
  bool isEmpty() const { return values.empty(); }

  /// \return the number of data of the segment.
  std::size_t size() const { return values.size(); }

  /// \return `inf Dom(ω)`: the time of the oldest datum (`+∞` for the empty segment).
  double infDom() const { return values.empty() ? std::numeric_limits<double>::infinity() : values.begin()->first; }

  /// \return `sup Dom(ω)`: the time of the newest datum (`−∞` for the empty segment).
  double supDom() const { return values.empty() ? -std::numeric_limits<double>::infinity() : values.rbegin()->first; }

  /// \param tNow  the current time `t_now`.
  /// \param delta the freshness threshold `δ`.
  /// \return `true` iff `inf Dom(ω) ≥ t_now − δ ∧ sup Dom(ω) ≤ t_now`.
  bool isRealTime(double tNow, double delta) const {
    return !isEmpty() && infDom() >= tNow - delta && supDom() <= tNow;
  }

  /// \param tNow  the current time `t_now`.
  /// \param delta the freshness threshold `δ`.
  /// \return `true` iff `sup Dom(ω) < t_now − δ`.
  bool isHistorical(double tNow, double delta) const { return !isEmpty() && supDom() < tNow - delta; }

  /// \param tNow  the current time `t_now`.
  /// \param delta the freshness threshold `δ`.
  /// \return `true` iff `inf Dom(ω) < t_now − δ ∧ t_now − δ ≤ sup Dom(ω) ≤ t_now`.
  bool isMixed(double tNow, double delta) const {
    return !isEmpty() && infDom() < tNow - delta && tNow - delta <= supDom() && supDom() <= tNow;
  }

  /// \brief Classifies the segment at `t_now`.
  /// \param tNow  the current time `t_now`.
  /// \param delta the freshness threshold `δ`.
  /// \return the kind of the segment: real-time, historical or mixed.
  /// \throw std::invalid_argument if the segment is empty or has a datum in the future
  /// (`sup Dom(ω) > t_now`), i.e., it is not an admissible measured segment.
  Kind kind(double tNow, double delta) const {
    if (isEmpty()) throw std::invalid_argument("The empty segment cannot be classified.");
    if (supDom() > tNow)
      throw std::invalid_argument("No datum may lie in the future: sup Dom(ω) = " +
                                  Automaton::formatDouble(supDom()) + " > t_now = " + Automaton::formatDouble(tNow));
    if (isRealTime(tNow, delta)) return Kind::REAL_TIME;
    return isHistorical(tNow, delta) ? Kind::HISTORICAL : Kind::MIXED;
  }

  /// \param tNow  the current time `t_now`.
  /// \param delta the freshness threshold `δ`.
  /// \return `ω_rt`: `ω` restricted to the times `t ≥ t_now − δ`.
  DT_DataSegment<T> realTimePart(double tNow, double delta) const {
    DT_DataSegment<T> part;
    part.values.insert(values.lower_bound(tNow - delta), values.end());
    return part;
  }

  /// \param tNow  the current time `t_now`.
  /// \param delta the freshness threshold `δ`.
  /// \return `ω_hist`: `ω` restricted to the times `t < t_now − δ`.
  DT_DataSegment<T> historicalPart(double tNow, double delta) const {
    DT_DataSegment<T> part;
    part.values.insert(values.begin(), values.lower_bound(tNow - delta));
    return part;
  }

  /// \brief Concatenation `ω1 ⊕ ω2` (`this` is `ω1`) of two segments whose domains follow each other in time:
  /// the segment that equals `ω1` on `Dom(ω1)` and `ω2` on `Dom(ω2)`.
  /// \param next the segment `ω2`.
  /// \return the new segment `ω1 ⊕ ω2`.
  /// \throw std::invalid_argument if some time of `Dom(ω1)` does not precede every time of `Dom(ω2)`.
  DT_DataSegment<T> concat(const DT_DataSegment<T>& next) const {
    if (!isEmpty() && !next.isEmpty() && supDom() >= next.infDom())
      throw std::invalid_argument("ω1 ⊕ ω2 requires Dom(ω1) to precede Dom(ω2).");
    DT_DataSegment<T> result = *this;
    result.values.insert(next.values.begin(), next.values.end());
    return result;
  }

  /// \brief Converts the segment into a human-readable string format.
  /// \return the pairs `ω{t=ω(t), ...}`.
  std::string toString() const {
    std::string text = "ω{";
    for (auto it = values.begin(); it != values.end(); ++it) {
      if (it != values.begin()) text += ", ";
      text += Automaton::formatDouble(it->first) + "=" + Automaton::valueToString(it->second);
    }
    return text + "}";
  }

 private:
  /// The pairs `(t, ω(t))`, ordered by time.
  std::map<double, T> values;
};

}  // namespace Data

#endif  // DT_DMS_DATA_SEGMENT_HPP
