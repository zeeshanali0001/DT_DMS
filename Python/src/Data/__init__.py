# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@package Data
@brief The Data (`D`) of the DT-DMS framework.

`D` is the data collected from the ToI, either historical or real-time: `D = D_hist ∪ D_rt`. The
classification depends only on the measurement time: a datum `d = ω(t)` is real-time iff `t ≥ t_now − δ`,
and historical otherwise.
- DT_Data: a source of data of the ToI. It records every datum with its measurement time and notifies its
  observers (the data assimilation function `μ`).
- DT_DataSegment: a measured data segment `ω : T_U ⇀ D`, classified at `t_now` as real-time, historical or
  mixed.
- DT_DataCharacterization: the characterization of a data source (unit, signal type, data flow type, sensor
  precision, location, calibration status and last maintenance date).
"""
