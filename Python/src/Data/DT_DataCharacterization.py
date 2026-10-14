# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_DataCharacterization.py
@brief The characterization of a data source of the ToI.
"""

from typing import Optional


class DT_DataCharacterization:
    """!
    @brief Class representing the characterization of a data source of the ToI: the metadata that tell how the
    data of `D` were produced and how they must be interpreted.

    Every DT_Data holds one characterization. It is used by the Digital Twin to check the data characteristics
    (formats, units, ...) with respect to the services it provides (`DT_DMS.check_data_versus_services`).

    The characteristics are:
    - the unit of measurement (e.g., Celsius, Pascal, meter);
    - the operational status of the system providing the data (e.g., active, inactive);
    - the signal type: analog (continuous) or digital (discrete);
    - the data flow type: event-triggered, continuous or periodic;
    - the precision (level of tolerance error) of the sensor;
    - the physical location of the sensor;
    - the calibration status of the sensor (e.g., calibrated, not_calibrated);
    - the date of the last maintenance performed on the sensor.

    A characteristic that is not known is `None`.
    """

    def __init__(self, other: Optional["DT_DataCharacterization"] = None):
        """!
        @brief Creates a characterization where no characteristic is known, or a copy of `other`.
        @param other the characterization to copy, or `None`.
        """
        ## Unit of measurement for the data, e.g., Celsius, Pascal, meter.
        self.unit: Optional[str] = None
        ## Current operational status of the system providing the data, e.g., active, inactive.
        self.system_status: Optional[str] = None
        ## Analog vs digital data signals: analog signals are continuous, digital signals are discrete.
        self.signal_type: Optional[str] = None
        ## Event-triggered vs continuous flow of data: event-triggered, continuous or periodic.
        self.data_flow_type: Optional[str] = None
        ## Level of tolerance error of the sensor.
        self.sensor_precision: Optional[str] = None
        ## Physical location of the sensor, describing the place where it is installed.
        self.physical_location_of_the_sensor: Optional[str] = None
        ## Tells whether the sensor has been calibrated or not to deliver the data, e.g., calibrated, not_calibrated.
        self.sensor_calibration_status: Optional[str] = None
        ## Date of the last maintenance performed on the sensor.
        self.sensor_last_maintenance_date: Optional[str] = None
        if other is not None:
            self.__dict__.update(other.__dict__)

    def get_unit(self) -> Optional[str]:
        """! @return the unit of measurement for the data, e.g., Celsius, Pascal, meter."""
        return self.unit

    def set_unit(self, unit: Optional[str]) -> None:
        """! @param unit the unit of measurement for the data."""
        self.unit = unit

    def get_system_status(self) -> Optional[str]:
        """! @return the current operational status of the system providing the data."""
        return self.system_status

    def set_system_status(self, system_status: Optional[str]) -> None:
        """! @param system_status the operational status of the system providing the data."""
        self.system_status = system_status

    def get_signal_type(self) -> Optional[str]:
        """! @return the signal type: analog (continuous) or digital (discrete)."""
        return self.signal_type

    def set_signal_type(self, signal_type: Optional[str]) -> None:
        """! @param signal_type the signal type: analog or digital."""
        self.signal_type = signal_type

    def get_data_flow_type(self) -> Optional[str]:
        """! @return the data flow type: event-triggered, continuous or periodic."""
        return self.data_flow_type

    def set_data_flow_type(self, data_flow_type: Optional[str]) -> None:
        """! @param data_flow_type the data flow type: event-triggered, continuous or periodic."""
        self.data_flow_type = data_flow_type

    def get_sensor_precision(self) -> Optional[str]:
        """! @return the level of tolerance error of the sensor."""
        return self.sensor_precision

    def set_sensor_precision(self, sensor_precision: Optional[str]) -> None:
        """! @param sensor_precision the level of tolerance error of the sensor."""
        self.sensor_precision = sensor_precision

    def get_physical_location_of_the_sensor(self) -> Optional[str]:
        """! @return the physical location of the sensor."""
        return self.physical_location_of_the_sensor

    def set_physical_location_of_the_sensor(self, location: Optional[str]) -> None:
        """! @param location the physical location of the sensor."""
        self.physical_location_of_the_sensor = location

    def get_sensor_calibration_status(self) -> Optional[str]:
        """! @return whether the sensor has been calibrated, e.g., calibrated, not_calibrated."""
        return self.sensor_calibration_status

    def set_sensor_calibration_status(self, status: Optional[str]) -> None:
        """! @param status the calibration status of the sensor."""
        self.sensor_calibration_status = status

    def get_sensor_last_maintenance_date(self) -> Optional[str]:
        """! @return the date of the last maintenance performed on the sensor."""
        return self.sensor_last_maintenance_date

    def set_sensor_last_maintenance_date(self, date: Optional[str]) -> None:
        """! @param date the date of the last maintenance performed on the sensor."""
        self.sensor_last_maintenance_date = date

    def __repr__(self) -> str:
        """! @return the characteristics, `None` for the unknown ones."""
        return ("DT_DataCharacterization{unit=" + str(self.unit) + ", systemStatus=" + str(self.system_status)
                + ", signalType=" + str(self.signal_type) + ", dataFlowType=" + str(self.data_flow_type)
                + ", sensorPrecision=" + str(self.sensor_precision)
                + ", location=" + str(self.physical_location_of_the_sensor)
                + ", calibration=" + str(self.sensor_calibration_status)
                + ", lastMaintenance=" + str(self.sensor_last_maintenance_date) + "}")
