# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Model_Descriptive.py
@brief A descriptive model `m ∈ M_desc`.
"""

import math
from typing import TypeVar

from Automaton.DT_Automaton import format_double, numeric_value, value_to_string
from Models.DT_Model import DT_Model

T = TypeVar("T")


class DT_Model_Descriptive(DT_Model):
    """!
    @brief Represents a descriptive model (`m ∈ M_desc`) of the Digital Twin.

    A descriptive model describes what happened or what is happening in the ToI: it summarizes and interprets
    the historical and real-time data it has assimilated.
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, model_name: str, data_source: str):
        """!
        @brief Constructor to initialize a descriptive model with its attributes.
        @param model_name  the name of the descriptive model.
        @param data_source the source of the data (e.g., "Sensor A", "Database X").
        """
        super().__init__(model_name)
        ## The source of the data described by the model.
        self.data_source: str = data_source
        ## The total number of data points assimilated by the model.
        self.data_point_count: int = 0
        ## A summary of the data assimilated by the model.
        self.data_summary: str = "no data"
        ## The smallest numeric value assimilated.
        self.minimum: float = math.inf
        ## The largest numeric value assimilated.
        self.maximum: float = -math.inf
        ## The sum of the numeric values assimilated (used for the mean).
        self.sum: float = 0.0

    def update(self, updated_data: T) -> None:
        """!
        @brief Updates the description with a new datum: counts it and, if it is a number, updates the minimum,
        maximum and mean.
        @param updated_data the new datum.
        """
        self.data_point_count += 1
        v = numeric_value(updated_data)
        if not math.isnan(v):
            self.minimum = min(self.minimum, v)
            self.maximum = max(self.maximum, v)
            self.sum += v
            self.data_summary = ("min=" + format_double(self.minimum) + ", max=" + format_double(self.maximum)
                                 + ", mean=" + format_double(self.get_mean()))
        else:
            self.data_summary = "last=" + value_to_string(updated_data)

    def execute_model(self) -> None:
        """! @brief Executes the descriptive model: prints the description of the data assimilated so far."""
        print("Executing descriptive model: " + self.model_name)
        print("  Data source: " + self.data_source)
        print("  Number of data points analyzed: " + str(self.data_point_count))
        print("  Data summary: " + self.data_summary)

    def get_mean(self) -> float:
        """! @return the mean of the numeric values assimilated (`NaN` if none)."""
        return math.nan if self.data_point_count == 0 else self.sum / self.data_point_count

    def get_data_source(self) -> str:
        """! @return the source of the data."""
        return self.data_source

    def get_data_point_count(self) -> int:
        """! @return the total number of data points assimilated."""
        return self.data_point_count

    def get_data_summary(self) -> str:
        """! @return the summary of the data assimilated."""
        return self.data_summary

    def __repr__(self) -> str:
        """! @return a string describing the descriptive model."""
        return ("DT_Model_Descriptive [modelName=" + self.model_name + ", dataSource=" + self.data_source
                + ", dataPointCount=" + str(self.data_point_count) + ", dataSummary=" + self.data_summary + "]")
