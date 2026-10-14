# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Service_Monitoring.py
@brief A monitoring service `s ∈ S_moni`.
"""

import math
from typing import Optional, TypeVar

from Automaton.DT_Automaton import format_double, numeric_value
from Services.DT_Service import DT_Service

T = TypeVar("T")


class DT_Service_Monitoring(DT_Service):
    """!
    @brief Class representing a Monitoring Service (`s ∈ S_moni`) of the Digital Twin.

    A Monitoring Service observes the state of the ToI in real time: it compares the latest value of a monitored
    entity against thresholds and raises an alert when it is out of bounds.
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, service_name: str, service_description: Optional[str], monitored_entity: str,
                 lower_threshold: float, upper_threshold: float):
        """!
        @brief Constructor to initialize the Monitoring Service with necessary attributes.
        @param service_name        the name of the service.
        @param service_description a brief description of the service.
        @param monitored_entity    the entity being monitored.
        @param lower_threshold     the lower limit of the monitored parameter.
        @param upper_threshold     the upper limit of the monitored parameter.
        """
        super().__init__(service_name, service_description)
        ## The name of the entity being monitored (e.g., "Temperature Sensor", "Power Grid").
        self.monitored_entity: str = monitored_entity
        ## The upper threshold limit for the monitored parameter.
        self.upper_threshold: float = float(upper_threshold)
        ## The lower threshold limit for the monitored parameter.
        self.lower_threshold: float = float(lower_threshold)
        ## The current value of the monitored parameter (`NaN` until a value is received).
        self.current_value: float = math.nan

    def update(self, data: T) -> None:
        """!
        @brief Called when new data arrives: a numeric datum becomes the current value of the monitored parameter.
        @param data the new datum.
        """
        super().update(data)
        v = numeric_value(data)
        if not math.isnan(v):
            self.current_value = v

    def execute_service(self) -> str:
        """!
        @brief Executes the monitoring logic: compares the current value with the thresholds.
        @return the monitoring status (an alert if the value is out of bounds).
        """
        if math.isnan(self.current_value):
            return "STATUS: no value received yet for " + self.monitored_entity + "."
        value = format_double(self.current_value)
        if self.current_value > self.upper_threshold:
            return ("ALERT: " + self.monitored_entity + " = " + value + " exceeds the upper threshold "
                    + format_double(self.upper_threshold) + "!")
        if self.current_value < self.lower_threshold:
            return ("ALERT: " + self.monitored_entity + " = " + value + " falls below the lower threshold "
                    + format_double(self.lower_threshold) + "!")
        return "STATUS: " + self.monitored_entity + " = " + value + " is within safe limits."

    def get_monitored_entity(self) -> str:
        """! @return the name of the monitored entity."""
        return self.monitored_entity

    def get_upper_threshold(self) -> float:
        """! @return the upper threshold."""
        return self.upper_threshold

    def set_upper_threshold(self, upper_threshold: float) -> None:
        """! @param upper_threshold the new upper threshold."""
        self.upper_threshold = float(upper_threshold)

    def get_lower_threshold(self) -> float:
        """! @return the lower threshold."""
        return self.lower_threshold

    def set_lower_threshold(self, lower_threshold: float) -> None:
        """! @param lower_threshold the new lower threshold."""
        self.lower_threshold = float(lower_threshold)

    def get_current_value(self) -> float:
        """! @return the current value of the monitored parameter."""
        return self.current_value
