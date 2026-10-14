# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_DMS.py
@brief The general class of the DT-DMS framework: `DT = ⟨D, M, S, μ⟩`.
"""

import math
import sys
import time
from typing import Any, Callable, List, Optional, TextIO, TypeVar

from Automaton.DT_Automaton import format_double, numeric_value
from Data.DT_Data import DT_Data
from Data.DT_DataSegment import DT_DataSegment
from Models.DT_Model import DT_Model
from Models.DT_Model_Descriptive import DT_Model_Descriptive
from Models.DT_Model_Predictive import DT_Model_Predictive
from Models.DT_Model_Prescriptive import DT_Model_Prescriptive
from Mu.DT_Mu import DT_Mu
from Services.DT_Service import DT_Service
from Services.DT_Service_Control import DT_Service_Control
from Services.DT_Service_Diagnosis import DT_Service_Diagnosis
from Services.DT_Service_Monitoring import DT_Service_Monitoring
from Services.DT_Service_Optimization import DT_Service_Optimization
from Services.DT_Service_Prediction import DT_Service_Prediction

T = TypeVar("T")

_MODEL_TYPES = (DT_Model_Descriptive, DT_Model_Predictive, DT_Model_Prescriptive)
_SERVICE_TYPES = (DT_Service_Monitoring, DT_Service_Prediction, DT_Service_Diagnosis, DT_Service_Optimization,
                  DT_Service_Control)


class DT_DMS:
    """!
    @brief The `DT_DMS` class is the general class of the Digital Twin Data-Models-Services (DT-DMS) framework.

    It holds the formal specification of a Digital Twin:

        DT = ⟨ D, M, S, μ ⟩

    Key features of the class include:
    - `D`: the ToI data (DT_Data), `D = D_hist ∪ D_rt`.
    - `M`: the models of the ToI (DT_Model), `M = M_desc ∪ M_pred ∪ M_presc`.
    - `S`: the DT services (DT_Service), `S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont`, with
      `s : M × Ω_D → Y_s`.
    - `μ`: the model inference or data assimilation function (DT_Mu), `μ : Ω_D × M → M`.
    - It connects the Data, Models and Services of the Digital Twin and manages the information flow between
      them: `μ` observes every data source and updates every model, and each model notifies the services
      coupled to it.
    - The iterator design pattern over the data, the models and the services (`begin_…()`, `current_…()`,
      `next_…()`, `end_…()`).
    - Validity checks of the DT: services versus models, models versus data and data versus services
      (check_consistency()).
    - The export of its own Data-Models-Services diagram in the Graphviz format (dump_graphviz()).

    The components are held by composition: data, models, services and `μ` are parts of the DT.
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, *args):
        """!
        @brief Constructs a Digital Twin `⟨D, M, S, μ⟩`, in one of three ways (as the Java constructors):
        - `DT_DMS(delta)`: an empty Digital Twin;
        - `DT_DMS(name, delta)`: an empty named Digital Twin;
        - `DT_DMS(other)`: a copy of `other`, with the same data, models and services. The lists are copied but
          the components themselves are shared, and so are `μ`, `δ` and the clock, so that every datum is still
          assimilated once into each model.

        `delta` is the freshness threshold `δ` (same time unit as the data measurement times).
        """
        if len(args) == 1 and isinstance(args[0], DT_DMS):
            other = args[0]
            ## The name of the Digital Twin.
            self.name: str = other.name
            ## `D`: the data sources of the ToI.
            self.data_list: List[DT_Data] = list(other.data_list)
            ## `M`: the models of the ToI.
            self.model_list: List[DT_Model] = list(other.model_list)
            ## `S`: the DT services.
            self.service_list: List[DT_Service] = list(other.service_list)
            ## The freshness threshold `δ ∈ ℝ+`: the data scope during which data is considered real-time.
            self.delta: float = other.delta
            ## Gives the current time `t_now ∈ T_U` (default: system time in seconds).
            self.clock: Callable[[], float] = other.clock
            ## `μ`: the model inference or data assimilation function.
            self.mu: DT_Mu = other.mu
        else:
            if len(args) == 1:
                name, delta = "Digital Twin", args[0]
            elif len(args) == 2:
                name, delta = args
            else:
                raise TypeError("DT_DMS(delta), DT_DMS(name, delta) or DT_DMS(other)")
            self.name = name
            self.data_list = []
            self.model_list = []
            self.service_list = []
            self.delta = float(delta)
            self.clock = time.time
            self.mu = DT_Mu(self.delta, self.t_now)
        ## Position of the iterator over the data.
        self._data_iterator: int = 0
        ## Position of the iterator over the models.
        self._model_iterator: int = 0
        ## Position of the iterator over the services.
        self._service_iterator: int = 0

    # ---------------------------------------------------------------- time

    def t_now(self) -> float:
        """! @return the current time `t_now`."""
        return self.clock()

    def set_clock(self, clock: Callable[[], float]) -> None:
        """!
        @brief Replaces the clock giving `t_now` (e.g., a simulated clock).
        @param clock the new clock.
        """
        self.clock = clock

    def get_delta(self) -> float:
        """! @return the freshness threshold `δ`."""
        return self.delta

    # ---------------------------------------------------------------- name

    def get_name(self) -> str:
        """! @return the name of the Digital Twin."""
        return self.name

    def set_name(self, name: str) -> None:
        """! @param name the new name of the Digital Twin."""
        self.name = name

    # ---------------------------------------------------------------- D

    def add_data(self, d: DT_Data) -> None:
        """!
        @brief Adds a data source to `D`; it shares the clock of the DT and `μ` starts observing it.
        @param d the data source.
        """
        if d not in self.data_list:
            self.data_list.append(d)
        d.set_clock(self.t_now)
        self.mu.observe(d)

    def get_real_time_data(self) -> List[DT_DataSegment]:
        """! @return `D_rt`: for each data source, its data measured at `t ≥ t_now − δ`."""
        return [d.get_real_time_data(self.delta) for d in self.data_list]

    def get_historical_data(self) -> List[DT_DataSegment]:
        """! @return `D_hist`: for each data source, its data measured at `t < t_now − δ`."""
        return [d.get_historical_data(self.delta) for d in self.data_list]

    # ---------------------------------------------------------------- M

    def add_model(self, m: DT_Model) -> None:
        """!
        @brief Adds a model to `M`; it will be updated by `μ`.
        @param m the model.
        @exception ValueError if `m` is not descriptive, predictive or prescriptive.
        """
        if not isinstance(m, _MODEL_TYPES):
            raise ValueError("A model must be descriptive, predictive or prescriptive: " + repr(m))
        if m not in self.model_list:
            self.model_list.append(m)
        self.mu.add_model(m)

    def get_descriptive_models(self) -> List[DT_Model_Descriptive]:
        """! @return `M_desc`: the descriptive models."""
        return [m for m in self.model_list if isinstance(m, DT_Model_Descriptive)]

    def get_predictive_models(self) -> List[DT_Model_Predictive]:
        """! @return `M_pred`: the predictive models."""
        return [m for m in self.model_list if isinstance(m, DT_Model_Predictive)]

    def get_prescriptive_models(self) -> List[DT_Model_Prescriptive]:
        """! @return `M_presc`: the prescriptive models."""
        return [m for m in self.model_list if isinstance(m, DT_Model_Prescriptive)]

    # ---------------------------------------------------------------- S

    def add_service(self, s: DT_Service, m: Optional[DT_Model]) -> None:
        """!
        @brief Adds a service to `S` and couples it to the model `m`: the service will be notified each time `μ`
        updates `m` (`DT_Model.add_service_to_be_updated`).
        @param s the service.
        @param m the model the service is coupled to (added to `M` if needed), or `None` for a purely data-driven
                 service.
        @exception ValueError if `s` is not one of the five service types.
        """
        if not isinstance(s, _SERVICE_TYPES):
            raise ValueError("A service must be monitoring, prediction, diagnosis, optimization or control: "
                             + repr(s))
        if s.get_model() is not None and s.get_model() is not m:
            s.get_model().remove_service_to_be_updated(s)
        if m is not None:
            self.add_model(m)
            m.add_service_to_be_updated(s)
        s.set_model(m)
        if s not in self.service_list:
            self.service_list.append(s)

    def run_service(self, s: DT_Service, omega: Optional[DT_DataSegment]) -> Any:
        """!
        @brief Runs a service with the model it is coupled to: `s(m, ω) ∈ Y_s`.
        @param s     the service.
        @param omega the data segment it consumes (may be `None`).
        @return the output of the service.
        """
        return s.execute(s.get_model(), omega)

    def get_monitoring_services(self) -> List[DT_Service_Monitoring]:
        """! @return `S_moni`: the monitoring services."""
        return [s for s in self.service_list if isinstance(s, DT_Service_Monitoring)]

    def get_prediction_services(self) -> List[DT_Service_Prediction]:
        """! @return `S_pred`: the prediction services."""
        return [s for s in self.service_list if isinstance(s, DT_Service_Prediction)]

    def get_diagnosis_services(self) -> List[DT_Service_Diagnosis]:
        """! @return `S_diag`: the diagnosis services."""
        return [s for s in self.service_list if isinstance(s, DT_Service_Diagnosis)]

    def get_optimization_services(self) -> List[DT_Service_Optimization]:
        """! @return `S_opti`: the optimization services."""
        return [s for s in self.service_list if isinstance(s, DT_Service_Optimization)]

    def get_control_services(self) -> List[DT_Service_Control]:
        """! @return `S_cont`: the control services."""
        return [s for s in self.service_list if isinstance(s, DT_Service_Control)]

    # ---------------------------------------------------------------- μ

    def get_mu(self) -> DT_Mu:
        """! @return the model inference or data assimilation function `μ`."""
        return self.mu

    def assimilate(self, omega: DT_DataSegment) -> None:
        """!
        @brief Assimilates a data segment into the models of the DT, according to its kind at `t_now`
        (real-time, historical or mixed).
        @param omega the data segment `ω`.
        """
        self.mu.assimilate(omega)

    # ---------------------------------------------------------------- validity

    def check_services_versus_models(self, out: TextIO = sys.stdout) -> bool:
        """!
        @brief Checks the consistency of the services with respect to the models they are coupled to.
        - A coupled model must belong to `M`.
        - A prediction service coupled to a model needs a predictive model.
        - An optimization or a control service needs a prescriptive model (it cannot run without it).
        - Monitoring and diagnosis services can use any model, or none (data-driven services).
        @param out where the report is written.
        @return `True` if every service is consistent with its model.
        """
        ok = True
        print("Services versus models:", file=out)
        for s in self.service_list:
            m = s.get_model()
            problem = None
            if m is not None and m not in self.model_list:
                problem = "its model " + m.get_model_name() + " is not in M"
            elif isinstance(s, (DT_Service_Optimization, DT_Service_Control)) \
                    and not isinstance(m, DT_Model_Prescriptive):
                problem = "it needs a prescriptive model"
            elif isinstance(s, DT_Service_Prediction) and m is not None and not isinstance(m, DT_Model_Predictive):
                problem = "it is coupled to a model that is not predictive"
            model = "no model (data-driven)" if m is None else m.get_model_name()
            if problem is None:
                print("  OK    " + s.get_service_name() + " → " + model, file=out)
            else:
                print("  ERROR " + s.get_service_name() + " → " + model + ": " + problem, file=out)
                ok = False
        return ok

    def check_models_versus_data(self, out: TextIO = sys.stdout, epsilon: float = 1e-3, big_m: float = 0.1) -> bool:
        """!
        @brief Checks the fitting of the data versus the models: for every data source `D_i` and every model `M_j`
        that gives values in time (`DT_Model.evaluate`), computes `δ = |ω(t) − m(t)|` over the real-time data
        of the source.
        - If `δ < ε` for every datum, the model fits the data very well.
        - If the mean of `δ` is greater than `big_m`, the model does not fit the data well and should be updated,
          re-parameterized or discarded.
        - Otherwise the fitting is acceptable.

        Models that give no value in time are skipped.
        @param out     where the report is written.
        @param epsilon the tolerance `ε` under which a datum is fitted very well.
        @param big_m   the mean deviation `M` over which the model does not fit the data.
        @return `False` if some model does not fit some data source.
        """
        ok = True
        print("Models versus data (ε=" + format_double(epsilon) + ", M=" + format_double(big_m) + "):", file=out)
        for m in self.model_list:
            for d in self.data_list:
                maximum, total, count = 0.0, 0.0, 0
                for t, datum in d.get_real_time_data(self.delta).items():
                    value = numeric_value(datum)
                    expected = m.evaluate(t)
                    if not math.isnan(value) and not math.isnan(expected):
                        deviation = abs(value - expected)
                        maximum = max(maximum, deviation)
                        total += deviation
                        count += 1
                pair = m.get_model_name() + " / " + d.get_source()
                if count == 0:
                    print("  SKIP  " + pair + ": the model gives no value at the real-time data", file=out)
                elif maximum < epsilon:
                    print("  OK    " + pair + ": fits very well (max δ=" + format_double(maximum) + ")", file=out)
                elif total / count > big_m:
                    print("  ERROR " + pair + ": does not fit (mean δ=" + format_double(total / count)
                          + "): update, re-parameterize or discard the model", file=out)
                    ok = False
                else:
                    print("  OK    " + pair + ": acceptable (mean δ=" + format_double(total / count)
                          + ", max δ=" + format_double(maximum) + ")", file=out)
        return ok

    def check_data_versus_services(self, out: TextIO = sys.stdout) -> bool:
        """!
        @brief Checks the data characteristics (formats, units, ...) with respect to the services of the DT.
        - Services need data: `S ≠ ∅ ⇒ D ≠ ∅`.
        - Every data source must be valid (`DT_Data.validate_data`).
        - Warnings (not errors): a data source without unit, and a monitoring service whose monitored entity is
          neither the source nor the type of a data source.
        @param out where the report is written.
        @return `True` if the data can be consumed by the services.
        """
        ok = True
        print("Data versus services:", file=out)
        if self.service_list and not self.data_list:
            print("  ERROR the DT has services but no data", file=out)
            ok = False
        for d in self.data_list:
            if not d.validate_data():
                print("  ERROR " + repr(d) + ": source or data type missing", file=out)
                ok = False
            elif d.get_characterization().get_unit() is None:
                print("  WARN  " + d.get_source() + ": no unit of measurement", file=out)
            else:
                print("  OK    " + d.get_source() + " (" + d.get_data_type() + ", "
                      + d.get_characterization().get_unit() + ")", file=out)
        for s in self.get_monitoring_services():
            found = any(s.get_monitored_entity() is not None and s.get_monitored_entity() in (d.get_source(),
                                                                                              d.get_data_type())
                        for d in self.data_list)
            if not found:
                print("  WARN  " + s.get_service_name() + ": no data source for the monitored entity "
                      + str(s.get_monitored_entity()), file=out)
        return ok

    def check_consistency(self, out: TextIO = sys.stdout) -> bool:
        """!
        @brief Runs the three validity checks of the DT: services versus models, models versus data (with the
        default tolerances) and data versus services.
        @param out where the report is written.
        @return `True` if all the checks succeed.
        """
        services_models = self.check_services_versus_models(out)
        models_data = self.check_models_versus_data(out)
        data_services = self.check_data_versus_services(out)
        return services_models and models_data and data_services

    # ---------------------------------------------------------------- Graphviz

    def to_graphviz(self) -> str:
        """!
        @brief Returns the Data-Models-Services diagram of the DT in the Graphviz format: one node lists the data
        sources, one the models and one the services; `μ` links the data to the models.
        Render it with `dot -Tpng file.dot -o file.png`.
        @return the diagram, in the dot language.
        """
        data = [d.get_source() + " (" + d.get_data_type() + ")" for d in self.data_list]
        models = [m.get_model_name() for m in self.model_list]
        services = [s.get_service_name() for s in self.service_list]
        dot = ['digraph "DMS Framework for the Digital Twin ' + self._escape(self.name) + '" {',
               "nodesep=0.3;", "center=true;", "rankdir=LR;",
               'node [shape=box, style=rounded, fontname="Arial"];',
               self._block("a", "DATA", data, "red"),
               self._block("b", "MODELS", models, "darkgreen"),
               self._block("c", "SERVICES", services, "blue"),
               "a -> b [label=<&#956;>, dir=both];", "c -> b [dir=both];", "a -> c [dir=both];", "}"]
        return "\n".join(dot) + "\n"

    def dump_graphviz(self, out) -> None:
        """!
        @brief Writes the Data-Models-Services diagram of the DT in the Graphviz format (to_graphviz()).
        @param out a file name (`str`), or a text stream where the diagram is written.
        @exception OSError if the file cannot be written.
        """
        if isinstance(out, str):
            with open(out, "w", encoding="utf-8", newline="\n") as file:
                file.write(self.to_graphviz())
        else:
            out.write(self.to_graphviz())

    @staticmethod
    def _block(node_id: str, title: str, lines: List[str], color: str) -> str:
        """! One node of the diagram: a title and one line per element ("(none)" if there is none)."""
        label = " \\n ".join(DT_DMS._escape(line) for line in lines) if lines else "(none)"
        return (node_id + ' [label=" ' + title + " \\n " + label + ' ", color="' + color + '", fontcolor="'
                + color + '"];')

    @staticmethod
    def _escape(s: Optional[str]) -> str:
        """! Escapes the backslashes and double quotes of a name for a Graphviz label."""
        return "" if s is None else s.replace("\\", "\\\\").replace('"', '\\"')

    # ---------------------------------------------------------------- Iterators

    def begin_models(self) -> None:
        """! @brief For the design pattern iterator: initialize the list of models to run."""
        self._model_iterator = 0

    def current_model(self) -> DT_Model:
        """!
        @brief For the design pattern iterator: return the current model.

            dt.begin_models()
            while not dt.end_models():
                print(dt.current_model().get_model_name())
                dt.next_model()
        @return the current model.
        """
        return self.model_list[self._model_iterator]

    def next_model(self) -> None:
        """! @brief For the design pattern iterator: move to the next model."""
        self._model_iterator += 1

    def end_models(self) -> bool:
        """! @return `True` if no other model exists (design pattern iterator)."""
        return self._model_iterator >= len(self.model_list)

    def get_number_of_models(self) -> int:
        """! @return the number of models."""
        return len(self.model_list)

    def get_models_iterator_position(self) -> int:
        """! @return the position of the iterator over the models."""
        return self._model_iterator

    def begin_services(self) -> None:
        """! @brief For the design pattern iterator: initialize the list of services to run."""
        self._service_iterator = 0

    def current_service(self) -> DT_Service:
        """! @return the current service (design pattern iterator)."""
        return self.service_list[self._service_iterator]

    def next_service(self) -> None:
        """! @brief For the design pattern iterator: move to the next service."""
        self._service_iterator += 1

    def end_services(self) -> bool:
        """! @return `True` if no other service exists (design pattern iterator)."""
        return self._service_iterator >= len(self.service_list)

    def get_number_of_services(self) -> int:
        """! @return the number of services."""
        return len(self.service_list)

    def get_services_iterator_position(self) -> int:
        """! @return the position of the iterator over the services."""
        return self._service_iterator

    def begin_data(self) -> None:
        """! @brief For the design pattern iterator: initialize the list of data to run."""
        self._data_iterator = 0

    def current_data(self) -> DT_Data:
        """! @return the current data source (design pattern iterator)."""
        return self.data_list[self._data_iterator]

    def next_data(self) -> None:
        """! @brief For the design pattern iterator: move to the next data source."""
        self._data_iterator += 1

    def end_data(self) -> bool:
        """! @return `True` if no other data source exists (design pattern iterator)."""
        return self._data_iterator >= len(self.data_list)

    def get_number_of_data(self) -> int:
        """! @return the number of data sources."""
        return len(self.data_list)

    def get_data_iterator_position(self) -> int:
        """! @return the position of the iterator over the data."""
        return self._data_iterator

    def __repr__(self) -> str:
        """! @return the name, the sizes of `D`, `M` and `S` and the value of `δ`."""
        return ("DT '" + self.name + "' = ⟨D, M, S, μ⟩ with |D|=" + str(len(self.data_list))
                + ", |M|=" + str(len(self.model_list)) + " (desc=" + str(len(self.get_descriptive_models()))
                + ", pred=" + str(len(self.get_predictive_models()))
                + ", presc=" + str(len(self.get_prescriptive_models())) + "), |S|=" + str(len(self.service_list))
                + ", δ=" + format_double(self.delta))
