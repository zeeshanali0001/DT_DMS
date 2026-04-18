package Models;

import Data.DT_Data;
import Data.DT_Data_SineValues;
import Services.DT_Services_Diagnosis_Sine;
/**
 * Cubic B-Spline model for interpolating sine wave data.
 * This is the core "Digital Model" component of the Digital Twin.
 * It reacts to new sine data and can trigger service updates.
 *
 * @author Zeeshan ALI, Mamadou Kaba TRAORE
 * @version 1.0
 */


public class DT_Models_Descr_CubicBSpline extends DT_Models_Descriptive {

    /** Step size used for generating x values in the sine wave */
    protected double _step;

    /** Left endpoint of the domain (usually 0.0) */
    protected double _leftEndpoint = 0.0;

    /** Reference to the linked diagnosis service */
    protected DT_Services_Diagnosis_Sine _linkedService;

    /**
     * Constructor for the Cubic B-Spline model.
     *
     * @param step Step size between samples
     */
    public DT_Models_Descr_CubicBSpline(double step) {
        this._step = step;
    }

    /**
     * Links a diagnosis service that this model can notify directly.
     *
     * @param srv The diagnosis service to be linked
     */
    public void addService2BeUpdated(DT_Services_Diagnosis_Sine srv) {
        this._linkedService = srv;
    }

    @Override
    public void update(DT_Data dat) {
        if (dat instanceof DT_Data_SineValues sineData) {
            System.out.println("# CubicBSpline_Model::update() done for index " 
                             + sineData.getCurrentIndex());
            notifyServices();
        }
    }

    /**
     * Notifies the linked diagnosis service.
     * This maintains the original model-to-service direct communication.
     */
    public void notifyServices() {
        if (_linkedService != null) {
            _linkedService.updateServiceWithModel();
        }
    }

    /**
     * Returns the sine model value at position x (simplified implementation).
     * In a real DT, this could be a proper B-Spline interpolation.
     *
     * @param x Input value
     * @return Computed model value (here simply sin(x))
     */
    public double getSineModelValue(double x) {
        return Math.sin(x);
    }

    public double getLeftEndpoint() {
        return _leftEndpoint;
    }

    public double getStep() {
        return _step;
    }
}
