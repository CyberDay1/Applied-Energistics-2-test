package appeng.blockentity.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.core.definitions.AEBlocks;

/**
 * Block entity for the crafting co-processing unit. Extends the base crafting CPU block entity to join
 * multiblock clusters while contributing additional parallel job slots instead of storage capacity.
 */
public class CraftingCoProcessorBlockEntity extends CraftingCPUBlockEntity {
    private static final int CO_PROCESSOR_COUNT = 1;

    public CraftingCoProcessorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setVisualRepresentation(AEBlocks.CRAFTING_CO_PROCESSOR.stack());
    }

    @Override
    public int getBaseCapacity() {
        return 0;
    }

    @Override
    public int getCoProcessorCount() {
        return CO_PROCESSOR_COUNT;
    }
}

