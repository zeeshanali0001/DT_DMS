# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@package Mu
@brief The model inference or data assimilation function (`μ`) of the DT-DMS framework.

`μ : Ω_D × M → M` updates a model with a data segment: `μ(ω, m) = m'`, where `m'` keeps the type of `m`.
Real-time data is assimilated synchronously and historical data asynchronously; a mixed segment is split into
`ω_hist ⊕ ω_rt` or assimilated as a whole.
- DT_Mu: the data assimilation function. It observes the data sources and updates the models.
- ObserverData: the observer of the data observer pattern (models, services and `μ`).
- ObserverDataManager: the subject of the data observer pattern (the data sources).
- ObserverModel: the observer of the model observer pattern (the services notified when the model they are
  coupled to is updated).
"""
