# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Model_Prescriptive.py
@brief A prescriptive model `m ∈ M_presc`.
"""

import math
from typing import TypeVar

from Automaton.DT_Automaton import format_double, numeric_value
from Models.DT_Model import DT_Model

T = TypeVar("T")


class DT_Model_Prescriptive(DT_Model):
    """!
    @brief Represents a prescriptive model (`m ∈ M_presc`) of the Digital Twin.

    A prescriptive model tells what should be done on the ToI: from the data it has assimilated, it recommends
    the action to take in order to reach an objective.
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, model_name: str, objective: str, target: float, assumptions: str):
        """!
        @brief Constructor to initialize a prescriptive model with its attributes.
        @param model_name  the name of the prescriptive model.
        @param objective   the primary goal or objective of the model.
        @param target      the target value of the objective.
        @param assumptions the assumptions considered in the model.
        """
        super().__init__(model_name)
        ## The goal or objective of the prescriptive model (e.g., "Keep temperature at 22").
        self.objective: str = objective
        ## The target value of the objective.
        self.target: float = float(target)
        ## The latest numeric value assimilated by the model.
        self.last_value: float = math.nan
        ## Any assumptions made while developing or executing the prescriptive model.
        self.assumptions: str = assumptions

    def update(self, updated_data: T) -> None:
        """!
        @brief Updates the model with a new datum: keeps the latest numeric value.
        @param updated_data the new datum.
        """
        v = numeric_value(updated_data)
        if not math.isnan(v):
            self.last_value = v

    def recommend(self) -> str:
        """!
        @brief Recommends the action to take to reach the objective, from the latest value (a simple placeholder
        that subclasses can replace by a real decision model).
        @return the recommended action.
        """
        if math.isnan(self.last_value):
            return "No data assimilated: no recommendation."
        gap = self.target - self.last_value
        if gap > 0:
            return "Increase by " + format_double(gap) + " to reach the target " + format_double(self.target) + "."
        if gap < 0:
            return "Decrease by " + format_double(-gap) + " to reach the target " + format_double(self.target) + "."
        return "Target " + format_double(self.target) + " reached: no action."

    def execute_model(self) -> None:
        """! @brief Executes the prescriptive model: prints its recommendation."""
        print("Executing prescriptive model: " + self.model_name)
        print("  Objective: " + self.objective)
        print("  Assumptions: " + self.assumptions)
        print("  Recommendation: " + self.recommend())

    def get_objective(self) -> str:
        """! @return the objective of the model."""
        return self.objective

    def get_target(self) -> float:
        """! @return the target value of the objective."""
        return self.target

    def set_target(self, target: float) -> None:
        """! @param target the new target value of the objective."""
        self.target = float(target)

    def get_assumptions(self) -> str:
        """! @return the assumptions of the model."""
        return self.assumptions

    def __repr__(self) -> str:
        """! @return a string describing the prescriptive model."""
        return ("DT_Model_Prescriptive [modelName=" + self.model_name + ", objective=" + self.objective
                + ", target=" + format_double(self.target) + ", lastValue=" + format_double(self.last_value) + "]")
