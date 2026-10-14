# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Model_Predictive.py
@brief A predictive model `m ∈ M_pred`.
"""

import math
from typing import List, TypeVar

from Automaton.DT_Automaton import format_double, numeric_value
from Models.DT_Model import DT_Model

T = TypeVar("T")


class DT_Model_Predictive(DT_Model):
    """!
    @brief Represents a predictive model (`m ∈ M_pred`) of the Digital Twin.

    A predictive model tells what is likely to happen in the ToI: it uses the historical and real-time data it
    has assimilated to predict future states or behaviours.
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, model_name: str, algorithm: str, window: int):
        """!
        @brief Constructor to initialize a predictive model with its attributes.
        @param model_name the name of the predictive model.
        @param algorithm  the algorithm used for prediction.
        @param window     the number of most recent values used for a prediction (at least 1).
        """
        super().__init__(model_name)
        ## The algorithm used for making predictions (e.g., "Moving average", "Linear Regression").
        self.algorithm: str = algorithm
        ## The number of most recent values used for a prediction.
        self.window: int = max(1, window)
        ## The numeric values assimilated by the model, in assimilation order.
        self.history: List[float] = []

    def update(self, updated_data: T) -> None:
        """!
        @brief Updates the model with a new datum: numeric values are added to the history.
        @param updated_data the new datum.
        """
        v = numeric_value(updated_data)
        if not math.isnan(v):
            self.history.append(v)

    def predict(self) -> float:
        """!
        @brief Predicts the next value as the mean of the last #window values (a simple placeholder that
        subclasses can replace by a real algorithm).
        @return the predicted value, or `NaN` if no data has been assimilated.
        """
        if not self.history:
            return math.nan
        recent = self.history[-self.window:]
        total = 0.0
        for v in recent:
            total += v
        return total / len(recent)

    def execute_model(self) -> None:
        """! @brief Executes the predictive model: prints its prediction."""
        print("Executing predictive model: " + self.model_name)
        print("  Using algorithm: " + self.algorithm + " (window=" + str(self.window) + ")")
        print("  Predicted value: " + format_double(self.predict()))

    def get_algorithm(self) -> str:
        """! @return the name of the algorithm."""
        return self.algorithm

    def set_algorithm(self, algorithm: str) -> None:
        """! @param algorithm the new algorithm name."""
        self.algorithm = algorithm

    def get_history(self) -> List[float]:
        """! @return the numeric values assimilated by the model."""
        return self.history

    def history_to_string(self) -> str:
        """! @return the history as text: `[v1, v2, ...]`."""
        return "[" + ", ".join(format_double(v) for v in self.history) + "]"

    def __repr__(self) -> str:
        """! @return a string describing the predictive model."""
        return ("DT_Model_Predictive [modelName=" + self.model_name + ", algorithm=" + self.algorithm
                + ", window=" + str(self.window) + ", assimilated=" + str(len(self.history)) + "]")
