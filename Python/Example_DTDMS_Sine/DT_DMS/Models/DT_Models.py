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

DT_Models.py

Base class for all Models in the Digital Twin system.
Implements ObserverData so it can react to changes in DT_Data.
"""

from DataObserver.ObserverData import ObserverData


class DT_Models(ObserverData):

    def __init__(self):
        # Name of this model
        self._modelName = None

    def update(self, dat):
        """
        Called by DT_Data when new data is available.
        Subclasses should override this method to implement specific model logic.

        :param dat: The updated data object.
        """
        # Default empty implementation
        pass
