package appeng.client.screen.spatial;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.core.network.AE2Packets;
import appeng.menu.spatial.SpatialIOPortMenu;

public class SpatialIOPortScreen extends AbstractContainerScreen<SpatialIOPortMenu> {
    public SpatialIOPortScreen(SpatialIOPortMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.leftPos;
        int top = this.topPos;

        addRenderableWidget(Button.builder(Component.translatable("gui.ae2.spatial.capture"), button -> {
            AE2Packets.sendSpatialCapture(menu.containerId, menu.getBlockPos());
        }).bounds(left + 10, top + 20, 80, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.ae2.spatial.restore"), button -> {
            AE2Packets.sendSpatialRestore(menu.containerId, menu.getBlockPos());
        }).bounds(left + 10, top + 50, 80, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        // TODO: Draw spatial IO port background once textures are available.
    }
}
