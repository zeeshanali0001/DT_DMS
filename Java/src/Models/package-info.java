/**
 * This package contains the Models ({@code M}) of the DT-DMS framework.
 *
 * <p>
 *     {@code M} is the set of DT models that represent the ToI virtually:
 *     {@code M = M_desc ∪ M_pred ∪ M_presc}. Every model belongs to the class of automata {@code 𝒜}
 *     ({@code M ⊆ 𝒜}) and is specified by the 7-tuple {@code ℳ = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩}.
 *     The description for each class in the {@code Models} package is as follows:
 * </p>
 *
 * <ul>
 *   <li>{@code DT_Model}: the abstract class, built on {@code DT_Automaton}. It handles generic DT models.</li>
 *   <li>{@code DT_Model_Descriptive}: Class inherited from {@code DT_Model}. It describes what happened or is happening.</li>
 *   <li>{@code DT_Model_Predictive}: Class inherited from {@code DT_Model}. It predicts what is likely to happen.</li>
 *   <li>{@code DT_Model_Prescriptive}: Class inherited from {@code DT_Model}. It recommends what should be done.</li>
 * </ul>
 *
 * @author [ALI Zeeshan, Mamadou Kaba TRAORÉ]
 * @version 2.0
 */
package Models;
