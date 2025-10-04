package appeng.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.registry.AE2BlockEntities;

public class ChargerBlockEntity extends BlockEntity {
    public ChargerBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.CHARGER_BE.get(), pos, state);
    }

    public static void tick(BlockPos pos, BlockState state, ChargerBlockEntity be) {
        // TODO: add charging logic
    }
}
