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

DT_Services_Diagnosis_Sine.py

Diagnosis service for sine wave data.
Performs random validation checks by comparing raw sine values with model
predictions, and performs anomaly detection whenever the Digital Model
notifies it of new data.
"""

import math
import random

from Data.DT_Data import DT_Data
from Data.DT_Data_SineValues import DT_Data_SineValues
from Models.DT_Models_Descr_CubicBSpline import DT_Models_Descr_CubicBSpline
from Services.DT_Services_Diagnostic import DT_Services_Diagnostic
from Visualization.DT_ChartPanel import DT_ChartPanel


class DT_Services_Diagnosis_Sine(DT_Services_Diagnostic):

    # Threshold for anomaly detection (error between model and actual value)
    ANOMALY_THRESHOLD = 0.15

    def __init__(self, model: DT_Models_Descr_CubicBSpline):
        """
        Constructor - links this service with the model.

        :param model: The Cubic B-Spline model instance.
        """
        super().__init__()

        # Reference to the Cubic B-Spline model used for comparison
        self._CS_model = model

        # Reference to the latest data source (used for index and raw values)
        self.latestData = None

        # Live visualization chart panel
        self.chartPanel = DT_ChartPanel()

    def set_latest_data(self, data: DT_Data_SineValues):
        """
        Sets the reference to the latest data source.
        This is needed for chart updates and accessing raw sine values.

        :param data: The sine data source.
        """
        self.latestData = data

    def update(self, dat: DT_Data):
        """Called via the Observer Pattern whenever DT_Data_SineValues changes."""
        if isinstance(dat, DT_Data_SineValues):
            self.perform_random_check(5)

    def update_service_with_model(self):
        """
        Called by the model when new data is processed.
        Performs diagnosis, anomaly detection, and updates the live chart.
        """
        self.perform_random_check(10)
        print("=== SineDiagnosis_Service::update_service_with_model ===")

        if self.latestData is None or len(self.latestData.get_sine_data_values()) == 0:
            return

        # Get latest values for chart and diagnosis
        index = self.latestData.get_current_index()
        x = index * self._CS_model.get_step()
        raw_sine_value = self.latestData.get_sine_data_values()[index - 1]
        model_value = self._CS_model.get_sine_model_value(x)

        # Update live chart
        self.chartPanel.update_chart(index, raw_sine_value, model_value)

        # Perform diagnosis - detect anomaly
        self.detect_anomaly(index, raw_sine_value, model_value)

    def perform_random_check(self, count: int):
        """
        Performs random validation checks (original behaviour).

        :param count: Number of random checks to perform.
        """
        for _ in range(count):
            x = random.uniform(0, 5.0)
            print(f"      sin({x:.4f}) = {math.sin(x):.4f} | spline = "
                  f"{self._CS_model.get_sine_model_value(x):.4f}")

    def detect_anomaly(self, index: int, actual: float, expected: float):
        """
        Detects anomalies by comparing raw sine value with model prediction.
        If error exceeds the threshold, prints an alert (can be extended to
        log, notify, etc.).

        :param index: Current sample index.
        :param actual: Actual value from physical data.
        :param expected: Expected value from the model.
        """
        error = abs(actual - expected)

        if error > self.ANOMALY_THRESHOLD:
            print(f"*** ANOMALY DETECTED! *** Index={index} | Actual={actual} "
                  f"| Expected={expected} | Error={error}")
            # TODO: Add alert, email, shutdown logic, or trigger recovery here
