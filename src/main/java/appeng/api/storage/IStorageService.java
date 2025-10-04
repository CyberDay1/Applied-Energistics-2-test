package appeng.api.storage;

import java.util.List;

/**
 * Provides access to the storage channels exposed by a storage host.
 */
public interface IStorageService {
    /**
     * Gets a storage channel for the specified resource type.
     *
     * @param type The type handled by the requested channel.
     * @param <T>  The resource type handled by the channel.
     * @return The channel if available, or {@code null}.
     */
    <T> IStorageChannel<T> getChannel(Class<T> type);

    /**
     * Gets all storage channels currently exposed by this service.
     *
     * @return The available channels.
     */
    List<IStorageChannel<?>> getAllChannels();
}
