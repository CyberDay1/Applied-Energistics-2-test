package appeng.block.terminal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import appeng.blockentity.terminal.TerminalBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.terminal.TerminalMenu;

public class TerminalBlock extends Block implements EntityBlock {
    public TerminalBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0f, 3.0f));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TerminalBlockEntity(pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.getBlockEntity(pos) instanceof TerminalBlockEntity terminal) {
            terminal.onNeighborChanged();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof TerminalBlockEntity terminal) {
            if (!level.isClientSide()) {
                MenuOpener.open(TerminalMenu.TYPE, player, MenuLocators.forBlockEntity(terminal));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
