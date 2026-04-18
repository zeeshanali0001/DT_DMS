package DataObserver;
/**
 * Subject interface (Publisher) in the Observer Pattern.
 * Classes that hold data and want to notify observers when data changes
 * must implement this interface.
 *
 * @param <T> The type of data this subject manages
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 1.0
 */
public interface ObserverDataManager<T> {

    /**
     * Registers an observer to receive notifications when data changes.
     *
     * @param observer The observer to be registered
     */
    void registerObserver(ObserverData<T> observer);

    /**
     * Removes a previously registered observer so it no longer receives updates.
     *
     * @param observer The observer to be removed
     */
    void removeObserver(ObserverData<T> observer);

    /**
     * Notifies all registered observers that the data has changed.
     * This method is called internally whenever data is updated.
     */
    void notifyObservers();
}