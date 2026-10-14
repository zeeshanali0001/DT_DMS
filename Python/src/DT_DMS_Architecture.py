# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_DMS_Architecture.py
@brief A Digital Twin that connects one component of each kind of the DT-DMS framework, and an example run.

Run it from the project folder: `python src/DT_DMS_Architecture.py`.
"""

import math
import os
import sys
from typing import Optional, TypeVar

from Automaton.DT_Automaton import (DT_Automaton, Event, Initialization, Phase, Transition, TransitionKind,
                                    map_to_string, value_to_string)
from Data.DT_Data import DT_Data
from Data.DT_DataSegment import DT_DataSegment
from DT_DMS import DT_DMS
from Models.DT_Model_Descriptive import DT_Model_Descriptive
from Models.DT_Model_Predictive import DT_Model_Predictive
from Models.DT_Model_Prescriptive import DT_Model_Prescriptive
from Mu.DT_Mu import DT_Mu
from Services.DT_Service_Control import DT_Service_Control
from Services.DT_Service_Diagnosis import DT_Service_Diagnosis
from Services.DT_Service_Monitoring import DT_Service_Monitoring
from Services.DT_Service_Optimization import DT_Service_Optimization
from Services.DT_Service_Prediction import DT_Service_Prediction

T = TypeVar("T")


class DT_DMS_Architecture(DT_DMS):
    """!
    @brief The `DT_DMS_Architecture` class connects all classes of the DT-DMS framework.

    It is a Digital Twin (DT_DMS) with one named component of each kind: one data source, the three types of
    models, the five types of services and the data assimilation function. Its collaboration diagram (generated
    by Doxygen/Graphviz) is the architecture image of the framework.

    Assign the fields, then call connect() so that every component belongs to `D`, `M` or `S` and is reached by
    `μ`. The example run is main().
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, delta: float):
        """!
        @brief Constructs the DT architecture.
        @param delta the freshness threshold `δ`.
        """
        super().__init__(delta)
        ## The data source (`D`).
        self.data: DT_Data = None
        ## The descriptive model (`M_desc`).
        self.descriptive_model: DT_Model_Descriptive = None
        ## The predictive model (`M_pred`).
        self.predictive_model: DT_Model_Predictive = None
        ## The prescriptive model (`M_presc`).
        self.prescriptive_model: DT_Model_Prescriptive = None
        ## The monitoring service (`S_moni`).
        self.monitoring: DT_Service_Monitoring = None
        ## The prediction service (`S_pred`).
        self.prediction: DT_Service_Prediction = None
        ## The diagnosis service (`S_diag`).
        self.diagnosis: DT_Service_Diagnosis = None
        ## The optimization service (`S_opti`).
        self.optimization: DT_Service_Optimization = None
        ## The control service (`S_cont`).
        self.control: DT_Service_Control = None
        ## The model inference or data assimilation function (`μ`); the same object as get_mu().
        self.assimilation: DT_Mu = self.mu

    def connect(self) -> None:
        """!
        @brief Adds every assigned field to the Digital Twin. Each service is coupled to a model of the matching
        type: monitoring and diagnosis to the descriptive model, prediction to the predictive model, optimization
        and control to the prescriptive model.
        """
        if self.data is not None:
            self.add_data(self.data)
        for m in (self.descriptive_model, self.predictive_model, self.prescriptive_model):
            if m is not None:
                self.add_model(m)
        for s, m in ((self.monitoring, self.descriptive_model), (self.prediction, self.predictive_model),
                     (self.diagnosis, self.descriptive_model), (self.optimization, self.prescriptive_model),
                     (self.control, self.prescriptive_model)):
            if s is not None:
                self.add_service(s, m)


def _list_to_string(segments) -> str:
    """! @return a list of segments as text, as Java prints a list: `[ω{...}, ...]`."""
    return "[" + ", ".join(repr(s) for s in segments) + "]"


def main() -> None:
    """! @brief Example run of the framework on a temperature Digital Twin. Replace it with your own example."""
    print("Welcome to DMS Framework.")

    # ---- DT = ⟨D, M, S, μ⟩ with a simulated clock: t_now = 100, δ = 5 → real-time window [95, 100]
    now = [100.0]
    dt = DT_DMS_Architecture(5.0)
    dt.set_name("Temperature")
    dt.set_clock(lambda: now[0])

    # D, with the characterization of its data
    data = DT_Data("Sensor A", "Temperature")
    data.get_characterization().set_unit("Celsius")
    data.get_characterization().set_signal_type("digital")
    data.get_characterization().set_data_flow_type("periodic")
    data.get_characterization().set_sensor_precision("±0.1 °C")
    data.get_characterization().set_sensor_calibration_status("calibrated")
    # M = M_desc ∪ M_pred ∪ M_presc
    descriptive = DT_Model_Descriptive("Temperature summary", "Sensor A")
    predictive = DT_Model_Predictive("Temperature forecast", "Moving average", 3)
    prescriptive = DT_Model_Prescriptive("Temperature setpoint", "Keep temperature at 22", 22.0,
                                         "The heater reacts linearly")
    # S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont
    monitoring = DT_Service_Monitoring("Temperature monitoring", None, "Sensor A", 10.0, 30.0)

    dt.data = data
    dt.descriptive_model = descriptive
    dt.predictive_model = predictive
    dt.prescriptive_model = prescriptive
    dt.monitoring = monitoring
    dt.prediction = DT_Service_Prediction("Temperature prediction", None)
    dt.diagnosis = DT_Service_Diagnosis("Temperature diagnosis", None, 10.0, 30.0)
    dt.optimization = DT_Service_Optimization("Setpoint optimization", None, "Rule-based")
    dt.control = DT_Service_Control("Heater control", None, "Proportional", 0.5)
    dt.connect()
    print(dt)

    # ---- μ: real-time data is assimilated synchronously, historical data asynchronously
    print("\n-- Data assimilation (μ)")
    data.update_data(99.0, 21.5)        # 99 ≥ 100 − 5 → real-time, assimilated at once
    data.update_data(80.0, 19.0)        # 80 < 100 − 5 → historical, queued
    print("Assimilated by the predictive model: " + predictive.history_to_string()
          + ", waiting: " + str(dt.get_mu().get_pending_count()))
    dt.get_mu().flush()                 # asynchronous assimilation of the queue
    print("After flush(): " + predictive.history_to_string())

    # ---- a mixed segment ω = ω_hist ⊕ ω_rt is split at t_now − δ = 95
    omega = DT_DataSegment()
    omega.put(90.0, 20.0)
    omega.put(96.0, 21.0)
    omega.put(98.0, 35.0)
    print("ω = " + repr(omega) + " is " + omega.kind(dt.t_now(), dt.get_delta())
          + ": ω_hist = " + repr(omega.historical_part(dt.t_now(), dt.get_delta()))
          + ", ω_rt = " + repr(omega.real_time_part(dt.t_now(), dt.get_delta())))
    dt.assimilate(omega)                # policy SPLIT: ω_rt at once, ω_hist queued
    dt.get_mu().flush()

    # ---- D = D_hist ∪ D_rt
    print("\n-- Data (D)")
    print("D_rt   = " + _list_to_string(dt.get_real_time_data()))
    print("D_hist = " + _list_to_string(dt.get_historical_data()))

    # ---- iterator pattern over the models
    print("\n-- Models (M)")
    dt.begin_models()
    while not dt.end_models():
        dt.current_model().execute_model()
        dt.next_model()

    # ---- services s : M × Ω_D → Y_s, run on the real-time segment ω_rt
    print("\n-- Services (S)")
    real_time = omega.real_time_part(dt.t_now(), dt.get_delta())
    dt.begin_services()
    while not dt.end_services():
        print(dt.current_service().get_service_name() + " → "
              + value_to_string(dt.run_service(dt.current_service(), real_time)))
        dt.next_service()

    # ---- the monitoring service specified as an automaton 𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩
    print("\n-- Automaton of the monitoring service")
    monitoring.add_parameter("upperThreshold", 30.0)                                  # Λ
    monitoring.add_input("temperature")                                               # X
    monitoring.add_output("alert")                                                    # Y
    monitoring.add_state_variable("value", 0.0)                                       # Σ
    monitoring.add_phase(Phase("NORMAL", None, None, math.inf))                       # Φ
    monitoring.add_phase(Phase("ALARM", None, None, 0.0))

    def store_value(q, x):
        q["value"] = x.value
        return q

    monitoring.add_transition(Transition(TransitionKind.IN, "NORMAL", "ALARM",        # Δ^in
                                         {"temperature"}, None, None, store_value))
    monitoring.add_transition(Transition(TransitionKind.TIME, "ALARM", "NORMAL",      # Δ^time
                                         None, None, lambda q: [Event("alert", q["value"])], None))
    monitoring.set_initialization(Initialization("NORMAL", None, 0.0))               # Δ*
    monitoring.initialize()
    print("Phase: " + monitoring.get_current_phase())
    monitoring.external_transition(Event("temperature", 35.0), 1.0)
    print("Phase after input (temperature, 35.0): " + monitoring.get_current_phase()
          + ", state " + map_to_string(monitoring.get_state()))
    outputs = monitoring.internal_transition()
    print("Output on internal transition: " + repr(outputs) + ", phase: " + monitoring.get_current_phase())

    # ---- validity of the DT: services versus models, models versus data, data versus services
    print("\n-- Validity")
    print("Consistent: " + value_to_string(dt.check_consistency(sys.stdout)))

    # ---- the DT draws its own Data-Models-Services diagram (Graphviz)
    print("\n-- Graphviz")
    dot_file = "docs/DT_DMS_Temperature.dot" if os.path.isdir("docs") else "DT_DMS_Temperature.dot"
    try:
        dt.dump_graphviz(dot_file)
        print("Written " + dot_file + " (render it with: dot -Tpng " + dot_file + " -o DT_DMS_Temperature.png)")
    except OSError as e:
        print("Cannot write " + dot_file + ": " + str(e))


if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")  # the symbols μ, δ, ω, ⟨⟩ on the Windows console
    main()
