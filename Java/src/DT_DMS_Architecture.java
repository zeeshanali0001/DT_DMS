
import Data.*;
import Models.*;
import Services.*;


public class DT_DMS_Architecture extends DT_DMS{

    DT_Data DT_data = null;
    /*
    DT_Data_System systemData;
    DT_Data_Platform platformData = null;
    DT_Data_Human humanData = null;
    */

    //DT_Models models = null;
    DT_Models_Descriptive descriptiveModels = null;
    DT_Models_Predictive predictiveModels = null;
    DT_Models_Prescriptive prescriptiveModels = null;


    //DT_Services services = null;
    DT_Services_Monitoring monitoring = null;
    DT_Services_Prediction prediction = null;
    DT_Services_Diagnosis diagnosis = null;
    DT_Services_Optimization optimization = null;
    DT_Services_Control  control = null;

}
