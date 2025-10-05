package appeng.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class CableBlock extends Block {
    public CableBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLUE)
            .strength(1.0f));
    }

    public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction dir) {
        BlockState neighbor = level.getBlockState(pos.relative(dir));
        return neighbor.getBlock() instanceof CableBlock
            || neighbor.getBlock() instanceof ControllerBlock
            || neighbor.getBlock() instanceof EnergyAcceptorBlock;
    }
}
