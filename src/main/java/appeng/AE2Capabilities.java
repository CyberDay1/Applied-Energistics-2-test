package appeng;

import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.CapabilityToken;

import appeng.api.grid.IGridNode;

public final class AE2Capabilities {
    public static final Capability<IGridNode> GRID_NODE = new CapabilityToken<>() {
    };

    private AE2Capabilities() {
    }
}
