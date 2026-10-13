package Mu;

/**
 * Subject of the data observer pattern: an object that produces data and manages its observers.
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public interface ObserverDataManager<T> {

    /**
     * Registers an observer; it will be notified of every new datum.
     *
     * @param observer the observer to add.
     */
    void registerObserver(ObserverData<T> observer);

    /**
     * Removes an observer.
     *
     * @param observer the observer to remove.
     */
    void removeObserver(ObserverData<T> observer);

    /** Notifies all registered observers of the latest datum. */
    void notifyObservers();
}
