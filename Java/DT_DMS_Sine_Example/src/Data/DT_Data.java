package Data;
import DataObserver.*;
//import Models.*;
//import Services.*;


import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all data sources in the Digital Twin system.
 * Implements the ObserverDataManager to support the Observer Pattern.
 * This class replaces the old manual addModel/addService and notifyModels/notifyServices methods.
 *
 * <p>It maintains a list of observers (Models and Services) and notifies them
 * automatically when data is updated via {@link #updateData()}.</p>
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 1.0
 */
public abstract class DT_Data implements ObserverDataManager<DT_Data> {

    /** Name of this data source (e.g., "Sine values") */
    protected String _dataName;

    /** List of all registered observers (Models + Services) */
    protected final List<ObserverData<DT_Data>> observers = new ArrayList<>();

    @Override
    public void registerObserver(ObserverData<DT_Data> observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("Observer registered for " + _dataName + ": " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void removeObserver(ObserverData<DT_Data> observer) {
        if (observers.remove(observer)) {
            System.out.println("Observer removed for " + _dataName + ": " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void notifyObservers() {
        List<ObserverData<DT_Data>> copy = new ArrayList<>(observers);
        for (ObserverData<DT_Data> obs : copy) {
            obs.update(this);
        }
    }

    /**
     * Updates the data and notifies all registered observers (Models and Services).
     * This is the central method that triggers the Observer Pattern.
     */
    public void updateData() {
        System.out.println("\n[DT_Data] Data updated → notifying observers: " + _dataName);
        notifyObservers();
    }

    /**
     * Returns the name of this data source.
     * @return The data name
     */
    public String getDataName() {
        return _dataName;
    }
}