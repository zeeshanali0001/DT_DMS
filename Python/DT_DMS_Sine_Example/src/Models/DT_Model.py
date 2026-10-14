# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Model.py
@brief Abstract base class of the DT models `m ∈ M`.
"""

import math
from abc import abstractmethod
from typing import List, TypeVar

from Automaton.DT_Automaton import DT_Automaton
from Data.DT_DataSegment import DT_DataSegment
from Mu.ObserverData import ObserverData
from Mu.ObserverModel import ObserverModel

T = TypeVar("T")


class DT_Model(DT_Automaton, ObserverData):
    """!
    @brief Abstract base class representing a generic DT model `m ∈ M` of `DT = ⟨D, M, S, μ⟩`.

    `M` is the set of DT models that represent the ToI virtually: `M = M_desc ∪ M_pred ∪ M_presc`, the
    descriptive (DT_Model_Descriptive), predictive (DT_Model_Predictive) and prescriptive
    (DT_Model_Prescriptive) models.

    Every model belongs to the class of automata `𝒜` (`M ⊆ 𝒜`); hence this class extends DT_Automaton and
    inherits the 7-tuple `ℳ = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`. A model is updated by the data assimilation function
    `μ` through assimilate().

    A model is also the subject of the model observer pattern: the services coupled to it
    (add_service_to_be_updated()) are notified after each assimilation (notify_services()).
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, model_name: str):
        """!
        @brief Constructor to initialize the model's name.
        @param model_name the name of the model.
        """
        DT_Automaton.__init__(self)
        ## The name of the model, which uniquely identifies it.
        self.model_name: str = model_name
        ## The services to be notified when the model is updated (model observer pattern).
        self._services_to_be_updated: List[ObserverModel] = []

    def assimilate(self, omega: DT_DataSegment) -> None:
        """!
        @brief Assimilates a data segment into this model: the model side of `μ(ω, m) = m'`.

        The model is updated in place, so `m'` keeps the type of `m`. By default each datum of `ω` is passed to
        update() in time order, then the coupled services are notified (notify_services()); subclasses can
        override it (e.g., to re-train a predictive model on a whole historical segment) and should then call
        notify_services() too.
        @param omega the data segment `ω`.
        """
        for d in omega.get_image():
            self.update(d)
        self.notify_services()

    @abstractmethod
    def execute_model(self) -> None:
        """! @brief Executes the core logic of the model. Each subclass provides its specific implementation."""

    def evaluate(self, t: float) -> float:
        """!
        @brief Returns the value of the model at time `t`, `m(t)`, to be compared with the datum `ω(t)` measured at
        the same time (`DT_DMS.check_models_versus_data`).

        By default a model gives no value in time and this method returns `NaN`; a model that describes or
        predicts the measured signal (e.g., an interpolation) overrides it.
        @param t the time.
        @return `m(t)`, or `NaN` if the model gives no value at `t`.
        """
        return math.nan

    # ---------------------------------------------------------------- model observer pattern (subject)

    def add_service_to_be_updated(self, service: ObserverModel) -> None:
        """!
        @brief Adds a service to be notified each time this model is updated.
        @param service the service (or any model observer).
        """
        if service not in self._services_to_be_updated:
            self._services_to_be_updated.append(service)

    def remove_service_to_be_updated(self, service: ObserverModel) -> None:
        """!
        @brief Stops notifying a service when this model is updated.
        @param service the service to remove.
        """
        if service in self._services_to_be_updated:
            self._services_to_be_updated.remove(service)

    def notify_services(self) -> None:
        """! @brief Notifies the services coupled to this model that it has been updated."""
        # Safe copy to avoid issues if a service removes itself during the update
        for service in list(self._services_to_be_updated):
            service.update_with_model(self)

    def get_services_to_be_updated(self) -> List[ObserverModel]:
        """! @return the services notified when this model is updated (a copy)."""
        return list(self._services_to_be_updated)

    def get_model_name(self) -> str:
        """! @return the model name."""
        return self.model_name

    def set_model_name(self, model_name: str) -> None:
        """! @param model_name the new model name."""
        self.model_name = model_name

    def __repr__(self) -> str:
        """! @return a string describing the model's name."""
        return type(self).__name__ + " [modelName=" + str(self.model_name) + "]"

    def __eq__(self, other) -> bool:
        """! @brief Two models are considered equal if they have the same class and the same name."""
        return type(self) is type(other) and self.model_name == other.model_name

    def __hash__(self) -> int:
        """! @return the hash code of the model, based on its name."""
        return hash(self.model_name)
