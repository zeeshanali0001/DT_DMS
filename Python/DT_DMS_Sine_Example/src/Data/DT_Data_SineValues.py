# DT-DMS sine example (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Data_SineValues.py
@brief Data source of the sine example: the ToI producing a sine wave.
"""

import math

from Data.DT_Data import DT_Data


class DT_Data_SineValues(DT_Data):
    """!
    @brief Data source of the sine example: it plays the role of the ToI (the "physical twin") and produces the
    sine wave values measured over time.

    Each call to measure() produces the datum `d = ω(t) = sin(t)` measured at time `t` and records it; the
    observers (the data assimilation function `μ`) are notified at once. An anomaly can be injected to test the
    diagnosis service.
    """

    def __init__(self, name: str):
        """!
        @brief Constructor to initialize the sine data source.
        @param name descriptive name of this data source (e.g., "Sine values").
        """
        super().__init__(name, "Sine")
        ## The time at which an anomaly is injected (`NaN` = no anomaly).
        self._anomaly_time: float = math.nan
        ## The offset added to the sine value at the anomaly time.
        self._anomaly_offset: float = 0.0

    def measure(self, t: float) -> float:
        """!
        @brief Measures the ToI at time `t`: produces `sin(t)` (plus the anomaly offset, if any at that time),
        records it and notifies the observers.
        @param t the measurement time.
        @return the measured value.
        """
        value = math.sin(t)
        if abs(t - self._anomaly_time) < 1e-9:
            value += self._anomaly_offset
        self.update_data(t, value)
        return value

    def inject_anomaly(self, time: float, offset: float) -> None:
        """!
        @brief Injects an anomaly: the value measured at `time` will be shifted by `offset`.
        @param time   the measurement time of the anomaly.
        @param offset the offset added to the sine value.
        """
        self._anomaly_time = time
        self._anomaly_offset = offset
