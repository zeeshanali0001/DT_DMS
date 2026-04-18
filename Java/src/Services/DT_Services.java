package Services;

import DataObserver.ObserverData;

/**
 * Abstract class representing a generic service in the Digital Twin framework.
 * This serves as a base class for specific service types like Monitoring, Prediction,
 * Diagnosis, Optimization, and Control. It provides common properties and methods
 * for all services and is designed to be modular and extendable.
 */
public abstract class DT_Services<T> implements ObserverData<T>{



    
    @Override
    public void update(T data) {
        System.out.println("   → APPLICATION: Updating UI with " + data);
    }



    /**
     * The name of the service (e.g., "Temperature Monitoring").
     */
    protected String serviceName;

//    /**
//     * The type of the service (e.g., "Monitoring", "Prediction").
//     */
//    protected String serviceType;

    /**
     * A description of the service, providing additional details about its purpose.
     */
    protected String serviceDescription;

    /**
     * Constructor to initialize the service with a name, type, and optional description.
     *
     * @param serviceName        the name of the service
     * @param serviceType        the type of the service (e.g., "Monitoring", "Prediction")
     * @param serviceDescription a brief description of the service
     */
    public DT_Services(String serviceName, String serviceType, String serviceDescription) {
        this.serviceName = serviceName;
        //this.serviceType = serviceType;
        this.serviceDescription = serviceDescription != null ? serviceDescription : "No description provided.";
    }

    /**
     * Gets the name of the service.
     *
     * @return the name of the service
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the name of the service.
     *
     * @param serviceName the new name of the service
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

//    /**
//     * Gets the type of the service.
//     *
//     * @return the type of the service
//     */
//    public String getServiceType() {
//        return serviceType;
//    }
//
//    /**
//     * Sets the type of the service.
//     *
//     * @param serviceType the new type of the service
//     */
//    public void setServiceType(String serviceType) {
//        this.serviceType = serviceType;
//    }

    /**
     * Gets the description of the service.
     *
     * @return the description of the service
     */
    public String getServiceDescription() {
        return serviceDescription;
    }

    /**
     * Sets the description of the service.
     *
     * @param serviceDescription the new description of the service
     */
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }



    /**
     * Abstract method to execute the service logic.
     * This method should be implemented by child classes to define the specific behavior
     * of the service.
     */
    public abstract void executeService();















    /**
     * Provides a string representation of the service.
     *
     * @return a string containing the service's name, type, and description
     */
    @Override
    public String toString() {
        return "Service [serviceName=" + serviceName + ", serviceDescription=" + serviceDescription + "]";
        /**", serviceType=" + serviceType */
    }

    /**
     * Checks whether this service is equal to another object.
     * Two services are considered equal if they have the same name, type, and description.
     *
     * @param obj the object to compare with
     * @return true if the services are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        if (!(obj instanceof DT_Services<?>)) {
            return false;
        }
        
        DT_Services<?> otherService = (DT_Services<?>) obj;
        return serviceName.equals(otherService.serviceName) &&
                //serviceType.equals(otherService.serviceType) &&
                serviceDescription.equals(otherService.serviceDescription);
    }

    /**
     * Generates a hash code for the service.
     * This ensures that services with the same properties produce the same hash code.
     *
     * @return the hash code of the service
     */
    @Override
    public int hashCode() {
        int result = serviceName.hashCode();
        //result = 31 * result + serviceType.hashCode();
        result = 31 * result + serviceDescription.hashCode();
        return result;
    }


}
