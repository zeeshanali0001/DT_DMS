package DataObserver;

public interface ObserverDataManager<T> {
    void registerObserver(ObserverData<T> observer);
    void removeObserver(ObserverData<T> observer);
    void notifyObservers();
}