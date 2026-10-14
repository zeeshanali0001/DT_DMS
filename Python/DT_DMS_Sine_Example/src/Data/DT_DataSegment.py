# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_DataSegment.py
@brief A measured data segment `ω ∈ Ω_D`.
"""

import bisect
import math
from typing import Dict, List, Optional, TypeVar

from Automaton.DT_Automaton import format_double, value_to_string

T = TypeVar("T")


class Kind:
    """!
    @brief The kind of a segment at `t_now`: the set of `Ω_D` it belongs to.
    """
    ## `ω ∈ Ω_D^rt(t_now)`: all its data are real-time.
    REAL_TIME = "REAL_TIME"
    ## `ω ∈ Ω_D^hist(t_now)`: all its data are historical.
    HISTORICAL = "HISTORICAL"
    ## `ω ∈ Ω_D^mix(t_now)`: it contains both kinds of data.
    MIXED = "MIXED"


class DT_DataSegment:
    """!
    @brief Class representing a measured data segment `ω ∈ Ω_D` of the Digital Twin.

    A data segment is a partial function `ω : T_U ⇀ D` from universal time `T_U` to data, with `Dom(ω) ⊆ T_U`
    and `Im(ω) ⊆ D`. Time is a `float` (e.g., seconds). Since a segment is measured, no datum lies in the future:
    `sup Dom(ω) ≤ t_now`.

    With `t_now` the current time and `δ` the freshness threshold (data scope), a segment is:
    - **real-time**, `ω ∈ Ω_D^rt(t_now)`, iff `inf Dom(ω) ≥ t_now − δ ∧ sup Dom(ω) ≤ t_now`;
    - **historical**, `ω ∈ Ω_D^hist(t_now)`, iff `sup Dom(ω) < t_now − δ`;
    - **mixed**, `ω ∈ Ω_D^mix(t_now)`, iff `inf Dom(ω) < t_now − δ ∧ t_now − δ ≤ sup Dom(ω) ≤ t_now`.

    so that `Ω_D = Ω_D^rt(t_now) ∪ Ω_D^hist(t_now) ∪ Ω_D^mix(t_now)`. A mixed segment can be split at
    `t_now − δ` into `ω = ω_hist ⊕ ω_rt` with historical_part() and real_time_part().
    @tparam T the type of a datum `d ∈ D`.
    """

    ## The kinds of a segment (`DT_DataSegment.Kind.REAL_TIME`, ...).
    Kind = Kind

    def __init__(self, t: Optional[float] = None, d: Optional[T] = None):
        """!
        @brief Creates the empty segment (`Dom(ω) = ∅`), or a segment with one datum, `ω(t) = d`.
        @param t the measurement time (`None` for the empty segment).
        @param d the datum.
        """
        ## The measurement times `Dom(ω)`, in increasing order.
        self._times: List[float] = []
        ## The pairs `(t, ω(t))`.
        self._values: Dict[float, T] = {}
        if t is not None:
            self.put(t, d)

    def put(self, t: float, d: T) -> None:
        """!
        @brief Adds the datum `d` measured at time `t`, i.e., sets `ω(t) = d`.
        @param t the measurement time.
        @param d the datum.
        """
        t = float(t)
        if t not in self._values:
            bisect.insort(self._times, t)
        self._values[t] = d

    def get(self, t: float) -> Optional[T]:
        """!
        @param t a time.
        @return `ω(t)`, or `None` if `t ∉ Dom(ω)`.
        """
        return self._values.get(float(t))

    def get_domain(self) -> List[float]:
        """! @return `Dom(ω)`, in increasing order."""
        return list(self._times)

    def get_image(self) -> List[T]:
        """! @return `Im(ω)`, in time order."""
        return [self._values[t] for t in self._times]

    def items(self):
        """! @return the pairs `(t, ω(t))`, in time order."""
        return [(t, self._values[t]) for t in self._times]

    def is_empty(self) -> bool:
        """! @return `True` if `Dom(ω) = ∅`."""
        return not self._times

    def size(self) -> int:
        """! @return the number of data of the segment."""
        return len(self._times)

    def __len__(self) -> int:
        return len(self._times)

    def inf_dom(self) -> float:
        """! @return `inf Dom(ω)`: the time of the oldest datum (`+∞` for the empty segment)."""
        return self._times[0] if self._times else math.inf

    def sup_dom(self) -> float:
        """! @return `sup Dom(ω)`: the time of the newest datum (`−∞` for the empty segment)."""
        return self._times[-1] if self._times else -math.inf

    def is_real_time(self, t_now: float, delta: float) -> bool:
        """! @return `True` iff `inf Dom(ω) ≥ t_now − δ ∧ sup Dom(ω) ≤ t_now`."""
        return not self.is_empty() and self.inf_dom() >= t_now - delta and self.sup_dom() <= t_now

    def is_historical(self, t_now: float, delta: float) -> bool:
        """! @return `True` iff `sup Dom(ω) < t_now − δ`."""
        return not self.is_empty() and self.sup_dom() < t_now - delta

    def is_mixed(self, t_now: float, delta: float) -> bool:
        """! @return `True` iff `inf Dom(ω) < t_now − δ ∧ t_now − δ ≤ sup Dom(ω) ≤ t_now`."""
        return (not self.is_empty() and self.inf_dom() < t_now - delta
                and t_now - delta <= self.sup_dom() <= t_now)

    def kind(self, t_now: float, delta: float) -> str:
        """!
        @brief Classifies the segment at `t_now`.
        @param t_now the current time `t_now`.
        @param delta the freshness threshold `δ`.
        @return the kind of the segment: `Kind.REAL_TIME`, `Kind.HISTORICAL` or `Kind.MIXED`.
        @exception ValueError if the segment is empty or has a datum in the future (`sup Dom(ω) > t_now`), i.e.,
        it is not an admissible measured segment.
        """
        if self.is_empty():
            raise ValueError("The empty segment cannot be classified.")
        if self.sup_dom() > t_now:
            raise ValueError("No datum may lie in the future: sup Dom(ω) = " + format_double(self.sup_dom())
                             + " > t_now = " + format_double(t_now))
        if self.is_real_time(t_now, delta):
            return Kind.REAL_TIME
        return Kind.HISTORICAL if self.is_historical(t_now, delta) else Kind.MIXED

    def real_time_part(self, t_now: float, delta: float) -> "DT_DataSegment[T]":
        """! @return `ω_rt`: `ω` restricted to the times `t ≥ t_now − δ`."""
        part = DT_DataSegment()
        for t in self._times:
            if t >= t_now - delta:
                part.put(t, self._values[t])
        return part

    def historical_part(self, t_now: float, delta: float) -> "DT_DataSegment[T]":
        """! @return `ω_hist`: `ω` restricted to the times `t < t_now − δ`."""
        part = DT_DataSegment()
        for t in self._times:
            if t < t_now - delta:
                part.put(t, self._values[t])
        return part

    def concat(self, next_segment: "DT_DataSegment[T]") -> "DT_DataSegment[T]":
        """!
        @brief Concatenation `ω1 ⊕ ω2` (`self` is `ω1`) of two segments whose domains follow each other in time:
        the segment that equals `ω1` on `Dom(ω1)` and `ω2` on `Dom(ω2)`.
        @param next_segment the segment `ω2`.
        @return the new segment `ω1 ⊕ ω2`.
        @exception ValueError if some time of `Dom(ω1)` does not precede every time of `Dom(ω2)`.
        """
        if not self.is_empty() and not next_segment.is_empty() and self.sup_dom() >= next_segment.inf_dom():
            raise ValueError("ω1 ⊕ ω2 requires Dom(ω1) to precede Dom(ω2).")
        result = DT_DataSegment()
        for t, d in self.items() + next_segment.items():
            result.put(t, d)
        return result

    def __repr__(self) -> str:
        """! @return the pairs `ω{t=ω(t), ...}`."""
        return "ω{" + ", ".join(format_double(t) + "=" + value_to_string(d) for t, d in self.items()) + "}"
