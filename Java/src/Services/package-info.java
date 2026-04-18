/**
 * This package contains Service related to the DT_DMS framework.
 *  <p>
 *      DT uses set of Data (Historical &amp; real-time) and Models (Descriptive, Predictive &amp; Perspective),
 *      for the provision of data nd model drive services.
 *
 *      This package contains DT Services related classes, {@code Services} as
 *      Abstract class representing generic model, as well as the {@code Monitoring},
 *      {@code Prediction}, {@code Diagnosis}, {@code Optimization} and {@code Control} the Child classes inherited from the
 *      {@code Services} Abstract class, ensuring a modular and extensible design.
 *      The description for each class in the {@code Services} package is as follows:
 *  </p>
 *
 * <ul>
 *   <li>{@code Services}: The abstract class. It handels generic DT Services.</li>
 *   <li>{@code Monitoring}: Class inherited from {@code Services} class. It monitors the states of the physical entity in real-time.</li>
 *   <li>{@code Prediction}: Class inherited from {@code Services} class. It predicts the future states of the physical entity using historical and realtime data and predictive models.</li>
 *   <li>{@code Diagnosis}: Class inherited from {@code Services} class. It diagnosis the anomilies inherit in system before they occur, using various data and models.</li>
 *   <li>{@code Optimization}: Class inherited from {@code Services} class. It find the best solution, using various data and models.</li>
 *   <li>{@code Control}: Class inherited from {@code Services} class. It provides the feedback control on the physical entity using actuators or other systems.</li>
 * </ul>
 *
 *
 *
 * @author [ALI Zeeshan, Mamadou Kaba TRAORÉ]
 * @version 1.0
 */


package Services;