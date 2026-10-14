# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Service_Prediction.py
@brief A prediction service `s ∈ S_pred`.
"""

import math
from typing import Optional, TypeVar

from Automaton.DT_Automaton import numeric_value
from Models.DT_Model_Predictive import DT_Model_Predictive
from Services.DT_Service import DT_Service

T = TypeVar("T")


class DT_Service_Prediction(DT_Service):
    """!
    @brief Class representing a Prediction Service (`s ∈ S_pred`) of the Digital Twin.

    A Prediction Service forecasts future states of the ToI. It uses the predictive model it is coupled to;
    without such a model it falls back to the mean of the data it has consumed.
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, service_name: str, service_description: Optional[str]):
        """!
        @brief Constructor to initialize the Prediction Service.
        @param service_name        the name of the service.
        @param service_description a brief description of the service.
        """
        super().__init__(service_name, service_description)
        ## The predicted value, computed by execute_service() (`NaN` if none).
        self.predicted_value: float = math.nan

    def execute_service(self) -> float:
        """!
        @brief Executes the prediction logic: asks the coupled predictive model, or else averages the consumed data.
        @return the predicted value (`NaN` if nothing can be predicted).
        """
        if isinstance(self.model, DT_Model_Predictive):
            self.predicted_value = self.model.predict()
        else:
            values = [v for v in (numeric_value(d) for d in self.consumed_data) if not math.isnan(v)]
            total = 0.0
            for v in values:
                total += v
            self.predicted_value = math.nan if not values else total / len(values)
        return self.predicted_value

    def get_predicted_value(self) -> float:
        """! @return the last predicted value."""
        return self.predicted_value
