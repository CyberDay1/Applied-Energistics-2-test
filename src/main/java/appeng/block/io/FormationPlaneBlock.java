package appeng.block.io;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.io.FormationPlaneBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.FormationPlaneMenu;
import appeng.menu.locator.MenuLocators;

public class FormationPlaneBlock extends AEBaseEntityBlock<FormationPlaneBlockEntity> {

    public FormationPlaneBlock() {
        super(metalProps());
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facing();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        var blockEntity = getBlockEntity(level, pos);
        if (blockEntity != null) {
            if (!level.isClientSide()) {
                MenuOpener.open(FormationPlaneMenu.TYPE, player, MenuLocators.forBlockEntity(blockEntity));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}

