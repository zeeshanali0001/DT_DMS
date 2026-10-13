/**
 * This package contains the Services ({@code S}) of the DT-DMS framework.
 *
 * <p>
 *     {@code S} is the set of DT services: {@code S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont}.
 *     A service is parameterized by a model and/or the data segments it consumes,
 *     {@code s : M × Ω_D → Y_s}. Every service belongs to the class of automata {@code 𝒜}
 *     ({@code S ⊆ 𝒜}) and is specified by the 7-tuple {@code 𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩}.
 *     The description for each class in the {@code Services} package is as follows:
 * </p>
 *
 * <ul>
 *   <li>{@code DT_Service}: the abstract class, built on {@code DT_Automaton}. It handles generic DT services.</li>
 *   <li>{@code DT_Service_Monitoring}: Class inherited from {@code DT_Service}. It monitors the states of the ToI in real time.</li>
 *   <li>{@code DT_Service_Prediction}: Class inherited from {@code DT_Service}. It predicts the future states of the ToI.</li>
 *   <li>{@code DT_Service_Diagnosis}: Class inherited from {@code DT_Service}. It identifies the anomalies of the ToI.</li>
 *   <li>{@code DT_Service_Optimization}: Class inherited from {@code DT_Service}. It finds the best solution for the ToI.</li>
 *   <li>{@code DT_Service_Control}: Class inherited from {@code DT_Service}. It provides the feedback control on the ToI.</li>
 * </ul>
 *
 * @author [ALI Zeeshan, Mamadou Kaba TRAORÉ]
 * @version 2.0
 */
package Services;
