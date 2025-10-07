package appeng.spatial;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity.LastAction;
import appeng.core.network.payload.SpatialCaptureC2SPayload;
import appeng.core.network.payload.SpatialOpCancelC2SPayload;
import appeng.core.network.payload.SpatialOpCancelS2CPayload;
import appeng.core.network.payload.SpatialOpCompleteS2CPayload;
import appeng.core.network.payload.SpatialOpInProgressS2CPayload;
import appeng.core.network.payload.SpatialRestoreC2SPayload;
import appeng.items.storage.spatial.SpatialCellItem;
import appeng.menu.spatial.SpatialIOPortMenu;
import appeng.client.screen.spatial.SpatialIOPortScreen;
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
        for (int i = 0; i < 6; i++) {
            blockEntity.tickOperation();
        }
        blockEntity.restoreRegion();
    }

    @Test
    void captureOperationMarksAndClearsInProgress() {
        setSpatialCell(4);
        blockEntity.captureRegion();
        assertTrue(blockEntity.isInProgress());

        for (int i = 0; i < 6; i++) {
            blockEntity.tickOperation();
        }

        assertFalse(blockEntity.isInProgress());
    }

    @Test
    void cancelOperationClearsInProgress() {
        setSpatialCell(4);
        blockEntity.captureRegion();
        assertTrue(blockEntity.isInProgress());

        blockEntity.cancelOperation();

        assertFalse(blockEntity.isInProgress());
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

        buffer.clear();
        var inProgress = new SpatialOpInProgressS2CPayload(7, BlockPos.ZERO, true);
        SpatialOpInProgressS2CPayload.STREAM_CODEC.encode(buffer, inProgress);
        var decodedOp = SpatialOpInProgressS2CPayload.STREAM_CODEC.decode(buffer);
        assertTrue(decodedOp.inProgress());

        buffer.clear();
        var complete = new SpatialOpCompleteS2CPayload(9, BlockPos.ZERO);
        SpatialOpCompleteS2CPayload.STREAM_CODEC.encode(buffer, complete);
        var decodedComplete = SpatialOpCompleteS2CPayload.STREAM_CODEC.decode(buffer);
        assertEquals(BlockPos.ZERO, decodedComplete.pos());

        buffer.clear();
        var cancelC2S = new SpatialOpCancelC2SPayload(11, BlockPos.ZERO);
        SpatialOpCancelC2SPayload.STREAM_CODEC.encode(buffer, cancelC2S);
        var decodedCancelC2S = SpatialOpCancelC2SPayload.STREAM_CODEC.decode(buffer);
        assertEquals(cancelC2S.pos(), decodedCancelC2S.pos());

        buffer.clear();
        var cancelS2C = new SpatialOpCancelS2CPayload(12, BlockPos.ZERO);
        SpatialOpCancelS2CPayload.STREAM_CODEC.encode(buffer, cancelS2C);
        var decodedCancelS2C = SpatialOpCancelS2CPayload.STREAM_CODEC.decode(buffer);
        assertEquals(cancelS2C.containerId(), decodedCancelS2C.containerId());
    }

    @Test
    void datagenContainsSpatialLocalization() throws IOException {
        Path path = Path.of("src/generated/resources/assets/ae2/lang/en_us.json");
        try (var reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals("Capture", json.get("gui.ae2.spatial.capture").getAsString());
            assertEquals("Restore", json.get("gui.ae2.spatial.restore").getAsString());
            assertEquals("Spatial IO in progress", json.get("gui.ae2.spatial.in_progress").getAsString());
            assertEquals("Capturing region %s³…", json.get("log.ae2.spatial.capture_begin").getAsString());
            assertEquals("Restoring region %s³…", json.get("log.ae2.spatial.restore_begin").getAsString());
            assertEquals("Spatial operation completed.", json.get("log.ae2.spatial.complete").getAsString());
            assertEquals("Operation complete", json.get("gui.ae2.spatial.complete").getAsString());
            assertEquals("Spatial operation cancelled.", json.get("log.ae2.spatial.cancelled").getAsString());
            assertEquals("Operation cancelled", json.get("gui.ae2.spatial.cancelled").getAsString());
        }
    }

    @Test
    void screenDisablesButtonsDuringOperations() throws Exception {
        setSpatialCell(16);
        blockEntity.captureRegion();

        var menu = new SpatialIOPortMenu(0, new Inventory(null), blockEntity);
        menu.setInProgress(true);

        var screen = new SpatialIOPortScreen(menu, new Inventory(null), Component.literal("Spatial"));

        var captureButton = Button.builder(Component.empty(), button -> {
        }).bounds(0, 0, 10, 10).build();
        var restoreButton = Button.builder(Component.empty(), button -> {
        }).bounds(0, 0, 10, 10).build();
        var cancelButton = Button.builder(Component.empty(), button -> {
        }).bounds(0, 0, 10, 10).build();

        Field captureField = SpatialIOPortScreen.class.getDeclaredField("captureButton");
        captureField.setAccessible(true);
        captureField.set(screen, captureButton);

        Field restoreField = SpatialIOPortScreen.class.getDeclaredField("restoreButton");
        restoreField.setAccessible(true);
        restoreField.set(screen, restoreButton);

        Field cancelField = SpatialIOPortScreen.class.getDeclaredField("cancelButton");
        cancelField.setAccessible(true);
        cancelField.set(screen, cancelButton);

        Method updateButtons = SpatialIOPortScreen.class.getDeclaredMethod("updateButtonStates");
        updateButtons.setAccessible(true);

        updateButtons.invoke(screen);

        assertFalse(captureButton.active);
        assertFalse(restoreButton.active);
        assertTrue(cancelButton.active);
        assertNotNull(captureButton.getTooltip());
        assertNotNull(restoreButton.getTooltip());
        assertNull(cancelButton.getTooltip());

        menu.handleOperationComplete();
        updateButtons.invoke(screen);

        assertTrue(captureButton.active);
        assertTrue(restoreButton.active);
        assertFalse(cancelButton.active);
        assertNull(captureButton.getTooltip());
        assertNull(restoreButton.getTooltip());
        assertNotNull(cancelButton.getTooltip());
        assertTrue(menu.isShowingCompletionMessage());

        menu.clientTick();
        assertTrue(menu.isShowingCompletionMessage());
    }

    @Test
    void completionPayloadReenablesButtonsAndClearsAfterTicks() {
        var menu = new SpatialIOPortMenu(0, new Inventory(null), blockEntity);
        menu.setInProgress(true);
        menu.handleOperationComplete();

        assertFalse(menu.isInProgress());
        assertTrue(menu.isShowingCompletionMessage());
        assertFalse(menu.isShowingCancelledMessage());

        for (int i = 0; i < 60; i++) {
            menu.clientTick();
        }

        assertFalse(menu.isShowingCompletionMessage());
    }

    @Test
    void cancelPayloadReenablesButtonsAndClearsAfterTicks() {
        var menu = new SpatialIOPortMenu(0, new Inventory(null), blockEntity);
        menu.setInProgress(true);
        menu.handleOperationCancelled();

        assertFalse(menu.isInProgress());
        assertTrue(menu.isShowingCancelledMessage());
        assertFalse(menu.isShowingCompletionMessage());

        for (int i = 0; i < 60; i++) {
            menu.clientTick();
        }

        assertFalse(menu.isShowingCancelledMessage());
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
