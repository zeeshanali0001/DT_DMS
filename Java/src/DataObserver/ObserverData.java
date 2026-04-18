package DataObserver;
public interface ObserverData<T> {
    /**
     * Called when new data of type T arrives.
     */
    void update(T updatedData);
}