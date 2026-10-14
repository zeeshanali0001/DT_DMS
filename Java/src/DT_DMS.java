
import Data.*;
import Models.*;
import Mu.DT_Mu;
import Services.*;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
 *   <li>Validity checks of the DT: services versus models, models versus data and data versus services
 *       ({@link #checkConsistency}).</li>
 *   <li>The export of its own Data-Models-Services diagram in the Graphviz format ({@link #dumpGraphviz}).</li>
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

    /** The name of the Digital Twin. */
    protected String name = "Digital Twin";

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
     * Constructs an empty named Digital Twin {@code ⟨D, M, S, μ⟩}.
     *
     * @param name  the name of the Digital Twin.
     * @param delta the freshness threshold {@code δ} (same time unit as the data measurement times).
     */
    public DT_DMS(String name, double delta) {
        this(delta);
        this.name = name;
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
        this.name = DT.name;
    }

    // ---------------------------------------------------------------- name

    /** @return the name of the Digital Twin. */
    public String getName() {
        return name;
    }

    /** @param name the new name of the Digital Twin. */
    public void setName(String name) {
        this.name = name;
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
     * Adds a service to {@code S} and couples it to the model {@code m}: the service will be notified each
     * time {@code μ} updates {@code m} ({@code DT_Model.addServiceToBeUpdated}).
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
        if (s.getModel() != null && s.getModel() != m) {
            s.getModel().removeServiceToBeUpdated(s);
        }
        if (m != null) {
            addModel(m);
            m.addServiceToBeUpdated(s);
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

    // ---------------------------------------------------------------- validity

    /**
     * Checks the consistency of the services with respect to the models they are coupled to.
     * <ul>
     *   <li>A coupled model must belong to {@code M}.</li>
     *   <li>A prediction service coupled to a model needs a predictive model.</li>
     *   <li>An optimization or a control service needs a prescriptive model (it cannot run without it).</li>
     *   <li>Monitoring and diagnosis services can use any model, or none (data-driven services).</li>
     * </ul>
     *
     * @param out where the report is written.
     * @return {@code true} if every service is consistent with its model.
     */
    public boolean checkServicesVersusModels(PrintStream out) {
        boolean ok = true;
        out.println("Services versus models:");
        for (DT_Service<T> s : serviceList) {
            DT_Model<T> m = s.getModel();
            String problem = null;
            if (m != null && !modelList.contains(m)) {
                problem = "its model " + m.getModelName() + " is not in M";
            } else if ((s instanceof DT_Service_Optimization || s instanceof DT_Service_Control)
                    && !(m instanceof DT_Model_Prescriptive)) {
                problem = "it needs a prescriptive model";
            } else if (s instanceof DT_Service_Prediction && m != null && !(m instanceof DT_Model_Predictive)) {
                problem = "it is coupled to a model that is not predictive";
            }
            String model = m == null ? "no model (data-driven)" : m.getModelName();
            if (problem == null) {
                out.println("  OK    " + s.getServiceName() + " → " + model);
            } else {
                out.println("  ERROR " + s.getServiceName() + " → " + model + ": " + problem);
                ok = false;
            }
        }
        return ok;
    }

    /**
     * Checks the fitting of the data versus the models: for every data source {@code D_i} and every model
     * {@code M_j} that gives values in time ({@code DT_Model.evaluate}), computes
     * {@code δ = |ω(t) − m(t)|} over the real-time data of the source.
     * <ul>
     *   <li>If {@code δ < ε} for every datum, the model fits the data very well.</li>
     *   <li>If the mean of {@code δ} is greater than {@code bigM}, the model does not fit the data well and
     *       should be updated, re-parameterized or discarded.</li>
     *   <li>Otherwise the fitting is acceptable.</li>
     * </ul>
     * Models that give no value in time are skipped.
     *
     * @param out     where the report is written.
     * @param epsilon the tolerance {@code ε} under which a datum is fitted very well.
     * @param bigM    the mean deviation {@code M} over which the model does not fit the data.
     * @return {@code false} if some model does not fit some data source.
     */
    public boolean checkModelsVersusData(PrintStream out, double epsilon, double bigM) {
        boolean ok = true;
        out.println("Models versus data (ε=" + epsilon + ", M=" + bigM + "):");
        for (DT_Model<T> m : modelList) {
            for (DT_Data<T> d : dataList) {
                DT_DataSegment<T> omega = d.getRealTimeData(delta);
                double max = 0.0;
                double sum = 0.0;
                int count = 0;
                for (double t : omega.getDomain()) {
                    T datum = omega.get(t);
                    double value = datum instanceof Number n ? n.doubleValue() : Double.NaN;
                    double expected = m.evaluate(t);
                    if (!Double.isNaN(value) && !Double.isNaN(expected)) {
                        double deviation = Math.abs(value - expected);
                        max = Math.max(max, deviation);
                        sum += deviation;
                        count++;
                    }
                }
                String pair = m.getModelName() + " / " + d.getSource();
                if (count == 0) {
                    out.println("  SKIP  " + pair + ": the model gives no value at the real-time data");
                } else if (max < epsilon) {
                    out.println("  OK    " + pair + ": fits very well (max δ=" + max + ")");
                } else if (sum / count > bigM) {
                    out.println("  ERROR " + pair + ": does not fit (mean δ=" + sum / count
                            + "): update, re-parameterize or discard the model");
                    ok = false;
                } else {
                    out.println("  OK    " + pair + ": acceptable (mean δ=" + sum / count + ", max δ=" + max + ")");
                }
            }
        }
        return ok;
    }

    /**
     * Checks the fitting of the data versus the models with {@code ε = 1e-3} and {@code M = 0.1}.
     *
     * @param out where the report is written.
     * @return {@code false} if some model does not fit some data source.
     * @see #checkModelsVersusData(PrintStream, double, double)
     */
    public boolean checkModelsVersusData(PrintStream out) {
        return checkModelsVersusData(out, 1e-3, 0.1);
    }

    /**
     * Checks the data characteristics (formats, units, ...) with respect to the services of the DT.
     * <ul>
     *   <li>Services need data: {@code S ≠ ∅ ⇒ D ≠ ∅}.</li>
     *   <li>Every data source must be valid ({@code DT_Data.validateData}).</li>
     *   <li>Warnings (not errors): a data source without unit, and a monitoring service whose monitored
     *       entity is neither the source nor the type of a data source.</li>
     * </ul>
     *
     * @param out where the report is written.
     * @return {@code true} if the data can be consumed by the services.
     */
    public boolean checkDataVersusServices(PrintStream out) {
        boolean ok = true;
        out.println("Data versus services:");
        if (!serviceList.isEmpty() && dataList.isEmpty()) {
            out.println("  ERROR the DT has services but no data");
            ok = false;
        }
        for (DT_Data<T> d : dataList) {
            if (!d.validateData()) {
                out.println("  ERROR " + d + ": source or data type missing");
                ok = false;
            } else if (d.getCharacterization().getUnit() == null) {
                out.println("  WARN  " + d.getSource() + ": no unit of measurement");
            } else {
                out.println("  OK    " + d.getSource() + " (" + d.getDataType() + ", "
                        + d.getCharacterization().getUnit() + ")");
            }
        }
        for (DT_Service_Monitoring<T> s : getMonitoringServices()) {
            boolean found = false;
            for (DT_Data<T> d : dataList) {
                found |= s.getMonitoredEntity() != null && (s.getMonitoredEntity().equals(d.getSource())
                        || s.getMonitoredEntity().equals(d.getDataType()));
            }
            if (!found) {
                out.println("  WARN  " + s.getServiceName() + ": no data source for the monitored entity "
                        + s.getMonitoredEntity());
            }
        }
        return ok;
    }

    /**
     * Runs the three validity checks of the DT: services versus models, models versus data
     * (with the default tolerances) and data versus services.
     *
     * @param out where the report is written.
     * @return {@code true} if all the checks succeed.
     */
    public boolean checkConsistency(PrintStream out) {
        boolean servicesModels = checkServicesVersusModels(out);
        boolean modelsData = checkModelsVersusData(out);
        boolean dataServices = checkDataVersusServices(out);
        return servicesModels && modelsData && dataServices;
    }

    // ---------------------------------------------------------------- Graphviz

    /**
     * Returns the Data-Models-Services diagram of the DT in the Graphviz format: one node lists the data
     * sources, one the models and one the services; {@code μ} links the data to the models.
     * Render it with {@code dot -Tpng file.dot -o file.png}.
     *
     * @return the diagram, in the dot language.
     */
    public String toGraphviz() {
        List<String> data = new ArrayList<>();
        for (DT_Data<T> d : dataList) data.add(d.getSource() + " (" + d.getDataType() + ")");
        List<String> models = new ArrayList<>();
        for (DT_Model<T> m : modelList) models.add(m.getModelName());
        List<String> services = new ArrayList<>();
        for (DT_Service<T> s : serviceList) services.add(s.getServiceName());

        StringBuilder dot = new StringBuilder();
        dot.append("digraph \"DMS Framework for the Digital Twin ").append(escape(name)).append("\" {\n");
        dot.append("nodesep=0.3;\n");
        dot.append("center=true;\n");
        dot.append("rankdir=LR;\n");
        dot.append("node [shape=box, style=rounded, fontname=\"Arial\"];\n");
        appendBlock(dot, "a", "DATA", data, "red");
        appendBlock(dot, "b", "MODELS", models, "darkgreen");
        appendBlock(dot, "c", "SERVICES", services, "blue");
        dot.append("a -> b [label=<&#956;>, dir=both];\n");
        dot.append("c -> b [dir=both];\n");
        dot.append("a -> c [dir=both];\n");
        dot.append("}\n");
        return dot.toString();
    }

    /**
     * Writes the Data-Models-Services diagram of the DT in the Graphviz format ({@link #toGraphviz}).
     *
     * @param out where the diagram is written.
     */
    public void dumpGraphviz(PrintStream out) {
        out.print(toGraphviz());
    }

    /**
     * Writes the Data-Models-Services diagram of the DT in a Graphviz file ({@link #toGraphviz}).
     *
     * @param fileName the name of the {@code .dot} file.
     * @throws IOException if the file cannot be written.
     */
    public void dumpGraphviz(String fileName) throws IOException {
        Files.writeString(Path.of(fileName), toGraphviz(), StandardCharsets.UTF_8);
    }

    /** Appends one node of the diagram: a title and one line per element ("(none)" if there is none). */
    private static void appendBlock(StringBuilder dot, String id, String title, List<String> lines, String color) {
        List<String> escaped = new ArrayList<>();
        for (String line : lines) escaped.add(escape(line));
        dot.append(id).append(" [label=\" ").append(title).append(" \\n ")
           .append(escaped.isEmpty() ? "(none)" : String.join(" \\n ", escaped))
           .append(" \", color=\"").append(color).append("\", fontcolor=\"").append(color).append("\"];\n");
    }

    /** Escapes the backslashes and double quotes of a name for a Graphviz label. */
    private static String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
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
        return "DT '" + name + "' = ⟨D, M, S, μ⟩ with |D|=" + dataList.size() + ", |M|=" + modelList.size()
                + " (desc=" + getDescriptiveModels().size() + ", pred=" + getPredictiveModels().size()
                + ", presc=" + getPrescriptiveModels().size() + "), |S|=" + serviceList.size() + ", δ=" + delta;
    }
}
