# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@package Models
@brief The Models (`M`) of the DT-DMS framework.

`M` is the set of DT models that represent the ToI virtually: `M = M_desc ∪ M_pred ∪ M_presc`. Every model
belongs to the class of automata `𝒜` (`M ⊆ 𝒜`) and is specified by the 7-tuple `ℳ = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`.
- DT_Model: the abstract class, built on DT_Automaton. It handles generic DT models.
- DT_Model_Descriptive: Class inherited from DT_Model. It describes what happened or is happening.
- DT_Model_Predictive: Class inherited from DT_Model. It predicts what is likely to happen.
- DT_Model_Prescriptive: Class inherited from DT_Model. It recommends what should be done.
"""
