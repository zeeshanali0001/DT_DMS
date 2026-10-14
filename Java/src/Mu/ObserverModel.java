package Mu;

import Models.DT_Model;

/**
 * Observer of the model observer pattern: an object (typically a service {@code s ∈ S}) that wants to be told
 * when a model {@code m ∈ M} has been updated by {@code μ}.
 * <p>
 * The subject is the model ({@link DT_Model#addServiceToBeUpdated}); after each assimilation
 * {@code μ(ω, m) = m'}, the model calls {@link #updateWithModel} on its observers.
 * </p>
 *
 * @param <T> the type of a datum {@code d ∈ D}.
 */
public interface ObserverModel<T> {

    /**
     * Called when the model {@code m} has been updated.
     *
     * @param m the updated model {@code m'}.
     */
    void updateWithModel(DT_Model<T> m);
}
