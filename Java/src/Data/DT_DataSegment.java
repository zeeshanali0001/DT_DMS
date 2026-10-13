package Data;

import java.util.Collection;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeMap;

/**
 * Class representing a measured data segment {@code ω ∈ Ω_D} of the Digital Twin.
 * <p>
 * A data segment is a partial function {@code ω : T_U ⇀ D} from universal time {@code T_U} to data, with
 * {@code Dom(ω) ⊆ T_U} and {@code Im(ω) ⊆ D}. Time is a {@code double} (e.g., seconds). Since a segment is
 * measured, no datum lies in the future: {@code sup Dom(ω) ≤ t_now}.
 * </p>
 *
 * <p>With {@code t_now} the current time and {@code δ} the freshness threshold (data scope), a segment is:
 * <ul>
 *   <li><b>real-time</b>, {@code ω ∈ Ω_D^rt(t_now)}, iff {@code inf Dom(ω) ≥ t_now − δ ∧ sup Dom(ω) ≤ t_now};</li>
 *   <li><b>historical</b>, {@code ω ∈ Ω_D^hist(t_now)}, iff {@code sup Dom(ω) < t_now − δ};</li>
 *   <li><b>mixed</b>, {@code ω ∈ Ω_D^mix(t_now)}, iff {@code inf Dom(ω) < t_now − δ ∧ t_now − δ ≤ sup Dom(ω) ≤ t_now}.</li>
 * </ul>
 * so that {@code Ω_D = Ω_D^rt(t_now) ∪ Ω_D^hist(t_now) ∪ Ω_D^mix(t_now)}. A mixed segment can be split at
 * {@code t_now − δ} into {@code ω = ω_hist ⊕ ω_rt} with {@link #historicalPart} and {@link #realTimePart}.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_DataSegment<T> {

    /** The kind of a segment at {@code t_now}: the set of {@code Ω_D} it belongs to. */
    public enum Kind {
        /** {@code ω ∈ Ω_D^rt(t_now)}: all its data are real-time. */
        REAL_TIME,
        /** {@code ω ∈ Ω_D^hist(t_now)}: all its data are historical. */
        HISTORICAL,
        /** {@code ω ∈ Ω_D^mix(t_now)}: it contains both kinds of data. */
        MIXED
    }

    /** The pairs {@code (t, ω(t))}, ordered by time. */
    private final TreeMap<Double, T> values = new TreeMap<>();

    /** Creates the empty segment ({@code Dom(ω) = ∅}). */
    public DT_DataSegment() {
    }

    /**
     * Creates a segment with one datum, {@code ω(t) = d}.
     *
     * @param t the measurement time.
     * @param d the datum.
     */
    public DT_DataSegment(double t, T d) {
        values.put(t, d);
    }

    /**
     * Adds the datum {@code d} measured at time {@code t}, i.e., sets {@code ω(t) = d}.
     *
     * @param t the measurement time.
     * @param d the datum.
     */
    public void put(double t, T d) {
        values.put(t, d);
    }

    /**
     * @param t a time.
     * @return {@code ω(t)}, or {@code null} if {@code t ∉ Dom(ω)}.
     */
    public T get(double t) {
        return values.get(t);
    }

    /** @return {@code Dom(ω)}, in increasing order (read-only). */
    public NavigableSet<Double> getDomain() {
        return Collections.unmodifiableNavigableSet(values.navigableKeySet());
    }

    /** @return {@code Im(ω)}, in time order (read-only). */
    public Collection<T> getImage() {
        return Collections.unmodifiableCollection(values.values());
    }

    /** @return {@code true} if {@code Dom(ω) = ∅}. */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /** @return the number of data of the segment. */
    public int size() {
        return values.size();
    }

    /** @return {@code inf Dom(ω)}: the time of the oldest datum ({@code +∞} for the empty segment). */
    public double infDom() {
        return values.isEmpty() ? Double.POSITIVE_INFINITY : values.firstKey();
    }

    /** @return {@code sup Dom(ω)}: the time of the newest datum ({@code −∞} for the empty segment). */
    public double supDom() {
        return values.isEmpty() ? Double.NEGATIVE_INFINITY : values.lastKey();
    }

    /**
     * @param tNow  the current time {@code t_now}.
     * @param delta the freshness threshold {@code δ}.
     * @return {@code true} iff {@code inf Dom(ω) ≥ t_now − δ ∧ sup Dom(ω) ≤ t_now}.
     */
    public boolean isRealTime(double tNow, double delta) {
        return !isEmpty() && infDom() >= tNow - delta && supDom() <= tNow;
    }

    /**
     * @param tNow  the current time {@code t_now}.
     * @param delta the freshness threshold {@code δ}.
     * @return {@code true} iff {@code sup Dom(ω) < t_now − δ}.
     */
    public boolean isHistorical(double tNow, double delta) {
        return !isEmpty() && supDom() < tNow - delta;
    }

    /**
     * @param tNow  the current time {@code t_now}.
     * @param delta the freshness threshold {@code δ}.
     * @return {@code true} iff {@code inf Dom(ω) < t_now − δ ∧ t_now − δ ≤ sup Dom(ω) ≤ t_now}.
     */
    public boolean isMixed(double tNow, double delta) {
        return !isEmpty() && infDom() < tNow - delta && tNow - delta <= supDom() && supDom() <= tNow;
    }

    /**
     * Classifies the segment at {@code t_now}.
     *
     * @param tNow  the current time {@code t_now}.
     * @param delta the freshness threshold {@code δ}.
     * @return the kind of the segment: real-time, historical or mixed.
     * @throws IllegalArgumentException if the segment is empty or has a datum in the future
     *                                  ({@code sup Dom(ω) > t_now}), i.e., it is not an admissible measured segment.
     */
    public Kind kind(double tNow, double delta) {
        if (isEmpty()) {
            throw new IllegalArgumentException("The empty segment cannot be classified.");
        }
        if (supDom() > tNow) {
            throw new IllegalArgumentException("No datum may lie in the future: sup Dom(ω) = " + supDom()
                    + " > t_now = " + tNow);
        }
        if (isRealTime(tNow, delta)) {
            return Kind.REAL_TIME;
        }
        return isHistorical(tNow, delta) ? Kind.HISTORICAL : Kind.MIXED;
    }

    /**
     * @param tNow  the current time {@code t_now}.
     * @param delta the freshness threshold {@code δ}.
     * @return {@code ω_rt}: {@code ω} restricted to the times {@code t ≥ t_now − δ}.
     */
    public DT_DataSegment<T> realTimePart(double tNow, double delta) {
        DT_DataSegment<T> part = new DT_DataSegment<>();
        part.values.putAll(values.tailMap(tNow - delta, true));
        return part;
    }

    /**
     * @param tNow  the current time {@code t_now}.
     * @param delta the freshness threshold {@code δ}.
     * @return {@code ω_hist}: {@code ω} restricted to the times {@code t < t_now − δ}.
     */
    public DT_DataSegment<T> historicalPart(double tNow, double delta) {
        DT_DataSegment<T> part = new DT_DataSegment<>();
        part.values.putAll(values.headMap(tNow - delta, false));
        return part;
    }

    /**
     * Concatenation {@code ω1 ⊕ ω2} ({@code this} is {@code ω1}) of two segments whose domains follow each
     * other in time: the segment that equals {@code ω1} on {@code Dom(ω1)} and {@code ω2} on {@code Dom(ω2)}.
     *
     * @param next the segment {@code ω2}.
     * @return the new segment {@code ω1 ⊕ ω2}.
     * @throws IllegalArgumentException if some time of {@code Dom(ω1)} does not precede every time of {@code Dom(ω2)}.
     */
    public DT_DataSegment<T> concat(DT_DataSegment<T> next) {
        if (!isEmpty() && !next.isEmpty() && supDom() >= next.infDom()) {
            throw new IllegalArgumentException("ω1 ⊕ ω2 requires Dom(ω1) to precede Dom(ω2).");
        }
        DT_DataSegment<T> result = new DT_DataSegment<>();
        result.values.putAll(values);
        result.values.putAll(next.values);
        return result;
    }

    /**
     * Converts the segment into a human-readable string format.
     *
     * @return the pairs {@code {t=ω(t), ...}}.
     */
    @Override
    public String toString() {
        return "ω" + values;
    }
}
