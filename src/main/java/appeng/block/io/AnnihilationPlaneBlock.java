package appeng.block.io;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.io.AnnihilationPlaneBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.AnnihilationPlaneMenu;
import appeng.menu.locator.MenuLocators;

public class AnnihilationPlaneBlock extends AEBaseEntityBlock<AnnihilationPlaneBlockEntity> {

    public AnnihilationPlaneBlock() {
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
                MenuOpener.open(AnnihilationPlaneMenu.TYPE, player, MenuLocators.forBlockEntity(blockEntity));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        var blockEntity = getBlockEntity(level, pos);
        if (blockEntity != null) {
            blockEntity.onNeighborChanged();
        }
    }
}
