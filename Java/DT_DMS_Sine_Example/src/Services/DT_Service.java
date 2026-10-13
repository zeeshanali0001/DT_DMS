package Services;

import Automaton.DT_Automaton;
import Data.DT_DataSegment;
import Models.DT_Model;
import Mu.ObserverData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class representing a generic DT service {@code s ∈ S} of {@code DT = ⟨D, M, S, μ⟩}.
 * <p>
 * {@code S} is the set of DT services: {@code S = S_moni ∪ S_pred ∪ S_diag ∪ S_opti ∪ S_cont}, the
 * monitoring ({@link DT_Service_Monitoring}), prediction ({@link DT_Service_Prediction}), diagnosis
 * ({@link DT_Service_Diagnosis}), optimization ({@link DT_Service_Optimization}) and control
 * ({@link DT_Service_Control}) services.
 * </p>
 * <p>
 * A service is parameterized by a model {@code m ∈ M} and/or the data segment(s) it consumes
 * {@code ω ∈ Ω_D}: {@code s : M × Ω_D → Y_s}, where {@code Y_s} is the output produced by {@code s}
 * ({@link #execute}). Services are specified with the same formalism as models because both belong to the
 * class of automata {@code 𝒜} ({@code S ⊆ 𝒜}); hence this class extends {@link DT_Automaton} and inherits
 * the 7-tuple {@code 𝒮 = ⟨Λ, X, Y, Σ, Φ, Δ, Δ*⟩}.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public abstract class DT_Service<T> extends DT_Automaton implements ObserverData<T> {

    /** The name of the service (e.g., "Temperature Monitoring"). */
    protected String serviceName;

    /** A description of the service, providing additional details about its purpose. */
    protected String serviceDescription;

    /** The model {@code m ∈ M} the service is coupled to ({@code null} for a purely data-driven service). */
    protected DT_Model<T> model;

    /** The data consumed by the service, in the order received. */
    protected final List<T> consumedData = new ArrayList<>();

    /**
     * Constructor to initialize the service with a name and an optional description.
     *
     * @param serviceName        the name of the service.
     * @param serviceDescription a brief description of the service (may be {@code null}).
     */
    public DT_Service(String serviceName, String serviceDescription) {
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription != null ? serviceDescription : "No description provided.";
    }

    /**
     * Called when new data arrives: the datum is added to the data consumed by the service.
     *
     * @param data the new datum.
     */
    @Override
    public void update(T data) {
        consumedData.add(data);
    }

    /**
     * Runs the service, {@code s(m, ω) ∈ Y_s}: couples it to {@code m}, consumes the data of {@code ω} in
     * time order, then runs {@link #executeService()}.
     *
     * @param m     the model {@code m} (may be {@code null} for a data-driven service).
     * @param omega the data segment {@code ω} (may be {@code null} for a model-driven service).
     * @return the output of the service, an element of {@code Y_s}.
     */
    public Object execute(DT_Model<T> m, DT_DataSegment<T> omega) {
        this.model = m;
        if (omega != null) {
            consumedData.clear();
            for (T d : omega.getImage()) {
                update(d);
            }
        }
        return executeService();
    }

    /**
     * Executes the service logic on the coupled model and/or the consumed data.
     * Each subclass defines the specific behaviour of the service.
     *
     * @return the output of the service, an element of {@code Y_s}.
     */
    public abstract Object executeService();

    /** @return the model {@code m} the service is coupled to, or {@code null}. */
    public DT_Model<T> getModel() {
        return model;
    }

    /** @param model the model {@code m} to couple the service to. */
    public void setModel(DT_Model<T> model) {
        this.model = model;
    }

    /** @return the name of the service. */
    public String getServiceName() {
        return serviceName;
    }

    /** @param serviceName the new name of the service. */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /** @return the description of the service. */
    public String getServiceDescription() {
        return serviceDescription;
    }

    /** @param serviceDescription the new description of the service. */
    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    /** @return the data consumed by the service. */
    public List<T> getConsumedData() {
        return consumedData;
    }

    /**
     * Converts a datum to a number, when it is one.
     *
     * @param d the datum.
     * @return its numeric value, or {@code NaN} if the datum is not a number.
     */
    protected static double numericValue(Object d) {
        return d instanceof Number n ? n.doubleValue() : Double.NaN;
    }

    /**
     * Provides a string representation of the service.
     *
     * @return a string containing the service's name and description.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " [serviceName=" + serviceName
                + ", serviceDescription=" + serviceDescription + "]";
    }

    /**
     * Two services are considered equal if they have the same class and the same name.
     *
     * @param obj the object to compare with.
     * @return true if the services are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return Objects.equals(serviceName, ((DT_Service<?>) obj).serviceName);
    }

    /** @return the hash code of the service, based on its name. */
    @Override
    public int hashCode() {
        return Objects.hashCode(serviceName);
    }
}
