package appeng.block.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.PatternEncodingTerminalBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.PatternEncodingTerminalMenu;
import appeng.menu.locator.MenuLocators;

public class PatternEncodingTerminalBlock extends AEBaseEntityBlock<PatternEncodingTerminalBlockEntity> {

    public PatternEncodingTerminalBlock() {
        super(metalProps().noOcclusion());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        var be = getBlockEntity(level, pos);
        if (be != null) {
            if (!level.isClientSide()) {
                MenuOpener.open(PatternEncodingTerminalMenu.TYPE, player,
                        MenuLocators.forBlockEntity(be));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }
}
