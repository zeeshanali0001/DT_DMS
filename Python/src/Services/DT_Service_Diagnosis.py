# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Service_Diagnosis.py
@brief A diagnosis service `s ∈ S_diag`.
"""

from typing import Optional, TypeVar

from Automaton.DT_Automaton import format_double, numeric_value
from Services.DT_Service import DT_Service

T = TypeVar("T")


class DT_Service_Diagnosis(DT_Service):
    """!
    @brief Class representing a Diagnosis Service (`s ∈ S_diag`) of the Digital Twin.

    A Diagnosis Service identifies faults, anomalies or inefficiencies of the ToI from the data it consumes:
    here, the values that fall outside a normal range.
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, service_name: str, service_description: Optional[str], normal_minimum: float,
                 normal_maximum: float):
        """!
        @brief Constructor to initialize the Diagnosis Service with necessary attributes.
        @param service_name        the name of the service.
        @param service_description a brief description of the service.
        @param normal_minimum      the lower bound of the normal range.
        @param normal_maximum      the upper bound of the normal range.
        """
        super().__init__(service_name, service_description)
        ## The lower bound of the normal range.
        self.normal_minimum: float = float(normal_minimum)
        ## The upper bound of the normal range.
        self.normal_maximum: float = float(normal_maximum)
        ## The report with the results of the last diagnosis.
        self.diagnosis_report: str = "No diagnosis performed yet."

    def execute_service(self) -> str:
        """!
        @brief Executes the diagnosis logic: reports every consumed value outside the normal range.
        @return the diagnosis report.
        """
        if not self.consumed_data:
            self.diagnosis_report = "Diagnosis failed due to lack of data."
            return self.diagnosis_report
        report = []
        for i, d in enumerate(self.consumed_data):
            v = numeric_value(d)
            if v < self.normal_minimum or v > self.normal_maximum:
                report.append("Anomaly at index " + str(i) + " with value " + format_double(v) + ".")
        self.diagnosis_report = (" ".join(report) if report
                                 else "No anomalies detected. System functioning within normal parameters.")
        return self.diagnosis_report

    def get_diagnosis_report(self) -> str:
        """! @return the report of the last diagnosis."""
        return self.diagnosis_report
