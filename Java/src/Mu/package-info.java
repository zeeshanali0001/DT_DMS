/**
 * This package contains the model inference or data assimilation function ({@code μ}) of the DT-DMS framework.
 *
 * <p>
 *     {@code μ : Ω_D × M → M} updates a model with a data segment: {@code μ(ω, m) = m'}, where {@code m'} keeps
 *     the type of {@code m}. Real-time data is assimilated synchronously and historical data asynchronously;
 *     a mixed segment is split into {@code ω_hist ⊕ ω_rt} or assimilated as a whole.
 *     The description for each class in the {@code Mu} package is as follows:
 * </p>
 *
 * <ul>
 *   <li>{@code DT_Mu}: the data assimilation function. It observes the data sources and updates the models.</li>
 *   <li>{@code ObserverData}: the observer of the data observer pattern (models, services and {@code μ}).</li>
 *   <li>{@code ObserverDataManager}: the subject of the data observer pattern (the data sources).</li>
 *   <li>{@code ObserverModel}: the observer of the model observer pattern (the services notified when the
 *       model they are coupled to is updated).</li>
 * </ul>
 *
 * @author [ALI Zeeshan, Mamadou Kaba TRAORÉ]
 * @version 2.0
 */
package Mu;
