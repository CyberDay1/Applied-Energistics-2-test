package appeng.api.storage;

/**
 * Represents a storage channel capable of storing, extracting and querying a specific type of
 * resource.
 *
 * @param <T> The type of resource handled by the channel.
 */
public interface IStorageChannel<T> {
    /**
     * Attempts to insert the specified amount of a resource into this storage channel.
     *
     * @param resource The resource to insert.
     * @param amount   The amount of the resource to insert.
     * @param simulate If {@code true}, the operation should only be simulated.
     * @return The amount of the resource that was accepted by the channel.
     */
    long insert(T resource, long amount, boolean simulate);

    /**
     * Attempts to extract the specified amount of a resource from this storage channel.
     *
     * @param resource The resource to extract.
     * @param amount   The amount of the resource to extract.
     * @param simulate If {@code true}, the operation should only be simulated.
     * @return The amount of the resource that was successfully extracted from the channel.
     */
    long extract(T resource, long amount, boolean simulate);

    /**
     * Gets the total amount of the specified resource stored in this channel.
     *
     * @param resource The resource to query.
     * @return The amount of the resource currently stored.
     */
    long getStoredAmount(T resource);

}
