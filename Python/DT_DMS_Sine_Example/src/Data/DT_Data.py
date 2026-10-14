# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Data.py
@brief A source of data of the ToI, a part of `D`.
"""

import time
from typing import Callable, List, Optional, TypeVar

from Automaton.DT_Automaton import format_double
from Data.DT_DataCharacterization import DT_DataCharacterization
from Data.DT_DataSegment import DT_DataSegment
from Mu.ObserverData import ObserverData
from Mu.ObserverDataManager import ObserverDataManager

T = TypeVar("T")


class DT_Data(ObserverDataManager):
    """!
    @brief Class representing a source of data collected from the ToI, i.e., a part of `D` in
    `DT = ⟨D, M, S, μ⟩`.

    The collected data is either historical or real-time, `D = D_hist ∪ D_rt`. The classification depends only
    on the measurement time: a datum `d = ω(t)` is real-time iff `t ≥ t_now − δ`, and historical otherwise,
    where `t` is the measurement time, `t_now` the current time and `δ` the data scope during which data is
    considered real-time.

    Key features of the class include:
    - Every datum is recorded with its measurement time in a segment `ω` (DT_DataSegment).
    - get_real_time_data() and get_historical_data() give `D_rt` and `D_hist` of the source.
    - Its characterization (DT_DataCharacterization) tells how the data must be interpreted (unit, signal type,
      sensor precision, calibration, ...).
    - It is the subject of the observer pattern (ObserverDataManager): each new datum is pushed to the
      registered observers, in particular to the data assimilation function `μ`.
    @tparam T the type of a datum `d ∈ D`.
    """

    def __init__(self, source: str, data_type: str):
        """!
        @brief Constructs a data source.
        @param source    the origin of the data (e.g., sensor name or system name).
        @param data_type the type or category of the data (e.g., temperature).
        """
        ## The observers interested in this data source.
        self._observers: List[ObserverData] = []
        ## The source of the data (e.g., "Sensor A", "API").
        self.source: str = source
        ## The type of the data (e.g., "Temperature", "Pressure").
        self.data_type: str = data_type
        ## The latest datum.
        self._current_data: Optional[T] = None
        ## The measurement time `t ∈ T_U` of the latest datum, i.e., `current_data = ω(current_time)`.
        self._current_time: float = 0.0
        ## All data collected from this source, as a segment `ω : T_U ⇀ D`.
        self._collected_data: DT_DataSegment = DT_DataSegment()
        ## Gives the current time `t_now` (default: system time in seconds).
        self._clock: Callable[[], float] = time.time
        ## The characterization of the data: unit, signal type, sensor precision, calibration, ...
        self.characterization: DT_DataCharacterization = DT_DataCharacterization()

    # ---------------------------------------------------------------- observer pattern (subject)

    def register_observer(self, observer: ObserverData) -> None:
        """! @brief Registers an observer; it will be notified of every new datum."""
        if observer not in self._observers:
            self._observers.append(observer)

    def remove_observer(self, observer: ObserverData) -> None:
        """! @brief Removes an observer."""
        if observer in self._observers:
            self._observers.remove(observer)

    def notify_observers(self) -> None:
        """! @brief Notifies all registered observers of the latest datum."""
        # Safe copy to avoid issues if an observer removes itself during update
        for observer in list(self._observers):
            observer.update(self._current_data)

    # ---------------------------------------------------------------- data collection

    def update_data(self, *args) -> None:
        """!
        @brief Call this method whenever new data arrives: `update_data(new_data)` time-stamps the datum with the
        current time `t_now`; `update_data(t, new_data)` records a datum measured at time `t`. The datum is
        recorded (`ω(t) = new_data`) and the observers are notified.
        @exception ValueError if `t > t_now`: no datum may lie in the future.
        """
        if len(args) == 1:
            t, new_data = self._clock(), args[0]
        elif len(args) == 2:
            t, new_data = args
        else:
            raise TypeError("update_data(new_data) or update_data(t, new_data)")
        t_now = self._clock()
        if t > t_now:
            raise ValueError("No datum may lie in the future: t = " + format_double(t)
                             + " > t_now = " + format_double(t_now))
        self._current_data = new_data
        self._current_time = float(t)
        self._collected_data.put(t, new_data)
        self.notify_observers()

    def get_current_data(self) -> Optional[T]:
        """! @return the latest datum."""
        return self._current_data

    def get_current_time(self) -> float:
        """! @return the measurement time of the latest datum."""
        return self._current_time

    def get_collected_data(self) -> DT_DataSegment:
        """! @return all data collected from this source, as a segment `ω`."""
        return self._collected_data

    def get_real_time_data(self, delta: float) -> DT_DataSegment:
        """!
        @param delta the data scope `δ`.
        @return `D_rt`: the collected data measured at `t ≥ t_now − δ`.
        """
        return self._collected_data.real_time_part(self._clock(), delta)

    def get_historical_data(self, delta: float) -> DT_DataSegment:
        """!
        @param delta the data scope `δ`.
        @return `D_hist`: the collected data measured at `t < t_now − δ`.
        """
        return self._collected_data.historical_part(self._clock(), delta)

    def set_clock(self, clock: Callable[[], float]) -> None:
        """!
        @brief Replaces the clock giving `t_now` (set by the DT so that all its parts share the same time).
        @param clock the new clock.
        """
        self._clock = clock

    # ---------------------------------------------------------------- attributes

    def validate_data(self) -> bool:
        """!
        @brief Validates the common attributes of the data source.
        @return True if the source and the data type are defined; False otherwise.
        """
        return bool(self.source) and bool(self.data_type)

    def get_source(self) -> str:
        """! @return the source of the data."""
        return self.source

    def set_source(self, source: str) -> None:
        """! @param source the new source of the data."""
        self.source = source

    def get_data_type(self) -> str:
        """! @return the type of the data."""
        return self.data_type

    def set_data_type(self, data_type: str) -> None:
        """! @param data_type the new type of the data."""
        self.data_type = data_type

    def get_characterization(self) -> DT_DataCharacterization:
        """! @return the characterization of the data (unit, signal type, sensor precision, ...); never `None`."""
        return self.characterization

    def set_characterization(self, characterization: Optional[DT_DataCharacterization]) -> None:
        """! @param characterization the new characterization of the data (`None` = nothing known)."""
        self.characterization = characterization if characterization is not None else DT_DataCharacterization()

    def __repr__(self) -> str:
        """! @return a string with the source, the data type and the number of collected data."""
        return ("DT_Data{source='" + str(self.source) + "', dataType='" + str(self.data_type)
                + "', collected=" + str(self._collected_data.size()) + "}")

    def __eq__(self, other) -> bool:
        """! @brief Two data sources are considered equal if they have the same source and data type."""
        return isinstance(other, DT_Data) and self.source == other.source and self.data_type == other.data_type

    def __hash__(self) -> int:
        """! @return the hash code, based on the source and the data type."""
        return hash((self.source, self.data_type))
