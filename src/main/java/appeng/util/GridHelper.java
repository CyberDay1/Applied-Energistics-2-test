package appeng.util;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;

/**
 * Utility helpers for working with grid-hosting block entities via capabilities.
 */
public final class GridHelper {
    private GridHelper() {
    }

    @Nullable
    public static IGridNode getNode(BlockEntity be) {
        if (be instanceof IGridHost host) {
            return host.getGridNode();
        }
        return null;
    }
}
