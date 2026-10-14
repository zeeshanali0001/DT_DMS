# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Service.py
@brief Abstract base class of the DT services `s ∈ S`.
"""

from abc import abstractmethod
from typing import Any, List, Optional, TypeVar

from Automaton.DT_Automaton import DT_Automaton
from Data.DT_DataSegment import DT_DataSegment
from Models.DT_Model import DT_Model
from Mu.ObserverData import ObserverData
from Mu.ObserverModel import ObserverModel

T = TypeVar("T")


class DT_Service(DT_Automaton, ObserverData, ObserverModel):
    """!
    @brief Abstract base class representing a generic DT service `s ∈ S` of `DT = ⟨D, M, S, μ⟩`.

    `S` is the set of DT services: `S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont`, the monitoring
    (DT_Service_Monitoring), prediction (DT_Service_Prediction), diagnosis (DT_Service_Diagnosis), optimization
    (DT_Service_Optimization) and control (DT_Service_Control) services.

    A service is parameterized by a model `m ∈ M` and/or the data segment(s) it consumes `ω ∈ Ω_D`:
    `s : M × Ω_D → Y_s`, where `Y_s` is the output produced by `s` (execute()). Services are specified with the
    same formalism as models because both belong to the class of automata `𝒜` (`S ⊆ 𝒜`); hence this class
    extends DT_Automaton and inherits the 7-tuple `𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`.

    A service observes the data (ObserverData) and the model it is coupled to (ObserverModel):
    update_with_model() is called each time `μ` has updated that model.
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, service_name: str, service_description: Optional[str]):
        """!
        @brief Constructor to initialize the service with a name and an optional description.
        @param service_name        the name of the service.
        @param service_description a brief description of the service (may be `None`).
        """
        DT_Automaton.__init__(self)
        ## The name of the service (e.g., "Temperature Monitoring").
        self.service_name: str = service_name
        ## A description of the service, providing additional details about its purpose.
        self.service_description: str = service_description if service_description else "No description provided."
        ## The model `m ∈ M` the service is coupled to (`None` for a purely data-driven service).
        self.model: DT_Model = None
        ## The data consumed by the service, in the order received.
        self.consumed_data: List[T] = []

    def update(self, data: T) -> None:
        """!
        @brief Called when new data arrives: the datum is added to the data consumed by the service.
        @param data the new datum.
        """
        self.consumed_data.append(data)

    def update_with_model(self, m: DT_Model) -> None:
        """!
        @brief Called each time the model the service is coupled to has been updated by `μ`. By default it does
        nothing; a service that must react to every new version of its model (e.g., re-check an interpolation)
        overrides it.
        @param m the updated model `m'`.
        """

    def execute(self, m: Optional[DT_Model], omega: Optional[DT_DataSegment]) -> Any:
        """!
        @brief Runs the service, `s(m, ω) ∈ Y_s`: couples it to `m`, consumes the data of `ω` in time order, then
        runs execute_service().
        @param m     the model `m` (may be `None` for a data-driven service).
        @param omega the data segment `ω` (may be `None` for a model-driven service).
        @return the output of the service, an element of `Y_s`.
        """
        self.model = m
        if omega is not None:
            self.consumed_data.clear()
            for d in omega.get_image():
                self.update(d)
        return self.execute_service()

    @abstractmethod
    def execute_service(self) -> Any:
        """!
        @brief Executes the service logic on the coupled model and/or the consumed data.
        Each subclass defines the specific behaviour of the service.
        @return the output of the service, an element of `Y_s`.
        """

    def get_model(self) -> Optional[DT_Model]:
        """! @return the model `m` the service is coupled to, or `None`."""
        return self.model

    def set_model(self, model: Optional[DT_Model]) -> None:
        """! @param model the model `m` to couple the service to."""
        self.model = model

    def get_service_name(self) -> str:
        """! @return the name of the service."""
        return self.service_name

    def set_service_name(self, service_name: str) -> None:
        """! @param service_name the new name of the service."""
        self.service_name = service_name

    def get_service_description(self) -> str:
        """! @return the description of the service."""
        return self.service_description

    def set_service_description(self, service_description: str) -> None:
        """! @param service_description the new description of the service."""
        self.service_description = service_description

    def get_consumed_data(self) -> List[T]:
        """! @return the data consumed by the service."""
        return self.consumed_data

    def __repr__(self) -> str:
        """! @return a string containing the service's name and description."""
        return (type(self).__name__ + " [serviceName=" + self.service_name
                + ", serviceDescription=" + self.service_description + "]")

    def __eq__(self, other) -> bool:
        """! @brief Two services are considered equal if they have the same class and the same name."""
        return type(self) is type(other) and self.service_name == other.service_name

    def __hash__(self) -> int:
        """! @return the hash code of the service, based on its name."""
        return hash(self.service_name)
