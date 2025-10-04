package appeng;

import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.CapabilityToken;

import appeng.api.grid.IGridNode;
import appeng.api.storage.IStorageService;

public final class AE2Capabilities {
    public static final Capability<IGridNode> GRID_NODE = new CapabilityToken<>() {
    };
    public static final Capability<IStorageService> STORAGE_SERVICE = new CapabilityToken<>() {
    };

    private AE2Capabilities() {
    }
}
