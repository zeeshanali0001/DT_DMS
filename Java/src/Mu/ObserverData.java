package Mu;

/**
 * Observer of the data observer pattern: an object that wants to be told when new data arrives.
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public interface ObserverData<T> {

    /**
     * Called when new data of type T arrives.
     *
     * @param updatedData the new datum.
     */
    void update(T updatedData);
}
