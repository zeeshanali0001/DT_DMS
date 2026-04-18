package Data;


import java.util.ArrayList;
import java.util.List;
/**
 * Concrete data class that generates and stores sine wave values.
 * Every time 10 new values are added, it triggers notification to all observers
 * using the Observer Pattern.
 *
 * <p>This represents the "Physical Twin" part of the Digital Twin.</p>
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 1.0
 */



public class DT_Data_SineValues extends DT_Data {

    /** List that stores all generated sine values */
    private final List<Double> _sineValues = new ArrayList<>();

    /** Current index / counter of how many values have been pushed */
    private int _currentIndex = 0;

    /**
     * Constructor to initialize the sine data source.
     *
     * @param name Descriptive name of this data source (e.g., "Sine values")
     */
    public DT_Data_SineValues(String name) {
        this._dataName = name;
    }

    /**
     * Returns the list of all sine values collected so far.
     *
     * @return Immutable view of sine values list (consider Collections.unmodifiableList in production)
     */
    public List<Double> getSineDataValues() {
        return _sineValues;
    }

    /**
     * Returns the current index (number of values pushed so far).
     * This is used by the chart and diagnosis service to plot correct x-axis.
     *
     * @return Current sample index
     */
    public int getCurrentIndex() {
        return _currentIndex;
    }

    /**
     * Adds a new sine value to the collection.
     * Increments the index and triggers notification to observers every 10 values.
     *
     * @param val The sine value to add
     */
    public void push_back(double val) {
        _sineValues.add(val);
        _currentIndex++;

        if (_sineValues.size() % 10 == 0) {
            updateData();   // Triggers Observer Pattern - notifies Model and Service
        }
    }
}