package appeng.client.gui.implementations;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.ClientReadOnlySlot;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.core.localization.ActionItems;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.PatternEncodingTerminalMenu;

public class PatternEncodingTerminalScreen
        extends AEBaseScreen<PatternEncodingTerminalMenu> {

    public PatternEncodingTerminalScreen(PatternEncodingTerminalMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        var encodeButton = new ActionButton(ActionItems.ENCODE, btn -> menu.encode());
        widgets.add("encodePattern", encodeButton);

        menu.addClientSideSlot(new ResultDisplaySlot(menu), SlotSemantics.CRAFTING_RESULT);
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
