# DT-DMS sine example (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_DMS_SineCubicBP.py
@brief Main class of the Sine Wave Cubic B-Spline Digital Twin example.

Run it from the project folder: `python src/DT_DMS_SineCubicBP.py` (add `nochart` to run without the chart).
"""

import math
import os
import sys
from typing import Optional

from Automaton.DT_Automaton import format_double, map_to_string, value_to_string
from Data.DT_Data_SineValues import DT_Data_SineValues
from Data.DT_DataSegment import DT_DataSegment
from DT_DMS import DT_DMS
from Models.DT_Model_Descr_CubicBSpline import DT_Model_Descr_CubicBSpline
from Mu.DT_Mu import DT_Mu
from Services.DT_Service_Diagnosis_Sine import DT_Service_Diagnosis_Sine
from Visualization.DT_ChartPanel import DT_ChartPanel


class DT_DMS_SineCubicBP(DT_DMS):
    """!
    @brief Main class of the Sine Wave Cubic B-Spline Digital Twin example: a minimal "Hello World" Digital Twin
    `DT = ⟨D, M, S, μ⟩` built with the DT-DMS framework.
    - `D`: the sine values measured on the ToI (DT_Data_SineValues).
    - `M`: one descriptive model, the Cubic B-Spline (DT_Model_Descr_CubicBSpline).
    - `S`: one diagnosis service (DT_Service_Diagnosis_Sine), coupled to the model.
    - `μ`: the data assimilation function of the framework; it observes the data and updates the model.

    Information flow for each sample: Data → μ → Model → Diagnosis, with live charting and anomaly detection.
    The model and the service are both specified as automata (see their classes).
    """

    ## Step size between two samples.
    STEP = 0.05
    ## Freshness threshold `δ`: data measured during the last time unit is real-time.
    DELTA = 1.0
    ## Largest residual considered normal by the diagnosis service.
    ANOMALY_THRESHOLD = 0.05

    def __init__(self, show_chart: bool):
        """!
        @brief Builds the Digital Twin: creates the data, the model and the service and connects them.
        @param show_chart `True` to show the live chart.
        """
        super().__init__("Sine", self.DELTA)
        ## The simulated current time `t_now`.
        self._now: float = 0.0
        self.set_clock(lambda: self._now)
        ## The data assimilation function (`μ`); the same object as get_mu().
        self.assimilation: DT_Mu = self.mu

        ## The sine data source (`D`).
        self.sine_data: DT_Data_SineValues = DT_Data_SineValues("Sine values")
        self.sine_data.get_characterization().set_unit("dimensionless")
        self.sine_data.get_characterization().set_signal_type("digital")
        self.sine_data.get_characterization().set_data_flow_type("periodic")
        ## The Cubic B-Spline model (`M`).
        self.spline_model: DT_Model_Descr_CubicBSpline = DT_Model_Descr_CubicBSpline(self.STEP, 0.0)
        ## The diagnosis service (`S`).
        self.diagnosis: DT_Service_Diagnosis_Sine = DT_Service_Diagnosis_Sine(self.ANOMALY_THRESHOLD)

        self.add_data(self.sine_data)                         # μ observes the data
        self.add_service(self.diagnosis, self.spline_model)   # adds the model to M and couples the service to it

        ## The live chart (`None` if not shown).
        self.chart: DT_ChartPanel = DT_ChartPanel() if show_chart else None

    def execute(self, nb_val: int) -> None:
        """!
        @brief Runs the simulation: measures `nb_val` sine values, one per step. For each value:
        1. the data source measures the ToI at `t_now`;
        2. `μ` assimilates the real-time datum synchronously into the model;
        3. the diagnosis service is run on the datum, `s(m, ω)`;
        4. the chart is updated.

        At the end, the interpolation is checked at random points and the validity of the DT is checked.
        @param nb_val the number of values to measure.
        """
        print("Sine_DMS::execute() begin")
        print(self)

        for index in range(nb_val):
            self._now = math.floor(index * self.STEP * 1000.0 + 0.5) / 1000.0   # t_now, rounded to 3 decimals

            sine_value = self.sine_data.measure(self._now)                       # D, then μ(ω, m) = m'
            report = self.run_service(self.diagnosis, DT_DataSegment(self._now, sine_value))   # s(m, ω)

            if index % 10 == 0 or report != "OK":
                print("Data index " + str(index) + " (t=" + format_double(self._now) + "), sine value = "
                      + format_double(sine_value) + ", model = "
                      + format_double(self.spline_model.get_sine_model_value(self._now)) + " → " + report)
            if self.chart is not None:
                self.chart.update_chart(index, sine_value, self.spline_model.get_sine_model_value(self._now))

        print("\nSine_DMS::execute() end")
        self.spline_model.execute_model()
        print("Anomalies detected at t = " + self.diagnosis.anomaly_times_to_string())
        print("D_rt   (t ≥ t_now − δ): " + str(self.sine_data.get_real_time_data(self.DELTA).size()) + " values")
        print("D_hist (t < t_now − δ): " + str(self.sine_data.get_historical_data(self.DELTA).size()) + " values")
        print("Model automaton:   phase " + self.spline_model.get_current_phase() + ", state "
              + map_to_string(self.spline_model.get_state()))
        print("Service automaton: phase " + self.diagnosis.get_current_phase() + ", state "
              + map_to_string(self.diagnosis.get_state()))

        # check of the interpolation between the samples, against the exact sine function
        print("\nRandom check of the interpolation (10 points between the samples):")
        max_deviation = self.diagnosis.perform_random_check(10, 42, True)
        print("Largest deviation |sin(x) − m(x)| = " + format_double(max_deviation))

        # validity of the DT
        print()
        print("Consistent: " + value_to_string(self.check_consistency(sys.stdout)))

    def write_graphviz(self) -> None:
        """!
        @brief Writes the Data-Models-Services diagram of the Digital Twin (DT_DMS.dump_graphviz) in the `docs`
        folder if it exists, else in the current folder.
        """
        dot_file = "docs/DT_DMS_Sine.dot" if os.path.isdir("docs") else "DT_DMS_Sine.dot"
        try:
            self.dump_graphviz(dot_file)
            print("Written " + dot_file + " (render it with: dot -Tpng " + dot_file + " -o DT_DMS_Sine.png)")
        except OSError as e:
            print("Cannot write " + dot_file + ": " + str(e))


def main(args) -> None:
    """!
    @brief Runs the example. Pass the argument `nochart` to run without the live chart.
    @param args optional: `nochart`.
    """
    show_chart = not (args and args[0].lower() == "nochart") and DT_ChartPanel.available()

    dt_system = DT_DMS_SineCubicBP(show_chart)
    dt_system.sine_data.inject_anomaly(3.0, 0.5)      # a spike on the ToI, to be found by the diagnosis
    dt_system.execute(100)
    dt_system.write_graphviz()
    if dt_system.chart is not None:
        dt_system.chart.keep_open()


if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")  # the symbols μ, δ, ω, ⟨⟩ on the Windows console
    main(sys.argv[1:])
