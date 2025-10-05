package appeng.menu.terminal;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import appeng.blockentity.terminal.PatternTerminalBlockEntity;
import appeng.core.AppEng;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;

public class PatternTerminalMenu extends CraftingTerminalMenu {
    public static final MenuType<PatternTerminalMenu> TYPE = MenuTypeBuilder
            .create(PatternTerminalMenu::new, PatternTerminalBlockEntity.class)
            .buildUnregistered(AppEng.makeId("pattern_terminal"));

    private static final int BLANK_PATTERN_X = 110;
    private static final int PATTERN_SLOT_Y = 46;
    private static final int ENCODED_PATTERN_X = BLANK_PATTERN_X + 18;

    private final PatternTerminalBlockEntity patternTerminal;
    private final Container patternInventory;

    public PatternTerminalMenu(int id, Inventory inv, PatternTerminalBlockEntity terminal) {
        super(TYPE, id, inv, terminal);
        this.patternTerminal = terminal;
        this.patternInventory = terminal.getPatternInventory();

        addPatternSlots();
        addProcessingModeSync();
    }

    private void addPatternSlots() {
        Slot blankPatternSlot = new Slot(patternInventory, 0, BLANK_PATTERN_X, PATTERN_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        };
        addSlot(blankPatternSlot, SlotSemantics.BLANK_PATTERN);

        Slot encodedPatternSlot = new Slot(patternInventory, 1, ENCODED_PATTERN_X, PATTERN_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        };
        addSlot(encodedPatternSlot, SlotSemantics.ENCODED_PATTERN);
    }

    private void addProcessingModeSync() {
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return patternTerminal.isProcessingMode() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                patternTerminal.setProcessingMode(value != 0);
            }
        });
    }

    public boolean isProcessingMode() {
        return patternTerminal.isProcessingMode();
    }

    public void toggleProcessingMode(Player player) {
        if (!player.level().isClientSide()) {
            patternTerminal.setProcessingMode(!patternTerminal.isProcessingMode());
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == 0) {
            toggleProcessingMode(player);
            return true;
        }
        return super.clickMenuButton(player, buttonId);
    }
}
