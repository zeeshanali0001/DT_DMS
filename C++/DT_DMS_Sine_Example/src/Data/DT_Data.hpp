// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_Data.hpp
/// \brief A source of data of the ToI, a part of `D`.

#ifndef DT_DMS_DATA_HPP
#define DT_DMS_DATA_HPP

#include <algorithm>
#include <chrono>
#include <functional>
#include <memory>
#include <stdexcept>
#include <string>
#include <vector>

#include "Automaton/DT_Automaton.hpp"
#include "Data/DT_DataCharacterization.hpp"
#include "Data/DT_DataSegment.hpp"
#include "Mu/ObserverData.hpp"
#include "Mu/ObserverDataManager.hpp"

/// \brief The Data (`D`) of the DT-DMS framework.
///
/// `D` is the data collected from the ToI, either historical or real-time: `D = D_hist ∪ D_rt`. The
/// classification depends only on the measurement time: a datum `d = ω(t)` is real-time iff `t ≥ t_now − δ`,
/// and historical otherwise.
/// - DT_Data: a source of data of the ToI. It records every datum with its measurement time and notifies its
///   observers (the data assimilation function `μ`).
/// - DT_DataSegment: a measured data segment `ω : T_U ⇀ D`, classified at `t_now` as real-time, historical
///   or mixed.
/// - DT_DataCharacterization: the characterization of a data source (unit, signal type, data flow type,
///   sensor precision, location, calibration status and last maintenance date).
namespace Data {

/// The clock giving the current time `t_now` (in seconds by default).
using Clock = std::function<double()>;

/// \return the system time in seconds, the default clock.
inline double systemTimeInSeconds() {
  using namespace std::chrono;
  return duration_cast<duration<double>>(system_clock::now().time_since_epoch()).count();
}

/// \brief Class representing a source of data collected from the ToI, i.e., a part of `D` in
/// `DT = ⟨D, M, S, μ⟩`.
///
/// The collected data is either historical or real-time, `D = D_hist ∪ D_rt`. The classification depends only
/// on the measurement time: a datum `d = ω(t)` is real-time iff `t ≥ t_now − δ`, and historical otherwise,
/// where `t` is the measurement time, `t_now` the current time and `δ` the data scope during which data is
/// considered real-time.
///
/// Key features of the class include:
/// - Every datum is recorded with its measurement time in a segment `ω` (DT_DataSegment).
/// - getRealTimeData() and getHistoricalData() give `D_rt` and `D_hist` of the source.
/// - Its characterization (DT_DataCharacterization) tells how the data must be interpreted (unit, signal
///   type, sensor precision, calibration, ...).
/// - It is the subject of the observer pattern (Mu::ObserverDataManager): each new datum is pushed to the
///   registered observers, in particular to the data assimilation function `μ`.
/// \tparam T the type of a datum `d ∈ D`.
template <typename T>
class DT_Data : public Mu::ObserverDataManager<T> {

 public:
  /// \brief Constructs a data source.
  /// \param source   the origin of the data (e.g., sensor name or system name).
  /// \param dataType the type or category of the data (e.g., temperature).
  DT_Data(std::string source, std::string dataType) : source(std::move(source)), dataType(std::move(dataType)) {}

  /// Destructor
  ~DT_Data() override = default;

  // ---------------------------------------------------------------- observer pattern (subject)

  void registerObserver(std::shared_ptr<Mu::ObserverData<T>> observer) override {
    if (observer && std::find(observers.begin(), observers.end(), observer) == observers.end())
      observers.push_back(std::move(observer));
  }

  void removeObserver(const std::shared_ptr<Mu::ObserverData<T>>& observer) override {
    observers.erase(std::remove(observers.begin(), observers.end(), observer), observers.end());
  }

  void notifyObservers() override {
    // Safe copy to avoid issues if an observer removes itself during update
    std::vector<std::shared_ptr<Mu::ObserverData<T>>> copy = observers;
    for (const auto& observer : copy) observer->update(currentData);
  }

  // ---------------------------------------------------------------- data collection

  /// \brief Call this method whenever new data arrives; the datum is time-stamped with the current time `t_now`.
  /// \param newData the new datum.
  void updateData(const T& newData) { updateData(clock(), newData); }

  /// \brief Call this method whenever new data measured at time `t` arrives. The datum is recorded
  /// (`ω(t) = newData`) and the observers are notified.
  /// \param t       the measurement time `t ∈ T_U`.
  /// \param newData the new datum.
  /// \throw std::invalid_argument if `t > t_now`: no datum may lie in the future.
  void updateData(double t, const T& newData) {
    double tNow = clock();
    if (t > tNow)
      throw std::invalid_argument("No datum may lie in the future: t = " + Automaton::formatDouble(t) +
                                  " > t_now = " + Automaton::formatDouble(tNow));
    currentData = newData;
    currentTime = t;
    collectedData.put(t, newData);
    notifyObservers();
  }

  /// \return the latest datum.
  const T& getCurrentData() const { return currentData; }

  /// \return the measurement time of the latest datum.
  double getCurrentTime() const { return currentTime; }

  /// \return all data collected from this source, as a segment `ω`.
  const DT_DataSegment<T>& getCollectedData() const { return collectedData; }

  /// \param delta the data scope `δ`.
  /// \return `D_rt`: the collected data measured at `t ≥ t_now − δ`.
  DT_DataSegment<T> getRealTimeData(double delta) const { return collectedData.realTimePart(clock(), delta); }

  /// \param delta the data scope `δ`.
  /// \return `D_hist`: the collected data measured at `t < t_now − δ`.
  DT_DataSegment<T> getHistoricalData(double delta) const { return collectedData.historicalPart(clock(), delta); }

  /// \brief Replaces the clock giving `t_now` (set by the DT so that all its parts share the same time).
  /// \param newClock the new clock.
  void setClock(Clock newClock) { clock = std::move(newClock); }

  // ---------------------------------------------------------------- attributes

  /// \brief Validates the common attributes of the data source.
  /// \return true if the source and the data type are defined; false otherwise.
  bool validateData() const { return !source.empty() && !dataType.empty(); }

  /// \return the source of the data.
  const std::string& getSource() const { return source; }
  /// \param value the new source of the data.
  void setSource(const std::string& value) { source = value; }

  /// \return the type of the data.
  const std::string& getDataType() const { return dataType; }
  /// \param value the new type of the data.
  void setDataType(const std::string& value) { dataType = value; }

  /// \return the characterization of the data (unit, signal type, sensor precision, ...).
  DT_DataCharacterization& getCharacterization() { return characterization; }
  /// \return the characterization of the data (unit, signal type, sensor precision, ...).
  const DT_DataCharacterization& getCharacterization() const { return characterization; }
  /// \param value the new characterization of the data.
  void setCharacterization(const DT_DataCharacterization& value) { characterization = value; }

  /// \brief Converts the data source into a human-readable string format.
  /// \return a string with the source, the data type and the number of collected data.
  std::string toString() const {
    return "DT_Data{source='" + source + "', dataType='" + dataType +
           "', collected=" + std::to_string(collectedData.size()) + "}";
  }

 protected:
  /// The source of the data (e.g., "Sensor A", "API").
  std::string source;
  /// The type of the data (e.g., "Temperature", "Pressure").
  std::string dataType;
  /// The characterization of the data: unit, signal type, sensor precision, calibration, ...
  DT_DataCharacterization characterization;

 private:
  /// The observers interested in this data source.
  std::vector<std::shared_ptr<Mu::ObserverData<T>>> observers;
  /// The latest datum.
  T currentData{};
  /// The measurement time `t ∈ T_U` of the latest datum, i.e., `currentData = ω(currentTime)`.
  double currentTime = 0.0;
  /// All data collected from this source, as a segment `ω : T_U ⇀ D`.
  DT_DataSegment<T> collectedData;
  /// Gives the current time `t_now` (default: system time in seconds).
  Clock clock = systemTimeInSeconds;
};

}  // namespace Data

#endif  // DT_DMS_DATA_HPP
