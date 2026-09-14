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

DT_Models_Descr_CubicBSpline.py

Cubic B-Spline model for interpolating sine wave data.
This is the core "Digital Model" component of the Digital Twin.
It reacts to new sine data and can trigger service updates.
"""

import math

from Data.DT_Data import DT_Data
from Data.DT_Data_SineValues import DT_Data_SineValues
from Models.DT_Models_Descriptive import DT_Models_Descriptive


class DT_Models_Descr_CubicBSpline(DT_Models_Descriptive):

    def __init__(self, step: float):
        """
        Constructor for the Cubic B-Spline model.

        :param step: Step size between samples.
        """
        super().__init__()

        # Step size used for generating x values in the sine wave
        self._step = step

        # Left endpoint of the domain (usually 0.0)
        self._leftEndpoint = 0.0

        # Reference to the linked diagnosis service (DT_Services_Diagnosis_Sine)
        self._linkedService = None

    def add_service_2_be_updated(self, srv):
        """
        Links a diagnosis service that this model can notify directly.

        :param srv: The diagnosis service to be linked.
        """
        self._linkedService = srv

    def update(self, dat: DT_Data):
        """
        Reacts to new sine data pushed through the Observer Pattern.

        :param dat: The updated DT_Data instance (expected to be DT_Data_SineValues).
        """
        if isinstance(dat, DT_Data_SineValues):
            print(f"# CubicBSpline_Model::update() done for index {dat.get_current_index()}")
            self.notify_services()

    def notify_services(self):
        """
        Notifies the linked diagnosis service.
        This maintains the original model-to-service direct communication.
        """
        if self._linkedService is not None:
            self._linkedService.update_service_with_model()

    def get_sine_model_value(self, x: float) -> float:
        """
        Returns the sine model value at position x (simplified implementation).
        In a real DT, this could be a proper B-Spline interpolation.

        :param x: Input value.
        :return: Computed model value (here simply sin(x)).
        """
        return math.sin(x)

    def get_left_endpoint(self) -> float:
        return self._leftEndpoint

    def get_step(self) -> float:
        return self._step
