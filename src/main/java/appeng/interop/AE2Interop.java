package appeng.interop;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.AE2Capabilities;
import appeng.api.grid.IGridNode;

/**
 * Simple helpers for third-party integrations to query AE2 grid node capabilities.
 */
public final class AE2Interop {
    private AE2Interop() {
    }

    public static boolean hasGridNode(BlockEntity be) {
        return getGridNode(be) != null;
    }

    @Nullable
    public static IGridNode getGridNode(BlockEntity be) {
        return be.getCapability(AE2Capabilities.GRID_NODE, null);
    }
}
