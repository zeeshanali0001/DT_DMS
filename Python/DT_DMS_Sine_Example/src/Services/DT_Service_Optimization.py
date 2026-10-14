# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Service_Optimization.py
@brief An optimization service `s ∈ S_opti`.
"""

from typing import Optional, TypeVar

from Models.DT_Model_Prescriptive import DT_Model_Prescriptive
from Services.DT_Service import DT_Service

T = TypeVar("T")


class DT_Service_Optimization(DT_Service):
    """!
    @brief Class representing an Optimization Service (`s ∈ S_opti`) of the Digital Twin.

    An Optimization Service finds the best solution for the ToI (performance, resource allocation, ...). It
    relies on the prescriptive model it is coupled to for the recommendation.
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, service_name: str, service_description: Optional[str], optimization_algorithm: str):
        """!
        @brief Constructor to initialize the Optimization Service with necessary attributes.
        @param service_name           the name of the service.
        @param service_description    a brief description of the service.
        @param optimization_algorithm the algorithm used for optimization.
        """
        super().__init__(service_name, service_description)
        ## The optimization algorithm used (e.g., "Genetic Algorithm", "Linear Programming").
        self.optimization_algorithm: str = optimization_algorithm
        ## The result of the last optimization.
        self.optimization_result: str = "No optimization performed yet."

    def execute_service(self) -> str:
        """!
        @brief Executes the optimization logic: asks the coupled prescriptive model for its recommendation.
        @return the optimization result.
        """
        if isinstance(self.model, DT_Model_Prescriptive):
            self.optimization_result = ("Optimization (" + self.optimization_algorithm + ") for objective '"
                                        + self.model.get_objective() + "': " + self.model.recommend())
        else:
            self.optimization_result = "Optimization failed: no prescriptive model is coupled to the service."
        return self.optimization_result

    def get_optimization_algorithm(self) -> str:
        """! @return the optimization algorithm."""
        return self.optimization_algorithm

    def get_optimization_result(self) -> str:
        """! @return the result of the last optimization."""
        return self.optimization_result
