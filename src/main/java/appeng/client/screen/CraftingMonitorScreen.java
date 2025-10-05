package appeng.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.GenericStack;
import appeng.crafting.monitor.CraftingMonitorEntry;
import appeng.menu.me.crafting.CraftingMonitorMenu;

public class CraftingMonitorScreen extends AbstractContainerScreen<CraftingMonitorMenu> {

    public CraftingMonitorScreen(CraftingMonitorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + imageWidth, top + imageHeight, 0xFF202020);

        int y = top + 20;
        for (CraftingMonitorEntry entry : menu.getJobs()) {
            var stack = entry.stack();
            ItemStack renderStack = stack != null ? GenericStack.wrapInItemStack(stack) : ItemStack.EMPTY;
            if (!renderStack.isEmpty()) {
                graphics.renderItem(renderStack, left + 8, y);
            }

            int barX = left + 30;
            int barWidth = imageWidth - 40;
            int barHeight = 12;
            graphics.fill(barX, y, barX + barWidth, y + barHeight, 0xFF3A3A3A);
            int progressPixels = (int) ((barWidth - 2) * entry.progress());
            if (progressPixels > 0) {
                graphics.fill(barX + 1, y + 1, barX + 1 + progressPixels, y + barHeight - 1, 0xFF5BC04A);
            }

            Component name = stack != null ? stack.what().getDisplayName() : Component.literal("Crafting Job");
            graphics.drawString(font, name, barX + 4, y - 10, 0xFFFFFF, false);
            var percent = Math.round(entry.progress() * 100);
            graphics.drawString(font, percent + "%", barX + 4, y + 2, 0xFFFFFFFF, false);

            y += 26;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 6, 0xFFFFFF, false);
        graphics.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
