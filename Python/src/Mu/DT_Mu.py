# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@file DT_Mu.py
@brief The model inference or data assimilation function `μ : Ω_D × M → M`.
"""

from typing import Callable, List, TypeVar

from Data.DT_Data import DT_Data
from Data.DT_DataSegment import DT_DataSegment, Kind
from Models.DT_Model import DT_Model
from Mu.ObserverData import ObserverData

T = TypeVar("T")


class MixedPolicy:
    """!
    @brief How a mixed segment `ω ∈ Ω_D^mix(t_now)` is assimilated, depending on the DT requirements.
    """
    ## Part by part: `ω_rt` synchronously and `ω_hist` asynchronously.
    SPLIT = "SPLIT"
    ## As a whole, synchronously, like a real-time segment.
    AS_REAL_TIME = "AS_REAL_TIME"
    ## As a whole, asynchronously, like a historical segment.
    AS_HISTORICAL = "AS_HISTORICAL"


class _Assimilator(ObserverData):
    """!
    @brief Observer registered on one data source: forwards every new datum of that source to `μ`.
    """

    def __init__(self, mu: "DT_Mu", source: DT_Data):
        """!
        @param mu     the data assimilation function.
        @param source the observed data source.
        """
        ## The data assimilation function.
        self._mu = mu
        ## The observed data source.
        self._source = source

    def update(self, updated_data) -> None:
        """! @brief Called when new data arrives from the source: the datum `d = ω(t)` is assimilated."""
        self._mu.assimilate(DT_DataSegment(self._source.get_current_time(), updated_data))


class DT_Mu:
    """!
    @brief Class representing the model inference or data assimilation function `μ : Ω_D × M → M` of
    `DT = ⟨D, M, S, μ⟩`.

    Given a segment `ω ∈ Ω_D` and a model `m ∈ M`, `μ(ω, m) = m'` is the model `m` updated with `ω`. The model
    is updated in place, so `m'` is the same object as `m` and keeps the type of `m`
    (e.g., `m ∈ M_pred ⇒ m' ∈ M_pred`).

    Key features of the class include:
    - **Data observer pattern**: it observes the data sources (ObserverDataManager) through one ObserverData per
      source; each new datum of the ToI is turned into a segment `ω`.
    - **Synchronous assimilation** of real-time segments (`Ω_D^rt`): applied at once.
    - **Asynchronous assimilation** of historical segments (`Ω_D^hist`): queued until flush().
    - **Mixed segments** (`Ω_D^mix`) are handled according to the MixedPolicy: split into `ω_hist ⊕ ω_rt` and
      assimilated part by part (default), or assimilated as a whole like a real-time or like a historical
      segment.
    @tparam T the type of a datum `d ∈ D`.
    """

    ## The policies for mixed segments (`DT_Mu.MixedPolicy.SPLIT`, ...).
    MixedPolicy = MixedPolicy

    def __init__(self, delta: float, clock: Callable[[], float]):
        """!
        @brief Constructs the data assimilation function.
        @param delta the freshness threshold `δ`.
        @param clock gives the current time `t_now`.
        @exception ValueError if `δ < 0`.
        """
        if delta < 0:
            raise ValueError("δ must be in ℝ+.")
        ## The freshness threshold `δ ∈ ℝ+`: the data scope during which data is considered real-time.
        self._delta: float = float(delta)
        ## Gives the current time `t_now ∈ T_U`.
        self._clock: Callable[[], float] = clock
        ## The models `M` updated by `μ`.
        self._models: List[DT_Model] = []
        ## The observed data sources.
        self._sources: List[DT_Data] = []
        ## Segments waiting for asynchronous assimilation.
        self._pending: List[DT_DataSegment] = []
        ## How mixed segments are assimilated.
        self._mixed_policy: str = MixedPolicy.SPLIT

    # ---------------------------------------------------------------- what μ observes and updates

    def add_model(self, m: DT_Model) -> None:
        """!
        @brief Adds a model to the set of models updated by `μ`.
        @param m the model.
        """
        if m not in self._models:
            self._models.append(m)

    def observe(self, source: DT_Data) -> None:
        """!
        @brief Starts observing a data source: every new datum of `source` will be assimilated.
        @param source the data source.
        """
        if source not in self._sources:
            self._sources.append(source)
            source.register_observer(_Assimilator(self, source))

    # ---------------------------------------------------------------- μ

    def mu(self, omega: DT_DataSegment, m: DT_Model) -> DT_Model:
        """!
        @brief The data assimilation function `μ(ω, m) = m'`.
        @param omega the data segment `ω`.
        @param m     the model `m`.
        @return the updated model `m'` (same object and type as `m`).
        """
        m.assimilate(omega)
        return m

    def assimilate(self, omega: DT_DataSegment) -> None:
        """!
        @brief Assimilates a segment according to its kind at `t_now`: real-time → synchronously,
        historical → asynchronously, mixed → according to the MixedPolicy.
        @param omega the data segment `ω`.
        @exception ValueError if `ω` is empty or has a datum in the future.
        """
        t_now = self._clock()
        kind = omega.kind(t_now, self._delta)
        if kind == Kind.REAL_TIME:
            self.assimilate_synchronously(omega)
        elif kind == Kind.HISTORICAL:
            self.assimilate_asynchronously(omega)
        elif self._mixed_policy == MixedPolicy.SPLIT:
            # ω = ω_hist ⊕ ω_rt, split at t_now − δ
            self.assimilate_asynchronously(omega.historical_part(t_now, self._delta))
            self.assimilate_synchronously(omega.real_time_part(t_now, self._delta))
        elif self._mixed_policy == MixedPolicy.AS_REAL_TIME:
            self.assimilate_synchronously(omega)
        else:
            self.assimilate_asynchronously(omega)

    def assimilate_synchronously(self, omega: DT_DataSegment) -> None:
        """!
        @brief Synchronous assimilation: applies `μ(ω, m)` at once to every model `m ∈ M`.
        @param omega the data segment `ω`.
        """
        for m in self._models:
            self.mu(omega, m)

    def assimilate_asynchronously(self, omega: DT_DataSegment) -> None:
        """!
        @brief Asynchronous assimilation: queues the segment; `μ` is applied when flush() is called.
        @param omega the data segment `ω`.
        """
        self._pending.append(omega)

    def flush(self) -> None:
        """! @brief Applies `μ` to all queued segments, in the order they were queued."""
        to_assimilate, self._pending = self._pending, []
        for omega in to_assimilate:
            self.assimilate_synchronously(omega)

    # ---------------------------------------------------------------- accessors

    def get_delta(self) -> float:
        """! @return the freshness threshold `δ`."""
        return self._delta

    def get_pending_count(self) -> int:
        """! @return the number of segments waiting for flush()."""
        return len(self._pending)

    def get_mixed_policy(self) -> str:
        """! @return how mixed segments are assimilated."""
        return self._mixed_policy

    def set_mixed_policy(self, mixed_policy: str) -> None:
        """! @param mixed_policy how mixed segments are to be assimilated."""
        self._mixed_policy = mixed_policy

    def get_models(self) -> List[DT_Model]:
        """! @return the models updated by `μ`."""
        return self._models

    def get_sources(self) -> List[DT_Data]:
        """! @return the observed data sources."""
        return self._sources
