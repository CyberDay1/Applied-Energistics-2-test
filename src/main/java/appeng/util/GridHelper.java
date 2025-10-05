package appeng.util;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class GridHelper {
    private GridHelper() {
    }

    public static void discover(BlockEntity be) {
        if (!(be instanceof IGridHost host)) {
            return;
        }

        IGridNode self = host.getGridNode();
        if (self == null) {
            return;
        }

        Level level = be.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        BlockPos pos = be.getBlockPos();
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor instanceof IGridHost nHost) {
                IGridNode neighborNode = nHost.getGridNode();
                if (neighborNode != null) {
                    self.connect(neighborNode);
                    neighborNode.connect(self);
                }
            }
        }
    }
}
