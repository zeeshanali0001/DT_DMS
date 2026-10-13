/**
 * This package contains the class of automata ({@code 𝒜}) of the DT-DMS framework.
 *
 * <p>
 *     DT models and DT services are specified with the same formalism because both belong to the same
 *     class of automata: {@code M ⊆ 𝒜} and {@code S ⊆ 𝒜}. An automaton, whether discrete, continuous or
 *     hybrid, is specified with HiLLS (based on DEVS) as the 7-tuple {@code ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩}.
 *     The description for each class in the {@code Automaton} package is as follows:
 * </p>
 *
 * <ul>
 *   <li>{@code DT_Automaton}: the abstract class holding the 7-tuple. {@code DT_Model} and {@code DT_Service}
 *       inherit from it.</li>
 * </ul>
 *
 * @author [ALI Zeeshan, Mamadou Kaba TRAORÉ]
 * @version 2.0
 */
package Automaton;
