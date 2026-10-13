
import Data.*;
import Models.*;
import Mu.DT_Mu;
import Services.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;


/**
 * The {@code DT_DMS} class is the general class of the Digital Twin Data-Models-Services (DT-DMS) framework.
 * It holds the formal specification of a Digital Twin:
 * <pre>
 *   DT = ⟨ D, M, S, μ ⟩
 * </pre>
 *
 * <p>Key features of the class include:
 * <ul>
 *   <li>{@code D}: the ToI data ({@link DT_Data}), {@code D = D_hist ∪ D_rt}.</li>
 *   <li>{@code M}: the models of the ToI ({@link DT_Model}), {@code M = M_desc ∪ M_pred ∪ M_presc}.</li>
 *   <li>{@code S}: the DT services ({@link DT_Service}),
 *       {@code S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont}, with {@code s : M × Ω_D → Y_s}.</li>
 *   <li>{@code μ}: the model inference or data assimilation function ({@link DT_Mu}), {@code μ : Ω_D × M → M}.</li>
 *   <li>It connects the Data, Models and Services of the Digital Twin and manages the information flow
 *       between them: {@code μ} observes every data source and updates every model.</li>
 *   <li>The iterator design pattern over the data, the models and the services
 *       ({@code begin…()}, {@code current…()}, {@code next…()}, {@code end…()}).</li>
 * </ul>
 * </p>
 *
 * <p>
 * The components are held by composition: data, models, services and {@code μ} are parts of the DT.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public class DT_DMS<T> {

    /** {@code D}: the data sources of the ToI. */
    protected List<DT_Data<T>> dataList = new ArrayList<>();

    /** {@code M}: the models of the ToI. */
    protected List<DT_Model<T>> modelList = new ArrayList<>();

    /** {@code S}: the DT services. */
    protected List<DT_Service<T>> serviceList = new ArrayList<>();

    /** {@code μ}: the model inference or data assimilation function. */
    protected DT_Mu<T> mu;

    /** The freshness threshold {@code δ ∈ ℝ+}: the data scope during which data is considered real-time. */
    protected double delta;

    /** Gives the current time {@code t_now ∈ T_U} (default: system time in seconds). */
    protected DoubleSupplier clock = () -> System.currentTimeMillis() / 1000.0;

    /** Position of the iterator over the data. */
    private int dataIterator;

    /** Position of the iterator over the models. */
    private int modelIterator;

    /** Position of the iterator over the services. */
    private int serviceIterator;

    // ---------------------------------------------------------------- constructors

    /**
     * Constructs an empty Digital Twin {@code ⟨D, M, S, μ⟩}.
     *
     * @param delta the freshness threshold {@code δ} (same time unit as the data measurement times).
     */
    public DT_DMS(double delta) {
        this.delta = delta;
        this.mu = new DT_Mu<>(delta, this::tNow);
    }

    /**
     * Copy constructor: the new Digital Twin has the same data, models and services as {@code DT}.
     * <p>
     * The lists are copied but the components themselves are shared, and so are {@code μ}, {@code δ} and the
     * clock, so that every datum is still assimilated once into each model.
     * </p>
     *
     * @param DT the Digital Twin to copy.
     */
    public DT_DMS(DT_DMS<T> DT) {
        this.dataList = new ArrayList<>(DT.dataList);
        this.modelList = new ArrayList<>(DT.modelList);
        this.serviceList = new ArrayList<>(DT.serviceList);
        this.mu = DT.mu;
        this.delta = DT.delta;
        this.clock = DT.clock;
    }

    // ---------------------------------------------------------------- time

    /** @return the current time {@code t_now}. */
    public double tNow() {
        return clock.getAsDouble();
    }

    /**
     * Replaces the clock giving {@code t_now} (e.g., a simulated clock).
     *
     * @param clock the new clock.
     */
    public void setClock(DoubleSupplier clock) {
        this.clock = clock;
    }

    /** @return the freshness threshold {@code δ}. */
    public double getDelta() {
        return delta;
    }

    // ---------------------------------------------------------------- D

    /**
     * Adds a data source to {@code D}; it shares the clock of the DT and {@code μ} starts observing it.
     *
     * @param d the data source.
     */
    public void addData(DT_Data<T> d) {
        if (!dataList.contains(d)) {
            dataList.add(d);
        }
        d.setClock(this::tNow);
        mu.observe(d);
    }

    /** @return {@code D_rt}: for each data source, its data measured at {@code t ≥ t_now − δ}. */
    public List<DT_DataSegment<T>> getRealTimeData() {
        List<DT_DataSegment<T>> result = new ArrayList<>();
        for (DT_Data<T> d : dataList) {
            result.add(d.getRealTimeData(delta));
        }
        return result;
    }

    /** @return {@code D_hist}: for each data source, its data measured at {@code t < t_now − δ}. */
    public List<DT_DataSegment<T>> getHistoricalData() {
        List<DT_DataSegment<T>> result = new ArrayList<>();
        for (DT_Data<T> d : dataList) {
            result.add(d.getHistoricalData(delta));
        }
        return result;
    }

    // ---------------------------------------------------------------- M

    /**
     * Adds a model to {@code M}; it will be updated by {@code μ}.
     *
     * @param m the model.
     * @throws IllegalArgumentException if {@code m} is not descriptive, predictive or prescriptive.
     */
    public void addModel(DT_Model<T> m) {
        if (!(m instanceof DT_Model_Descriptive) && !(m instanceof DT_Model_Predictive)
                && !(m instanceof DT_Model_Prescriptive)) {
            throw new IllegalArgumentException("A model must be descriptive, predictive or prescriptive: " + m);
        }
        if (!modelList.contains(m)) {
            modelList.add(m);
        }
        mu.addModel(m);
    }

    /** @return {@code M_desc}: the descriptive models. */
    public List<DT_Model_Descriptive<T>> getDescriptiveModels() {
        List<DT_Model_Descriptive<T>> result = new ArrayList<>();
        for (DT_Model<T> m : modelList) {
            if (m instanceof DT_Model_Descriptive<T> desc) result.add(desc);
        }
        return result;
    }

    /** @return {@code M_pred}: the predictive models. */
    public List<DT_Model_Predictive<T>> getPredictiveModels() {
        List<DT_Model_Predictive<T>> result = new ArrayList<>();
        for (DT_Model<T> m : modelList) {
            if (m instanceof DT_Model_Predictive<T> pred) result.add(pred);
        }
        return result;
    }

    /** @return {@code M_presc}: the prescriptive models. */
    public List<DT_Model_Prescriptive<T>> getPrescriptiveModels() {
        List<DT_Model_Prescriptive<T>> result = new ArrayList<>();
        for (DT_Model<T> m : modelList) {
            if (m instanceof DT_Model_Prescriptive<T> presc) result.add(presc);
        }
        return result;
    }

    // ---------------------------------------------------------------- S

    /**
     * Adds a service to {@code S} and couples it to the model {@code m}.
     *
     * @param s the service.
     * @param m the model the service is coupled to (added to {@code M} if needed), or {@code null} for a
     *          purely data-driven service.
     * @throws IllegalArgumentException if {@code s} is not one of the five service types.
     */
    public void addService(DT_Service<T> s, DT_Model<T> m) {
        if (!(s instanceof DT_Service_Monitoring) && !(s instanceof DT_Service_Prediction)
                && !(s instanceof DT_Service_Diagnosis) && !(s instanceof DT_Service_Optimization)
                && !(s instanceof DT_Service_Control)) {
            throw new IllegalArgumentException(
                    "A service must be monitoring, prediction, diagnosis, optimization or control: " + s);
        }
        if (m != null) {
            addModel(m);
        }
        s.setModel(m);
        if (!serviceList.contains(s)) {
            serviceList.add(s);
        }
    }

    /**
     * Runs a service with the model it is coupled to: {@code s(m, ω) ∈ Y_s}.
     *
     * @param s     the service.
     * @param omega the data segment it consumes (may be {@code null}).
     * @return the output of the service.
     */
    public Object runService(DT_Service<T> s, DT_DataSegment<T> omega) {
        return s.execute(s.getModel(), omega);
    }

    /** @return {@code S_moni}: the monitoring services. */
    public List<DT_Service_Monitoring<T>> getMonitoringServices() {
        List<DT_Service_Monitoring<T>> result = new ArrayList<>();
        for (DT_Service<T> s : serviceList) {
            if (s instanceof DT_Service_Monitoring<T> moni) result.add(moni);
        }
        return result;
    }

    /** @return {@code S_pred}: the prediction services. */
    public List<DT_Service_Prediction<T>> getPredictionServices() {
        List<DT_Service_Prediction<T>> result = new ArrayList<>();
        for (DT_Service<T> s : serviceList) {
            if (s instanceof DT_Service_Prediction<T> pred) result.add(pred);
        }
        return result;
    }

    /** @return {@code S_diag}: the diagnosis services. */
    public List<DT_Service_Diagnosis<T>> getDiagnosisServices() {
        List<DT_Service_Diagnosis<T>> result = new ArrayList<>();
        for (DT_Service<T> s : serviceList) {
            if (s instanceof DT_Service_Diagnosis<T> diag) result.add(diag);
        }
        return result;
    }

    /** @return {@code S_opti}: the optimization services. */
    public List<DT_Service_Optimization<T>> getOptimizationServices() {
        List<DT_Service_Optimization<T>> result = new ArrayList<>();
        for (DT_Service<T> s : serviceList) {
            if (s instanceof DT_Service_Optimization<T> opti) result.add(opti);
        }
        return result;
    }

    /** @return {@code S_cont}: the control services. */
    public List<DT_Service_Control<T>> getControlServices() {
        List<DT_Service_Control<T>> result = new ArrayList<>();
        for (DT_Service<T> s : serviceList) {
            if (s instanceof DT_Service_Control<T> cont) result.add(cont);
        }
        return result;
    }

    // ---------------------------------------------------------------- μ

    /** @return the model inference or data assimilation function {@code μ}. */
    public DT_Mu<T> getMu() {
        return mu;
    }

    /**
     * Assimilates a data segment into the models of the DT, according to its kind at {@code t_now}
     * (real-time, historical or mixed).
     *
     * @param omega the data segment {@code ω}.
     */
    public void assimilate(DT_DataSegment<T> omega) {
        mu.assimilate(omega);
    }

    //
    // Iterators
    //

    /** For the design pattern iterator: initialize the list of models to run. */
    public void beginModels() {
        modelIterator = 0;
    }

    /**
     * For the design pattern iterator: return the current model.
     * <pre>
     * for (dt.beginModels(); !dt.endModels(); dt.nextModel())
     *     System.out.println(dt.currentModel().getModelName());
     * </pre>
     *
     * @return the current model.
     */
    public DT_Model<T> currentModel() {
        return modelList.get(modelIterator);
    }

    /** For the design pattern iterator: move to the next model. */
    public void nextModel() {
        modelIterator++;
    }

    /**
     * For the design pattern iterator: test whether the end of the models is reached.
     *
     * @return {@code true} if no other model exists.
     */
    public boolean endModels() {
        return modelIterator >= modelList.size();
    }

    /** @return the number of models. */
    public int getNumberOfModels() {
        return modelList.size();
    }

    /** @return the position of the iterator over the models. */
    public int getModelsIteratorPosition() {
        return modelIterator;
    }

    /** For the design pattern iterator: initialize the list of services to run. */
    public void beginServices() {
        serviceIterator = 0;
    }

    /**
     * For the design pattern iterator: return the current service.
     * <pre>
     * for (dt.beginServices(); !dt.endServices(); dt.nextService())
     *     System.out.println(dt.currentService().getServiceName());
     * </pre>
     *
     * @return the current service.
     */
    public DT_Service<T> currentService() {
        return serviceList.get(serviceIterator);
    }

    /** For the design pattern iterator: move to the next service. */
    public void nextService() {
        serviceIterator++;
    }

    /**
     * For the design pattern iterator: test whether the end of the services is reached.
     *
     * @return {@code true} if no other service exists.
     */
    public boolean endServices() {
        return serviceIterator >= serviceList.size();
    }

    /** @return the number of services. */
    public int getNumberOfServices() {
        return serviceList.size();
    }

    /** @return the position of the iterator over the services. */
    public int getServicesIteratorPosition() {
        return serviceIterator;
    }

    /** For the design pattern iterator: initialize the list of data to run. */
    public void beginData() {
        dataIterator = 0;
    }

    /**
     * For the design pattern iterator: return the current data source.
     * <pre>
     * for (dt.beginData(); !dt.endData(); dt.nextData())
     *     System.out.println(dt.currentData().getSource());
     * </pre>
     *
     * @return the current data source.
     */
    public DT_Data<T> currentData() {
        return dataList.get(dataIterator);
    }

    /** For the design pattern iterator: move to the next data source. */
    public void nextData() {
        dataIterator++;
    }

    /**
     * For the design pattern iterator: test whether the end of the data is reached.
     *
     * @return {@code true} if no other data source exists.
     */
    public boolean endData() {
        return dataIterator >= dataList.size();
    }

    /** @return the number of data sources. */
    public int getNumberOfData() {
        return dataList.size();
    }

    /** @return the position of the iterator over the data. */
    public int getDataIteratorPosition() {
        return dataIterator;
    }

    /**
     * Provides a summary of the Digital Twin, useful for debugging or logging.
     *
     * @return the sizes of {@code D}, {@code M} and {@code S} and the value of {@code δ}.
     */
    @Override
    public String toString() {
        return "DT = ⟨D, M, S, μ⟩ with |D|=" + dataList.size() + ", |M|=" + modelList.size()
                + " (desc=" + getDescriptiveModels().size() + ", pred=" + getPredictiveModels().size()
                + ", presc=" + getPrescriptiveModels().size() + "), |S|=" + serviceList.size() + ", δ=" + delta;
    }
}
