package Data;

import DataObserver.*;


import java.util.ArrayList;
import java.util.List;



/**
 * Abstract class representing generic data in the Digital Twin framework.
 * This class serves as a base for different specialized data types (e.g., {@code SystemData}, {@code PlatformData}),
 * providing shared attributes and methods for data management.
 *
 * <p>Key features of the class include:
 * <ul>
 *   <li>Encapsulation of common data attributes such as source, data type, and timestamp.</li>
 *   <li>Abstract methods for fetching and validating data to enforce implementation in derived classes.</li>
 *   <li>A standard {@code toString} method for debugging and logging purposes.</li>
 * </ul>
 * </p>
 */
public abstract class DT_Data<T> implements ObserverDataManager<T> {

    // List to hold all observers interested in this data type
    private final List<ObserverData<T>> observers = new ArrayList<>();

    // Holds the latest data
    private T currentData;

    @Override
    public void registerObserver(ObserverData<T> observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            System.out.println("Observer registered: " + observer.getClass().getSimpleName());
        }
    }

    @Override
    public void removeObserver(ObserverData<T> observer) {
        observers.remove(observer);
        System.out.println("Observer removed: " + observer.getClass().getSimpleName());
    }

    @Override
    public void notifyObservers() {
        // Safe copy to avoid issues if observer removes itself during update
        List<ObserverData<T>> observersCopy = new ArrayList<>(observers);

        for (ObserverData<T> observer : observersCopy) {
            observer.update(currentData);
        }
    }

    /**
     * Call this method whenever new data arrives
     */
    public void updateData(T newData) {
        this.currentData = newData;
        System.out.println("\n[DATA SOURCE] New data received: " + newData);
        notifyObservers();
    }

    /**
     * Get the latest data
     */
    public T getCurrentData() {
        return currentData;
    }



/*
private final List<ObserverData<T>> observers = new ArrayList<>();
    private int temperature;           // Example data field (you can add more)
    private String location = "City Center"; // Another field for realism

    // === Subject methods (from DataSubject) ===
    @Override
    public void registerObserver(ObserverData<DT_Data> observer) {
        observers.add(observer);
        System.out.println("Observer registered: " + observer.getClass().getSimpleName());
    }

    @Override
    public void removeObserver(ObserverData<DT_Data> observer) {
        observers.remove(observer);
        System.out.println("Observer removed: " + observer.getClass().getSimpleName());
    }

    @Override
    public void notifyObservers() {
        // Safe copy to avoid ConcurrentModificationException if an observer removes itself
        List<ObserverData<DT_Data>> observersCopy = new ArrayList<>(observers);
        for (ObserverData<DT_Data> observer : observersCopy) {
            observer.update(this);
        }
    }

    // === Your data-update method (this is where "new data comes") ===
    public void setNewTemperature(int newTemperature) {
        this.temperature = newTemperature;
        System.out.println("\n[DATA] New temperature received: " + newTemperature + "°C");
        notifyObservers();   // <-- This automatically updates all Models & Applications
    }

    // Getters so observers can read the latest data
    public int getTemperature() {
        return temperature;
    }

    public String getLocation() {
        return location;
    }
    */





    /**
     * The source of the data (e.g., "Sensor", "API",Human,...).
     */
    protected String source;

    /**
     * The type of the data (e.g., "Temperature", "Pressure").
     * */
    protected String dataType;

    /**
     * The timestamp indicating when the data was collected.
     * */
    protected String timestamp;

    /**
     * Constructs a new {@code DigitalTwin_DMS_Framework.Data} object with the specified attributes.
     *
     * @param source     The origin of the data (e.g., sensor name or system name).
     * @param dataType   The type or category of the data (e.g., temperature, text).
     * @param timestamp  The time at which the data was recorded, in an ISO-compliant format.
     */
    public DT_Data(String source, String dataType, String timestamp) {
        this.source = source;
        this.dataType = dataType;
        this.timestamp = timestamp;
    }

    /**
     * Fetches data from the source.
     * Subclasses must implement specific logic for fetching data based on their context.
     */
    public abstract void fetchData();

    /**
     * Validates the integrity and completeness of the data.
     * Subclasses must define their own validation criteria and logic.
     *
     * @return {@code true} if the data is valid; {@code false} otherwise.
     */
    /**
     * Validates the common attributes of the data object.
     * Subclasses can extend this validation as needed.
     *
     * @return true if the basic data attributes are valid; false otherwise.
     */
    public boolean validateData() {
        return source != null && !source.isEmpty() &&
                dataType != null && !dataType.isEmpty() &&
                timestamp != null && !timestamp.isEmpty();
    }

    /**
     * Retrieves the source of the data.
     *
     * @return The source of the data.
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets a new source for the data.
     *
     * @param source The new source of the data.
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Retrieves the type of the data.
     *
     * @return The data type.
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets a new type for the data.
     *
     * @param dataType The new data type.
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Retrieves the timestamp of the data.
     *
     * @return The timestamp when the data was collected.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets a new timestamp for the data.
     *
     * @param timestamp The new timestamp.
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Converts the {@code DigitalTwin_DMS_Framework.Data} object into a human-readable string format.
     *
     * @return A string representation of the data object, including its attributes.
     */
    @Override
    public String toString() {
        return "DigitalTwin_DMS_Framework.Data{" +
                "source='" + source + '\'' +
                ", dataType='" + dataType + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    /**
     * Compares this {@code DigitalTwin_DMS_Framework.Data} object with another object for equality.
     * Two {@code DigitalTwin_DMS_Framework.Data} objects are considered equal if their attributes match.
     *
     * @param obj The object to compare with this instance.
     * @return {@code true} if the objects are equal; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DT_Data)) return false;

        if (!(obj instanceof DT_Data<?>)) 
        {
            return false;
        }

        DT_Data<?> data = (DT_Data<?>) obj;

        return source.equals(data.source)
            && dataType.equals(data.dataType)
            && timestamp.equals(data.timestamp);
            
    }

    /**
     * Computes the hash code for this {@code DigitalTwin_DMS_Framework.Data} object.
     * The hash code is based on the source, data type, and timestamp.
     *
     * @return The computed hash code.
     */
    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + dataType.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }
}
