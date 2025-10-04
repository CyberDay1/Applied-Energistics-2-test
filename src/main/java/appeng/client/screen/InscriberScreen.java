package appeng.client.screen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.menu.InscriberMenu;

public class InscriberScreen extends AbstractContainerScreen<InscriberMenu> {
    public InscriberScreen(InscriberMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        // TODO: draw background
    }
}
