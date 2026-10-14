# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Service_Control.py
@brief A control service `s ∈ S_cont`.
"""

from typing import Optional, TypeVar

from Automaton.DT_Automaton import format_double, numeric_value
from Models.DT_Model_Prescriptive import DT_Model_Prescriptive
from Services.DT_Service import DT_Service

T = TypeVar("T")


class DT_Service_Control(DT_Service):
    """!
    @brief Class representing a Control Service (`s ∈ S_cont`) of the Digital Twin.

    A Control Service acts back on the ToI (through actuators or other systems) based on feedback: it turns the
    latest consumed value and the target of the coupled prescriptive model into a command.
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, service_name: str, service_description: Optional[str], control_strategy: str,
                 gain: float):
        """!
        @brief Constructor to initialize the Control Service with necessary attributes.
        @param service_name        the name of the service.
        @param service_description a brief description of the service.
        @param control_strategy    the control strategy applied.
        @param gain                the gain applied to the error.
        """
        super().__init__(service_name, service_description)
        ## The control strategy applied (e.g., "Proportional", "PID Control", "Rule-Based Control").
        self.control_strategy: str = control_strategy
        ## The gain applied to the error between the target and the measured value.
        self.gain: float = float(gain)
        ## The result of the last control action.
        self.control_action_result: str = "No control action performed yet."

    def execute_service(self) -> str:
        """!
        @brief Executes the control logic: command = gain × (target − latest consumed value).
        @return the control action result.
        """
        if not isinstance(self.model, DT_Model_Prescriptive):
            self.control_action_result = "Control action failed: no prescriptive model is coupled to the service."
        elif not self.consumed_data:
            self.control_action_result = "Control action failed due to lack of inputs."
        else:
            measured = numeric_value(self.consumed_data[-1])
            command = self.gain * (self.model.get_target() - measured)
            self.control_action_result = ("Control action (" + self.control_strategy + "): command = "
                                          + format_double(command) + " for measured value "
                                          + format_double(measured) + " and target "
                                          + format_double(self.model.get_target()) + ".")
        return self.control_action_result

    def get_control_strategy(self) -> str:
        """! @return the control strategy."""
        return self.control_strategy

    def get_control_action_result(self) -> str:
        """! @return the result of the last control action."""
        return self.control_action_result
