package appeng.block.crafting;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.CraftingCoProcessorBlockEntity;

/**
 * Block representing a co-processing unit within a crafting CPU cluster.
 */
public class CraftingCoProcessorBlock extends AEBaseEntityBlock<CraftingCoProcessorBlockEntity> {

    public CraftingCoProcessorBlock() {
        super(metalProps().strength(4.0f));
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.facing();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide()) {
            var be = getBlockEntity(level, pos);
            if (be != null) {
                be.markStructureDirty();
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
            BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);

        if (!level.isClientSide()) {
            var be = getBlockEntity(level, pos);
            if (be != null) {
                be.markStructureDirty();
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            var be = getBlockEntity(level, pos);
            if (be != null) {
                be.handleRemoved();
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }
}

