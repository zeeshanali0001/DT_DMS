package Services;

import Data.DT_Data;
import DataObserver.ObserverData;

/**
 * Base class for all Services in the Digital Twin.
 * Services typically perform diagnosis, validation, or external actions.
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 1.0
 */
public class DT_Services implements ObserverData<DT_Data> {

    protected String _serviceName;

    @Override
    public void update(DT_Data dat) {
        // Default implementation
    }
}