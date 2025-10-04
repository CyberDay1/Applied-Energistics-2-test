package appeng.api.storage;

/**
 * Represents a component that exposes an {@link IStorageService}.
 */
public interface IStorageHost {
    /**
     * Gets the storage service provided by this host.
     *
     * @return The storage service, or {@code null} if none is available.
     */
    IStorageService getStorageService();
}
