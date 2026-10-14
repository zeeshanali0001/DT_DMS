/**
 * This package contains the Data ({@code D}) of the DT-DMS framework.
 *
 * <p>
 *     {@code D} is the data collected from the ToI, either historical or real-time:
 *     {@code D = D_hist ∪ D_rt}. The classification depends only on the measurement time: a datum
 *     {@code d = ω(t)} is real-time iff {@code t ≥ t_now − δ}, and historical otherwise.
 *     The description for each class in the {@code Data} package is as follows:
 * </p>
 *
 * <ul>
 *   <li>{@code DT_Data}: a source of data of the ToI. It records every datum with its measurement time and
 *       notifies its observers (the data assimilation function {@code μ}).</li>
 *   <li>{@code DT_DataSegment}: a measured data segment {@code ω : T_U ⇀ D}, classified at {@code t_now} as
 *       real-time, historical or mixed.</li>
 *   <li>{@code DT_DataCharacterization}: the characterization of a data source (unit, signal type, data flow
 *       type, sensor precision, location, calibration status and last maintenance date).</li>
 * </ul>
 *
 * @author [ALI Zeeshan, Mamadou Kaba TRAORÉ]
 * @version 2.0
 */
package Data;
