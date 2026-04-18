import Data.DT_Data;
import Models.DT_Models;
import Services.DT_Services;

import java.util.ArrayList;
import java.util.List;

class DT_DMS {
    protected List<DT_Data> _data = new ArrayList<>();
    protected List<DT_Models> _models = new ArrayList<>();
    protected List<DT_Services> _services = new ArrayList<>();

    public void addData(DT_Data d) 
    { 
        _data.add(d); 
    }

    public void addModel(DT_Models m) 
    { 
        _models.add(m); 
    }

    public void addService(DT_Services s) 
    { 
        _services.add(s); 
    }

    public static void main(String[] args) {
        DT_DMS_SineCubicBP dtSystem = new DT_DMS_SineCubicBP();
        dtSystem.execute();
    }

    
}