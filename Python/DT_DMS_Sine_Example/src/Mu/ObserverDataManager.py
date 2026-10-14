# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file ObserverDataManager.py
@brief Subject of the data observer pattern.
"""

from abc import ABCMeta, abstractmethod
from typing import TypeVar

from Mu.ObserverData import ObserverData

T = TypeVar("T")


class ObserverDataManager(metaclass=ABCMeta):
    """!
    @brief Subject of the data observer pattern: an object that produces data and manages its observers.
    @tparam T the type of a datum `d ∈ D`.
    """

    @abstractmethod
    def register_observer(self, observer: ObserverData) -> None:
        """!
        @brief Registers an observer; it will be notified of every new datum.
        @param observer the observer to add.
        """

    @abstractmethod
    def remove_observer(self, observer: ObserverData) -> None:
        """!
        @brief Removes an observer.
        @param observer the observer to remove.
        """

    @abstractmethod
    def notify_observers(self) -> None:
        """! @brief Notifies all registered observers of the latest datum."""
