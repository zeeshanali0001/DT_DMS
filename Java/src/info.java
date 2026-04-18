/*!
 * This package contains the sub-packages and functionalities of the Digital Twin Data Models Services (DT-DMS) framework.
 * The DT-DMS framework enables the modeling and simulation of a real-world system in virtual world.
 * It uses both historical and real-time data, for model enrichment and to provide both data-drive and
 * model-driven service such as:
 * </p>
 * <ul>
 *   <li>MonitoringService: Tracks real-time the system states.</li>
 *   <li>PredictionService: Predicts future states or behaviors of the system.</li>
 *   <li>DiagnosisService: Identifies faults or inefficiencies in the system.</li>
 *   <li>OptimizationService: Improves the system's performance or efficiency.</li>
 *   <li>ControlService: Regulates and manages the system through command and contol based on the feedback.</li>
 * </ul>
 *

 *
 * <p>
 *  DT uses set of Data (Historical & Real-Time) from different Sensors/System, Platforms and Humans.
 *  Sensors/System, Platforms and Humans data features are implemented as a subclass of the
 *  abstract {@link DT_DMS.Data} class, ensuring a modular and extensible design.
 * </p>
 *
 * <p>
 *    Similarly, DT uses set of Models; Descriptive; Predictive; and Perspective Models. These models are
 *    implemented as a subclass of the abstract {@link DT_DMS.Models} class,
 *    ensuring a modular and extensible design.
 *  </p>
 *
 * <p>
 *    DT provide set of service. Each service is implemented as a subclass of the
 *    abstract {@link DT_DMS.DT_Services} class, ensuring a modular and extensible design.
 * </p>
 *
 * @author [ALI Zeeshan, Mamadou Kaba TRAORÉ]
 * @version 1.0
 */


// Optionally include package-level annotations