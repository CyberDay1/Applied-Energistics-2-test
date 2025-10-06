package appeng.block.simple;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.Nullable;

import appeng.blockentity.simple.DriveBlockEntity;

public class DriveBlock extends Block implements EntityBlock {
    public DriveBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f, 6.0f));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DriveBlockEntity(pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.getBlockEntity(pos) instanceof DriveBlockEntity drive) {
            drive.onNeighborChanged();
        }
    }
}
