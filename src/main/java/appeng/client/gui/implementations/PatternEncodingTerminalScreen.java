package appeng.client.gui.implementations;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.ClientReadOnlySlot;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.core.localization.ActionItems;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.PatternEncodingTerminalMenu;

public class PatternEncodingTerminalScreen
        extends AEBaseScreen<PatternEncodingTerminalMenu> {

    private final ToggleButton modeToggle;

    public PatternEncodingTerminalScreen(PatternEncodingTerminalMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        var encodeButton = new ActionButton(ActionItems.ENCODE, btn -> menu.encode());
        widgets.add("encodePattern", encodeButton);

        modeToggle = new ToggleButton(Icon.TAB_PROCESSING, Icon.TAB_CRAFTING, menu::setProcessingMode);
        modeToggle.setTooltipOn(List.of(
                Component.translatable("gui.appliedenergistics2.pattern_encoding.mode.processing"),
                Component.translatable("gui.appliedenergistics2.pattern_encoding.mode_toggle_hint")));
        modeToggle.setTooltipOff(List.of(
                Component.translatable("gui.appliedenergistics2.pattern_encoding.mode.crafting"),
                Component.translatable("gui.appliedenergistics2.pattern_encoding.mode_toggle_hint")));
        widgets.add("modeToggle", modeToggle);

        menu.addClientSideSlot(new ResultDisplaySlot(menu), SlotSemantics.CRAFTING_RESULT);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        modeToggle.setState(getMenu().isProcessingMode());
    }

    private static class ResultDisplaySlot extends ClientReadOnlySlot {
        private final PatternEncodingTerminalMenu menu;

        ResultDisplaySlot(PatternEncodingTerminalMenu menu) {
            this.menu = menu;
        }

        @Override
        public ItemStack getItem() {
            return menu.getCraftingResult();
        }
    }
}
