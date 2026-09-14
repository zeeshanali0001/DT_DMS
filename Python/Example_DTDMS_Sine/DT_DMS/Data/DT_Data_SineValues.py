"""
DT_Data_SineValues.py

Concrete data class that generates and stores sine wave values.
Every time 10 new values are added, it triggers notification to all observers
using the Observer Pattern.

This represents the "Physical Twin" part of the Digital Twin.

This is the Python equivalent of Data/DT_Data_SineValues.java.

@author Zeeshan ALI, Mamadou Kaba TRAORE
@version 1.0 (Python port)
"""

from Data.DT_Data import DT_Data


class DT_Data_SineValues(DT_Data):

    def __init__(self, name: str):
        """
        Constructor to initialize the sine data source.

        :param name: Descriptive name of this data source (e.g., "Sine values")
        """
        super().__init__()
        self._dataName = name

        # List that stores all generated sine values
        self._sineValues = []

        # Current index / counter of how many values have been pushed
        self._currentIndex = 0

    def get_sine_data_values(self):
        """Returns the list of all sine values collected so far."""
        return self._sineValues

    def get_current_index(self) -> int:
        """
        Returns the current index (number of values pushed so far).
        Used by the chart and diagnosis service to plot the correct x-axis.
        """
        return self._currentIndex

    def push_back(self, val: float):
        """
        Adds a new sine value to the collection.
        Increments the index and triggers notification to observers every 10 values.

        :param val: The sine value to add.
        """
        self._sineValues.append(val)
        self._currentIndex += 1

        if len(self._sineValues) % 10 == 0:
            self.update_data()  # Triggers Observer Pattern - notifies Model and Service
