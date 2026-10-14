# DT-DMS framework (Python) - Copyright (C) 2026 : Milad Zahediyami, ALI Zeeshan, Mamadou Kaba TRAORÉ
# Licensed under the GNU General Public License v3 or later (see License.txt).
"""!
@package Automaton
@brief The class of automata (`𝒜`) of the DT-DMS framework.

DT models and DT services are specified with the same formalism because both belong to the same class of
automata: `M ⊆ 𝒜` and `S ⊆ 𝒜`. An automaton, whether discrete, continuous or hybrid, is specified with
HiLLS (based on DEVS) as the 7-tuple `⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩`.
- DT_Automaton: the abstract class holding the 7-tuple. DT_Model and DT_Service inherit from it.
"""
