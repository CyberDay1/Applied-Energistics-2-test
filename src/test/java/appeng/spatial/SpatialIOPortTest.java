package appeng.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.items.storage.spatial.SpatialCellItem;
import appeng.registry.AE2Blocks;
import appeng.util.BootstrapMinecraftExtension;

@ExtendWith(BootstrapMinecraftExtension.class)
class SpatialIOPortTest {
    private SpatialIOPortBlockEntity blockEntity;

    @BeforeEach
    void setUp() {
        BlockState state = AE2Blocks.SPATIAL_IO_PORT.get().defaultBlockState();
        blockEntity = new SpatialIOPortBlockEntity(BlockPos.ZERO, state);
    }

    @Test
    void captureAndRestoreStubsAreCallable() {
        blockEntity.captureRegion();
        blockEntity.restoreRegion();

        blockEntity.getInternalInventory().setItemDirect(0, new ItemStack(new TestSpatialCellItem(2)));
        blockEntity.captureRegion();
        blockEntity.restoreRegion();
    }

    @Test
    void regionSizeMatchesCellTier() {
        blockEntity.getInternalInventory().setItemDirect(0, new ItemStack(new TestSpatialCellItem(2)));
        assertEquals(new BlockPos(2, 2, 2), blockEntity.getRegionSize());

        blockEntity.getInternalInventory().setItemDirect(0, new ItemStack(new TestSpatialCellItem(16)));
        assertEquals(new BlockPos(16, 16, 16), blockEntity.getRegionSize());

        blockEntity.getInternalInventory().setItemDirect(0, new ItemStack(new TestSpatialCellItem(128)));
        assertEquals(new BlockPos(128, 128, 128), blockEntity.getRegionSize());

        blockEntity.getInternalInventory().setItemDirect(0, new ItemStack(new TestSpatialCellItem(512)));
        assertEquals(new BlockPos(512, 512, 512), blockEntity.getRegionSize());
    }

    private static final class TestSpatialCellItem extends SpatialCellItem {
        TestSpatialCellItem(int size) {
            super(new Item.Properties(), size);
        }
    }
}
