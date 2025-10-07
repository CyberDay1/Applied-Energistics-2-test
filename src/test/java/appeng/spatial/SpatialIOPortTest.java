package appeng.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity.LastAction;
import appeng.core.network.payload.SpatialCaptureC2SPayload;
import appeng.core.network.payload.SpatialRestoreC2SPayload;
import appeng.items.storage.spatial.SpatialCellItem;
import appeng.menu.spatial.SpatialIOPortMenu;
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

        setSpatialCell(2);
        blockEntity.captureRegion();
        blockEntity.restoreRegion();
    }

    @Test
    void regionSizeMatchesCellTier() {
        setSpatialCell(2);
        assertEquals(new BlockPos(2, 2, 2), blockEntity.getRegionSize());

        setSpatialCell(16);
        assertEquals(new BlockPos(16, 16, 16), blockEntity.getRegionSize());

        setSpatialCell(128);
        assertEquals(new BlockPos(128, 128, 128), blockEntity.getRegionSize());

        setSpatialCell(512);
        assertEquals(new BlockPos(512, 512, 512), blockEntity.getRegionSize());
    }

    @Test
    void getRegionSizeFromCellUsesSpatialCellTier() {
        var cell = new ItemStack(new TestSpatialCellItem(16));
        assertEquals(new BlockPos(16, 16, 16), SpatialIOPortBlockEntity.getRegionSizeFromCell(cell));

        assertEquals(BlockPos.ZERO, SpatialIOPortBlockEntity.getRegionSizeFromCell(ItemStack.EMPTY));
    }

    @Test
    void captureCachesComputedRegionSize() {
        setSpatialCell(128);
        blockEntity.captureRegion();
        assertEquals(new BlockPos(128, 128, 128), blockEntity.getRegionSize());

        blockEntity.getInternalInventory().setItemDirect(0, ItemStack.EMPTY);
        blockEntity.captureRegion();
        assertEquals(BlockPos.ZERO, blockEntity.getRegionSize());
    }

    @Test
    void captureRestoreUpdateLastAction() {
        setSpatialCell(16);
        blockEntity.captureRegion();
        assertEquals(LastAction.CAPTURE, blockEntity.getLastAction());

        blockEntity.restoreRegion();
        assertEquals(LastAction.RESTORE, blockEntity.getLastAction());
    }

    @Test
    void menuStoresRegionSizeFromServer() {
        var menu = new SpatialIOPortMenu(0, new Inventory(null), blockEntity);
        var expected = new BlockPos(32, 32, 32);
        menu.updateRegionSize(expected);
        assertEquals(expected, menu.getRegionSize());
        assertNotNull(menu.getSpatialCellStack());
    }

    @Test
    void payloadRoundTripKeepsRegionSize() {
        var payload = new SpatialCaptureC2SPayload(5, BlockPos.ZERO, new BlockPos(4, 4, 4));
        var buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        SpatialCaptureC2SPayload.STREAM_CODEC.encode(buffer, payload);
        var decoded = SpatialCaptureC2SPayload.STREAM_CODEC.decode(buffer);
        assertEquals(new BlockPos(4, 4, 4), decoded.regionSize());

        var restorePayload = new SpatialRestoreC2SPayload(5, BlockPos.ZERO, new BlockPos(8, 8, 8));
        buffer.clear();
        SpatialRestoreC2SPayload.STREAM_CODEC.encode(buffer, restorePayload);
        var decodedRestore = SpatialRestoreC2SPayload.STREAM_CODEC.decode(buffer);
        assertEquals(new BlockPos(8, 8, 8), decodedRestore.regionSize());
    }

    @Test
    void datagenContainsSpatialLocalization() throws IOException {
        Path path = Path.of("src/generated/resources/assets/ae2/lang/en_us.json");
        try (var reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals("Capture", json.get("gui.ae2.spatial.capture").getAsString());
            assertEquals("Restore", json.get("gui.ae2.spatial.restore").getAsString());
            assertEquals("Spatial capture initiated (%s)",
                    json.get("log.ae2.spatial.capture_start").getAsString());
            assertEquals("Spatial restore initiated (%s)",
                    json.get("log.ae2.spatial.restore_start").getAsString());
        }
    }

    private static final class TestSpatialCellItem extends SpatialCellItem {
        TestSpatialCellItem(int size) {
            super(new Item.Properties(), size);
        }
    }

    private void setSpatialCell(int size) {
        blockEntity.getInternalInventory().setItemDirect(0, new ItemStack(new TestSpatialCellItem(size)));
        blockEntity.onChangeInventory(blockEntity.getInternalInventory(), 0);
    }
}
