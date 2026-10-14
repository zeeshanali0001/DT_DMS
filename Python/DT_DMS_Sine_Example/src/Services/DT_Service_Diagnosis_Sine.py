# DT-DMS sine example (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Service_Diagnosis_Sine.py
@brief Diagnosis service of the sine example.
"""

import math
import random
from typing import List

from Automaton.DT_Automaton import Event, Initialization, Phase, Transition, TransitionKind, format_double, \
    value_to_string
from Models.DT_Model_Descr_CubicBSpline import DT_Model_Descr_CubicBSpline
from Services.DT_Service_Diagnosis import DT_Service_Diagnosis


def _round3(t: float) -> float:
    """! @return `t` rounded to 3 decimal places, half up as Java's `Math.round`."""
    return math.floor(t * 1000.0 + 0.5) / 1000.0


class DT_Service_Diagnosis_Sine(DT_Service_Diagnosis):
    """!
    @brief Diagnosis service of the sine example (`s ∈ S_diag`): it detects the anomalies of the measured sine
    wave.

    The service is coupled to the Cubic B-Spline model and consumes the latest measured value: `s(m, ω)`
    compares the value `d` of `ω` with the value expected by the model at the same time; the difference is the
    residual. An anomaly is reported when the residual exceeds the threshold.

    It can also check the interpolation itself at random points between the samples (perform_random_check()),
    against the exact sine function.

    The service is specified as the automaton `𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`:
    - `Λ` = {threshold}: the largest residual considered normal.
    - `X` = {data}: a measured sine value.
    - `Y` = {anomaly}: the residual of an anomalous value.
    - `Σ` = {residual}: the residual of the last value checked.
    - `Φ` = {IDLE ⟨residual ≥ 0, NIL, ∞⟩, CHECK ⟨residual ≥ 0, NIL, 0⟩}.
    - `Δ^in`: IDLE → CHECK on `(data, d)`, Update: `residual := |d − m(t)|`.
    - `Δ^time`: CHECK → IDLE, Guard: `residual > threshold`, Send: `(anomaly, residual)`.
    - `Δ^time`: CHECK → IDLE, Guard: `residual ≤ threshold`, no output.
    - `Δ*` = ⟨IDLE, NIL, 0⟩.
    """

    def __init__(self, threshold: float):
        """!
        @brief Constructor of the sine diagnosis service.
        @param threshold the largest residual considered normal.
        """
        super().__init__("Sine diagnosis", "Detects the anomalies of the measured sine wave", -1.0, 1.0)
        ## Threshold for anomaly detection (residual between the measured value and the model).
        self.threshold: float = threshold
        ## The times at which an anomaly was detected.
        self.anomaly_times: List[float] = []
        self._specify_automaton()

    def _specify_automaton(self) -> None:
        """! Specifies the 7-tuple `𝒮` of the service and runs its initialization `Δ*`."""
        self.add_parameter("threshold", self.threshold)                               # Λ
        self.add_input("data")                                                        # X
        self.add_output("anomaly")                                                    # Y
        self.add_state_variable("residual", 0.0)                                      # Σ
        self.add_phase(Phase("IDLE", lambda q: q["residual"] >= 0, None, math.inf))   # Φ
        self.add_phase(Phase("CHECK", lambda q: q["residual"] >= 0, None, 0.0))

        def store_residual(q, x):
            q["residual"] = self.residual_of(x.value)
            return q

        self.add_transition(Transition(TransitionKind.IN, "IDLE", "CHECK",           # Δ^in
                                       {"data"}, None, None, store_residual))
        self.add_transition(Transition(TransitionKind.TIME, "CHECK", "IDLE",         # Δ^time (anomaly)
                                       None, lambda q: q["residual"] > self.threshold,
                                       lambda q: [Event("anomaly", q["residual"])], None))
        self.add_transition(Transition(TransitionKind.TIME, "CHECK", "IDLE",         # Δ^time (normal)
                                       None, lambda q: q["residual"] <= self.threshold, None, None))
        self.set_initialization(Initialization("IDLE", None, 0.0))                   # Δ*
        self.initialize()

    def residual_of(self, d: float) -> float:
        """!
        @brief Computes the residual of a measured value: its distance to the value expected by the coupled
        Cubic B-Spline model at the time of the last value assimilated.
        @param d the measured value.
        @return the residual `|d − m(t)|` (0 if the service is not coupled to a Cubic B-Spline model).
        """
        if isinstance(self.model, DT_Model_Descr_CubicBSpline):
            return abs(d - self.model.get_sine_model_value(self.model.get_last_time()))
        return 0.0

    def execute_service(self) -> str:
        """!
        @brief Executes the diagnosis on the latest value consumed: the automaton goes IDLE → CHECK → IDLE and an
        anomaly is reported if it emits the output `(anomaly, residual)`.
        @return the diagnosis report.
        """
        spline = self.model
        if not self.consumed_data or not isinstance(spline, DT_Model_Descr_CubicBSpline):
            self.diagnosis_report = "Diagnosis failed: no data or no Cubic B-Spline model."
            return self.diagnosis_report
        d = self.consumed_data[-1]
        self.external_transition(Event("data", d), spline.get_step())
        emitted = self.internal_transition()
        if not emitted:
            self.diagnosis_report = "OK"
        else:
            t = _round3(spline.get_last_time())
            self.anomaly_times.append(t)
            self.diagnosis_report = ("ANOMALY DETECTED at t=" + format_double(t) + ": actual=" + format_double(d)
                                     + ", expected=" + format_double(spline.get_sine_model_value(spline.get_last_time()))
                                     + ", residual=" + value_to_string(emitted[0].value))
        return self.diagnosis_report

    def perform_random_check(self, number_of_points_to_check: int, seed: int, verbose: bool) -> float:
        """!
        @brief Checks the interpolation of the sine function at random points: draws `number_of_points_to_check`
        abscissas `x` uniformly in the domain of the model and compares `sin(x)` with the value of the B-spline at
        `x`. Unlike execute_service(), which checks the measured values, this check uses the exact sine function
        and points between the samples.
        @param number_of_points_to_check the number of random points.
        @param seed    the seed of the random generator (the same seed gives the same points).
        @param verbose `True` to print one line per point.
        @return the largest deviation `|sin(x) − m(x)|` found (`NaN` if no Cubic B-Spline model with data is
                coupled to the service).
        """
        spline = self.model
        if not isinstance(spline, DT_Model_Descr_CubicBSpline) or math.isnan(spline.get_last_time()):
            return math.nan
        generator = random.Random(seed)
        start, end = spline.get_left_endpoint(), spline.get_last_time()
        max_deviation = 0.0
        for _ in range(number_of_points_to_check):
            x = start + generator.random() * (end - start)
            expected = math.sin(x)
            interpolated = spline.get_sine_model_value(x)
            max_deviation = max(max_deviation, abs(expected - interpolated))
            if verbose:
                print("     sin(%.6f) = %+.9f    spline interpolation gives %+.9f    delta = %.3e"
                      % (x, expected, interpolated, expected - interpolated))
        return max_deviation

    def get_threshold(self) -> float:
        """! @return the threshold for anomaly detection."""
        return self.threshold

    def get_anomaly_times(self) -> List[float]:
        """! @return the times at which an anomaly was detected."""
        return self.anomaly_times

    def anomaly_times_to_string(self) -> str:
        """! @return the anomaly times as text: `[t1, t2, ...]`."""
        return "[" + ", ".join(format_double(t) for t in self.anomaly_times) + "]"
