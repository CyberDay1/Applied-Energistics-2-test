package appeng.block.terminal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.Nullable;

import appeng.blockentity.terminal.PatternTerminalBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.terminal.PatternTerminalMenu;

public class PatternTerminalBlock extends CraftingTerminalBlock {
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PatternTerminalBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof PatternTerminalBlockEntity terminal) {
            if (!level.isClientSide()) {
                MenuOpener.open(PatternTerminalMenu.TYPE, player, MenuLocators.forBlockEntity(terminal));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
