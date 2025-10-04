package appeng.util;

import appeng.api.storage.IStorageHost;
import appeng.api.storage.IStorageService;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Utility helpers for interacting with {@link IStorageHost} implementations.
 */
public final class StorageHelper {
    private StorageHelper() {
    }

    /**
     * Attempts to retrieve the {@link IStorageService} exposed by the given block entity.
     *
     * @param be The block entity to query.
     * @return The storage service if the block entity implements {@link IStorageHost}, otherwise {@code null}.
     */
    public static IStorageService getStorage(BlockEntity be) {
        if (be instanceof IStorageHost host) {
            return host.getStorageService();
        }
        return null;
    }
}
