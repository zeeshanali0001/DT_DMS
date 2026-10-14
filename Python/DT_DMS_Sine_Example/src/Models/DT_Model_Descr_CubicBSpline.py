# DT-DMS sine example (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Model_Descr_CubicBSpline.py
@brief Cubic B-Spline model of the sine example.
"""

import math
from typing import List

from Automaton.DT_Automaton import Event, Initialization, Phase, Transition, TransitionKind
from Models.DT_Model_Descriptive import DT_Model_Descriptive


class DT_Model_Descr_CubicBSpline(DT_Model_Descriptive):
    """!
    @brief Cubic B-Spline model of the sine example: a descriptive model (`m ∈ M_desc`) that represents the
    measured sine wave by a smooth curve.

    The assimilated values are the control points of a uniform cubic B-spline: the i-th value assimilated is the
    control point of time `left_endpoint + i × step`. The data must therefore be assimilated in time order, one
    value per step.

    The model is specified as the automaton `ℳ = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`:
    - `Λ` = {step, leftEndpoint}: the sampling assumptions.
    - `X` = {data}: a measured sine value.
    - `Y` = {updated}: the number of control points after an update.
    - `Σ` = {count, last}: the number of control points and the last value assimilated.
    - `Φ` = {WAIT ⟨count ≥ 0, NIL, ∞⟩, UPDATE ⟨count ≥ 1, NIL, 0⟩}.
    - `Δ^in`: WAIT → UPDATE on `(data, d)`, Update: `count := count + 1; last := d`.
    - `Δ^time`: UPDATE → WAIT, Send: `(updated, count)`.
    - `Δ*` = ⟨WAIT, NIL, 0⟩.
    """

    def __init__(self, step: float, left_endpoint: float):
        """!
        @brief Constructor for the Cubic B-Spline model.
        @param step          step size between samples.
        @param left_endpoint time of the first sample.
        """
        super().__init__("Cubic B-Spline", "Sine values")
        ## Step size between two samples of the sine wave.
        self.step: float = step
        ## Left endpoint of the domain: the time of the first sample (usually 0.0).
        self.left_endpoint: float = left_endpoint
        ## The control points of the B-spline: the values assimilated, in time order.
        self.control_points: List[float] = []
        self._specify_automaton()

    def _specify_automaton(self) -> None:
        """! Specifies the 7-tuple `ℳ` of the model and runs its initialization `Δ*`."""
        self.add_parameter("step", self.step)                                         # Λ
        self.add_parameter("leftEndpoint", self.left_endpoint)
        self.add_input("data")                                                        # X
        self.add_output("updated")                                                    # Y
        self.add_state_variable("count", 0)                                           # Σ
        self.add_state_variable("last", math.nan)
        self.add_phase(Phase("WAIT", lambda q: q["count"] >= 0, None, math.inf))      # Φ
        self.add_phase(Phase("UPDATE", lambda q: q["count"] >= 1, None, 0.0))

        def count_and_store(q, x):
            q["count"] = q["count"] + 1
            q["last"] = x.value
            return q

        self.add_transition(Transition(TransitionKind.IN, "WAIT", "UPDATE",          # Δ^in
                                       {"data"}, None, None, count_and_store))
        self.add_transition(Transition(TransitionKind.TIME, "UPDATE", "WAIT",        # Δ^time
                                       None, None, lambda q: [Event("updated", q["count"])], None))
        self.set_initialization(Initialization("WAIT", None, 0.0))                   # Δ*
        self.initialize()

    def update(self, updated_data: float) -> None:
        """!
        @brief Updates the model with a new sine value: it becomes the next control point, and the automaton goes
        WAIT → UPDATE → WAIT.
        @param updated_data the new sine value.
        """
        super().update(updated_data)                     # descriptive statistics (count, min, max, mean)
        self.control_points.append(updated_data)
        self.external_transition(Event("data", updated_data), self.step)
        self.internal_transition()

    def get_sine_model_value(self, x: float) -> float:
        """!
        @brief Returns the value of the model at position `x`: the uniform cubic B-spline of the control points.
        @param x the time.
        @return the model value (`NaN` if no data has been assimilated).
        """
        if not self.control_points:
            return math.nan
        s = (x - self.left_endpoint) / self.step
        i = math.floor(s)
        u = s - i
        u2 = u * u
        u3 = u2 * u
        return ((1 - u) * (1 - u) * (1 - u) * self._point(i - 1)
                + (3 * u3 - 6 * u2 + 4) * self._point(i)
                + (-3 * u3 + 3 * u2 + 3 * u + 1) * self._point(i + 1)
                + u3 * self._point(i + 2)) / 6.0

    def evaluate(self, t: float) -> float:
        """!
        @brief Returns the value of the model at time `t`: the B-spline, compared with the measured sine values by
        `DT_DMS.check_models_versus_data`.
        @param t the time.
        @return get_sine_model_value(t).
        """
        return self.get_sine_model_value(t)

    def _point(self, i: int) -> float:
        """! @return the control point of index `i`; the first and last points are repeated at the ends."""
        return self.control_points[max(0, min(len(self.control_points) - 1, i))]

    def get_last_time(self) -> float:
        """! @return the time of the last value assimilated (`NaN` if none)."""
        if not self.control_points:
            return math.nan
        return self.left_endpoint + (len(self.control_points) - 1) * self.step

    def get_left_endpoint(self) -> float:
        """! @return the left endpoint of the domain."""
        return self.left_endpoint

    def get_step(self) -> float:
        """! @return the step size between samples."""
        return self.step
