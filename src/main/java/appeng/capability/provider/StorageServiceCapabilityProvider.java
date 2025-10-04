package appeng.capability.provider;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capability;

import appeng.AE2Capabilities;
import appeng.api.storage.IStorageHost;
import appeng.api.storage.IStorageService;

/**
 * Capability provider that exposes the storage service capability for AE2 block entities.
 */
public final class StorageServiceCapabilityProvider extends AE2BlockEntityCapabilityProvider {
    private final IStorageHost host;

    public StorageServiceCapabilityProvider(BlockEntity be, IStorageHost host) {
        super(be);
        this.host = host;
    }

    @Override
    public <T> T getCapability(BlockEntity object, Capability<T> cap, @Nullable Direction side) {
        if (cap == AE2Capabilities.STORAGE_SERVICE) {
            IStorageService service = host.getStorageService();
            if (service != null) {
                return AE2Capabilities.STORAGE_SERVICE.cast(service);
            }
        }
        return null;
    }
}
