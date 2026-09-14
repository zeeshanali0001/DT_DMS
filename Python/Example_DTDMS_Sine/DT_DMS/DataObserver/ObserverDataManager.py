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

ObserverDataManager.py

Subject interface (Publisher) in the Observer Pattern.
Classes that hold data and want to notify observers when data changes
must implement (inherit from) this abstract base class.
"""

from abc import ABC, abstractmethod


class ObserverDataManager(ABC):
    """
    Generic Subject/Publisher interface.

    As with ObserverData, the Java generic type parameter <T> is dropped
    since Python does not require it for correct behaviour (dynamic typing).
    """

    @abstractmethod
    def register_observer(self, observer):
        """
        Registers an observer to receive notifications when data changes.

        :param observer: The observer (ObserverData instance) to be registered.
        """
        raise NotImplementedError

    @abstractmethod
    def remove_observer(self, observer):
        """
        Removes a previously registered observer so it no longer receives updates.

        :param observer: The observer (ObserverData instance) to be removed.
        """
        raise NotImplementedError

    @abstractmethod
    def notify_observers(self):
        """
        Notifies all registered observers that the data has changed.
        This method is called internally whenever data is updated.
        """
        raise NotImplementedError
