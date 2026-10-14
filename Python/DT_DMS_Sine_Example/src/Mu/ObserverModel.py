# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file ObserverModel.py
@brief Observer of the model observer pattern.
"""

from abc import ABCMeta, abstractmethod
from typing import TypeVar

T = TypeVar("T")


class ObserverModel(metaclass=ABCMeta):
    """!
    @brief Observer of the model observer pattern: an object (typically a service `s ∈ S`) that wants to be told
    when a model `m ∈ M` has been updated by `μ`.

    The subject is the model (`DT_Model.add_service_to_be_updated`); after each assimilation `μ(ω, m) = m'`,
    the model calls update_with_model() on its observers.
    @tparam T the type of a datum `d ∈ D`.
    """

    @abstractmethod
    def update_with_model(self, m) -> None:
        """!
        @brief Called when the model `m` has been updated.
        @param m the updated model `m'` (a DT_Model).
        """
