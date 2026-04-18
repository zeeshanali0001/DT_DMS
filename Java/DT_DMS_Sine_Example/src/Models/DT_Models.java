package Models;
import Data.DT_Data;
import DataObserver.ObserverData;

/**
 * Base class for all Models in the Digital Twin system.
 * Implements ObserverData so it can react to changes in DT_Data.
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 1.0
 */
public class DT_Models implements ObserverData<DT_Data> {

    /** Name of this model */
    protected String _modelName;

    /**
     * Called by DT_Data when new data is available.
     * Subclasses should override this method to implement specific model logic.
     *
     * @param dat The updated data object
     */
    @Override
    public void update(DT_Data dat) {
        // Default empty implementation
    }
    
}