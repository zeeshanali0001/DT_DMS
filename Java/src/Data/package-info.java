

/**
 * This package contains Data related to the DT-DMS framework.
 *
 *  <p>
 *     DT uses set of Data (Historical &amp; Real-Time) from Sensors/System, Platforms and Humans.
 *     This package contains DT data related classes with {@code Data} as Abstract class representing generic data
 *     in DMS framework, as well as the {@code SystemData}, {@code PlatformData} and {@code HumanData} the
 *     Child classes inherited from the {@code Data} Abstract class, ensuring a modular and extensible design.
 *     The description for each class in the {@code Data} package is as follows:
 *  </p>
 *
 * <ul>
 *   <li>{@code Data}: The abstract class. It handels generic DT data.</li>
 *   <li>{@code SystemData}: Class inherited from {@code Data} class. It handels the data from Sensors, IoTs, Embedded Systems, etc.</li>
 *   <li>{@code PlatformData}: Class inherited from {@code Data} class. It handels the data from data platforms, APIs, Cloud, etc.</li>
 *   <li>{@code HumanData}: Class inherited from {@code Data} class. It handels the DT human-related and interaction data.</li>
 * </ul>
 *
 *
 *
 * @author [ALI Zeeshan, Mamadou Kaba TRAORÉ]
 * @version 1.0
 */

package Data;
// Optionally include package-level annotations