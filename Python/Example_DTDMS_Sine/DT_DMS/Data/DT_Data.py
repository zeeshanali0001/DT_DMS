"""
Copyright (C) 2026 : Milad Zahediyami

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

author Milad Zahediyami (milad.zahediyami@u-bordeaux.fr)
IMS (UMR CNRS 5218 / University of Bordeaux)

DT_Data.py

Abstract base class for all data sources in the Digital Twin system.
Implements ObserverDataManager to support the Observer Pattern.

It maintains a list of observers (Models and Services) and notifies them
automatically when data is updated via update_data().
"""

from abc import ABC
from DataObserver.ObserverDataManager import ObserverDataManager


class DT_Data(ObserverDataManager, ABC):
    """
    Abstract base class representing a source of data (the "Physical Twin"
    side of the Digital Twin).
    """

    def __init__(self):
        # Name of this data source (e.g., "Sine values")
        self._dataName = None

        # List of all registered observers (Models + Services)
        self.observers = []

    def register_observer(self, observer):
        """Registers an observer if it is not already registered."""
        if observer not in self.observers:
            self.observers.append(observer)
            print(f"Observer registered for {self._dataName}: {type(observer).__name__}")

    def remove_observer(self, observer):
        """Removes a previously registered observer."""
        if observer in self.observers:
            self.observers.remove(observer)
            print(f"Observer removed for {self._dataName}: {type(observer).__name__}")

    def notify_observers(self):
        """Notifies every registered observer that data has changed."""
        # Iterate over a shallow copy
        # 'new ArrayList<>(observers)', to protect against concurrent
        for obs in list(self.observers):
            obs.update(self)

    def update_data(self):
        """
        Updates the data and notifies all registered observers (Models and Services).
        This is the central method that triggers the Observer Pattern.
        """
        print(f"\n[DT_Data] Data updated -> notifying observers: {self._dataName}")
        self.notify_observers()

    def get_data_name(self):
        """Returns the name of this data source."""
        return self._dataName
