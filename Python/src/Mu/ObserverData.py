# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file ObserverData.py
@brief Observer of the data observer pattern.
"""

from abc import ABCMeta, abstractmethod
from typing import TypeVar

T = TypeVar("T")


class ObserverData(metaclass=ABCMeta):
    """!
    @brief Observer of the data observer pattern: an object that wants to be told when new data arrives.
    @tparam T the type of a datum `d ∈ D`.
    """

    @abstractmethod
    def update(self, updated_data: T) -> None:
        """!
        @brief Called when new data of type T arrives.
        @param updated_data the new datum.
        """
