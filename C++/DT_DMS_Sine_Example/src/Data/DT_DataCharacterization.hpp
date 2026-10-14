// DT-DMS framework (C++) - Copyright (C) 2025 : Delos Vincent, ALI Zeeshan, Mamadou Kaba TRAORÉ
// Licensed under the GNU General Public License v3 or later (see License.txt).
/// \file DT_DataCharacterization.hpp
/// \brief The characterization of a data source of the ToI.

#ifndef DT_DMS_DATA_CHARACTERIZATION_HPP
#define DT_DMS_DATA_CHARACTERIZATION_HPP

#include <string>

namespace Data {

/// \brief Class representing the characterization of a data source of the ToI: the metadata that tell how
/// the data of `D` were produced and how they must be interpreted.
///
/// Every DT_Data holds one characterization. It is used by the Digital Twin to check the data characteristics
/// (formats, units, ...) with respect to the services it provides (`DT_DMS::checkDataVersusServices`).
///
/// The characteristics are:
/// - the unit of measurement (e.g., Celsius, Pascal, meter);
/// - the operational status of the system providing the data (e.g., active, inactive);
/// - the signal type: analog (continuous) or digital (discrete);
/// - the data flow type: event-triggered, continuous or periodic;
/// - the precision (level of tolerance error) of the sensor;
/// - the physical location of the sensor;
/// - the calibration status of the sensor (e.g., calibrated, not_calibrated);
/// - the date of the last maintenance performed on the sensor.
///
/// A characteristic that is not known is the empty string.
class DT_DataCharacterization {

 public:
  /// \return the unit of measurement for the data, e.g., Celsius, Pascal, meter.
  const std::string& getUnit() const { return unit; }
  /// \param value the unit of measurement for the data.
  void setUnit(const std::string& value) { unit = value; }

  /// \return the current operational status of the system providing the data, e.g., active, inactive.
  const std::string& getSystemStatus() const { return systemStatus; }
  /// \param value the operational status of the system providing the data.
  void setSystemStatus(const std::string& value) { systemStatus = value; }

  /// \return the signal type: analog (continuous) or digital (discrete).
  const std::string& getSignalType() const { return signalType; }
  /// \param value the signal type: analog or digital.
  void setSignalType(const std::string& value) { signalType = value; }

  /// \return the data flow type: event-triggered, continuous or periodic.
  const std::string& getDataFlowType() const { return dataFlowType; }
  /// \param value the data flow type: event-triggered, continuous or periodic.
  void setDataFlowType(const std::string& value) { dataFlowType = value; }

  /// \return the level of tolerance error of the sensor.
  const std::string& getSensorPrecision() const { return sensorPrecision; }
  /// \param value the level of tolerance error of the sensor.
  void setSensorPrecision(const std::string& value) { sensorPrecision = value; }

  /// \return the physical location of the sensor.
  const std::string& getPhysicalLocationOfTheSensor() const { return physicalLocationOfTheSensor; }
  /// \param value the physical location of the sensor.
  void setPhysicalLocationOfTheSensor(const std::string& value) { physicalLocationOfTheSensor = value; }

  /// \return whether the sensor has been calibrated, e.g., calibrated, not_calibrated.
  const std::string& getSensorCalibrationStatus() const { return sensorCalibrationStatus; }
  /// \param value the calibration status of the sensor.
  void setSensorCalibrationStatus(const std::string& value) { sensorCalibrationStatus = value; }

  /// \return the date of the last maintenance performed on the sensor.
  const std::string& getSensorLastMaintenanceDate() const { return sensorLastMaintenanceDate; }
  /// \param value the date of the last maintenance performed on the sensor.
  void setSensorLastMaintenanceDate(const std::string& value) { sensorLastMaintenanceDate = value; }

  /// \brief Converts the characterization into a human-readable string format.
  /// \return the characteristics, `null` for the unknown ones.
  std::string toString() const {
    auto show = [](const std::string& s) { return s.empty() ? std::string("null") : s; };
    return "DT_DataCharacterization{unit=" + show(unit) + ", systemStatus=" + show(systemStatus) +
           ", signalType=" + show(signalType) + ", dataFlowType=" + show(dataFlowType) +
           ", sensorPrecision=" + show(sensorPrecision) + ", location=" + show(physicalLocationOfTheSensor) +
           ", calibration=" + show(sensorCalibrationStatus) + ", lastMaintenance=" + show(sensorLastMaintenanceDate) +
           "}";
  }

 protected:
  /// Unit of measurement for the data, e.g., Celsius, Pascal, meter.
  std::string unit;
  /// Current operational status of the system providing the data, e.g., active, inactive.
  std::string systemStatus;
  /// Analog vs digital data signals: analog signals are continuous, digital signals are discrete.
  std::string signalType;
  /// Event-triggered vs continuous flow of data: event-triggered, continuous or periodic.
  std::string dataFlowType;
  /// Level of tolerance error of the sensor.
  std::string sensorPrecision;
  /// Physical location of the sensor, describing the place where it is installed.
  std::string physicalLocationOfTheSensor;
  /// Tells whether the sensor has been calibrated or not to deliver the data, e.g., calibrated, not_calibrated.
  std::string sensorCalibrationStatus;
  /// Date of the last maintenance performed on the sensor.
  std::string sensorLastMaintenanceDate;
};

}  // namespace Data

#endif  // DT_DMS_DATA_CHARACTERIZATION_HPP
