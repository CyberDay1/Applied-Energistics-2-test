package appeng.menu.implementations;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.blockentity.crafting.PatternEncodingTerminalBlockEntity;
import appeng.core.network.AE2Packets;
import appeng.menu.AEBaseMenu;
import appeng.menu.MenuTypeBuilder;
import appeng.menu.SlotSemantics;

public class PatternEncodingTerminalMenu extends AEBaseMenu {
    private static final int GRID_WIDTH = 3;
    private static final int GRID_HEIGHT = 3;

    public static final MenuType<PatternEncodingTerminalMenu> TYPE = MenuTypeBuilder
            .create(PatternEncodingTerminalMenu::new, PatternEncodingTerminalBlockEntity.class)
            .build("pattern_encoding_terminal");

    private final PatternEncodingTerminalBlockEntity terminal;
    private boolean processingMode;

    public PatternEncodingTerminalMenu(int id, Inventory playerInventory,
            PatternEncodingTerminalBlockEntity terminal) {
        super(TYPE, id, playerInventory, terminal);
        this.terminal = terminal;

        addCraftingMatrixSlots(terminal.getCraftingMatrix());
        addPatternSlots(terminal.getPatternInventory());
        addProcessingModeSync();

        createPlayerInventorySlots(playerInventory);
    }

    private void addCraftingMatrixSlots(Container craftingMatrix) {
        int slotIndex = 0;
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                addSlot(new Slot(craftingMatrix, slotIndex++, 0, 0), SlotSemantics.CRAFTING_GRID);
            }
        }
    }

    private void addPatternSlots(Container patternInventory) {
        addSlot(new Slot(patternInventory, 0, 0, 0), SlotSemantics.BLANK_PATTERN);

        addSlot(new Slot(patternInventory, 1, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        }, SlotSemantics.ENCODED_PATTERN);
    }

    public ItemStack getCraftingResult() {
        return terminal.getCraftingResult();
    }

    public void encode() {
        if (isClientSide()) {
            AE2Packets.encodePattern(containerId);
        } else {
            encodeServer();
        }
    }

    public void encodeServer() {
        var result = terminal.encode();
        var player = getPlayer();
        if (player != null) {
            player.displayClientMessage(result.message(), result.success());
        }
    }

    public boolean isProcessingMode() {
        return processingMode;
    }

    public void toggleProcessingMode() {
        setProcessingMode(!isProcessingMode());
    }

    public void setProcessingMode(boolean processingMode) {
        if (isClientSide()) {
            AE2Packets.setPatternEncodingMode(containerId, processingMode);
        } else {
            setProcessingModeServer(processingMode);
        }
    }

    public void setProcessingModeServer(boolean processingMode) {
        if (this.processingMode == processingMode) {
            return;
        }

        this.processingMode = processingMode;
        terminal.setProcessingMode(processingMode);
        broadcastChanges();
    }

    private void addProcessingModeSync() {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                processingMode = terminal.isProcessingMode();
                return processingMode ? 1 : 0;
            }

            @Override
            public void set(int value) {
                processingMode = value != 0;
            }
        });
    }
}
