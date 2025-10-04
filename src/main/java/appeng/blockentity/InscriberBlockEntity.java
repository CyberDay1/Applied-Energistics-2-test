package appeng.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.registry.AE2BlockEntities;

public class InscriberBlockEntity extends BlockEntity {
    public InscriberBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.INSCRIBER_BE.get(), pos, state);
    }

    public static void tick(BlockPos pos, BlockState state, InscriberBlockEntity be) {
        // TODO: add processing logic
    }
}
