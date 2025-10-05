package appeng.blockentity;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.registry.AE2BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CableBlockEntity extends BlockEntity implements IGridHost {
    private final IGridNode gridNode = new IGridNode() { };

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.CABLE.get(), pos, state);
    }

    @Override
    public IGridNode getGridNode() {
        return gridNode;
    }
}
