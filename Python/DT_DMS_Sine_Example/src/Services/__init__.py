# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@package Services
@brief The Services (`S`) of the DT-DMS framework.

`S` is the set of DT services: `S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont`. A service is parameterized by
a model and/or the data segments it consumes, `s : M × Ω_D → Y_s`. Every service belongs to the class of
automata `𝒜` (`S ⊆ 𝒜`) and is specified by the 7-tuple `𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`.
- DT_Service: the abstract class, built on DT_Automaton. It handles generic DT services.
- DT_Service_Monitoring: Class inherited from DT_Service. It monitors the states of the ToI in real time.
- DT_Service_Prediction: Class inherited from DT_Service. It predicts the future states of the ToI.
- DT_Service_Diagnosis: Class inherited from DT_Service. It identifies the anomalies of the ToI.
- DT_Service_Optimization: Class inherited from DT_Service. It finds the best solution for the ToI.
- DT_Service_Control: Class inherited from DT_Service. It provides the feedback control on the ToI.
"""
