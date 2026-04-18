package DataObserver;

/**
 * Observer interface for the Observer Pattern used in the Digital Twin system.
 * Any class that wants to be notified when data changes must implement this interface.
 *
 * @param <T> The type of data this observer listens to (usually DT_Data or its subclasses)
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 1.0
 */
public interface ObserverData<T> {

    /**
     * Called automatically when the observed data changes.
     *
     * @param updatedData The latest data object that has been updated
     */
    void update(T updatedData);
}