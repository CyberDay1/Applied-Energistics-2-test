package appeng.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.RedstoneMode;
import appeng.client.gui.OfflineOverlayRenderer;
import appeng.menu.simple.SimpleDriveMenu;

public class SimpleDriveScreen extends AbstractContainerScreen<SimpleDriveMenu> {
    private Button redstoneButton;

    public SimpleDriveScreen(SimpleDriveMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int buttonSize = 20;
        int buttonX = this.leftPos + this.imageWidth - buttonSize - 8;
        int buttonY = this.topPos + 5;
        this.redstoneButton = Button.builder(Component.translatable("gui.appliedenergistics2.redstone.button"), btn -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId,
                        SimpleDriveMenu.BUTTON_TOGGLE_REDSTONE);
            }
        }).bounds(buttonX, buttonY, buttonSize, buttonSize).build();
        addRenderableWidget(this.redstoneButton);
        updateRedstoneButton();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateRedstoneButton();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (!this.menu.isGridOnline()) {
            renderOfflineOverlay(graphics);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF2F2F2F);

        int slotBaseX = this.leftPos + 62;
        int slotBaseY = this.topPos + 20;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                int slotX = slotBaseX + col * 18;
                int slotY = slotBaseY + row * 18;
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF3F3F3F);
            }
        }

        int inventoryBaseY = this.topPos + 84;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = this.leftPos + 8 + col * 18;
                int slotY = inventoryBaseY + row * 18;
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF3F3F3F);
            }
        }

        int hotbarY = this.topPos + 142;
        for (int col = 0; col < 9; col++) {
            int slotX = this.leftPos + 8 + col * 18;
            graphics.fill(slotX, hotbarY, slotX + 16, hotbarY + 16, 0xFF3F3F3F);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0xFFFFFF, false);
    }

    private void updateRedstoneButton() {
        if (this.redstoneButton == null) {
            return;
        }

        RedstoneMode mode = this.menu.getRedstoneMode();
        Component tooltip;
        Component label;
        switch (mode) {
            case HIGH_SIGNAL -> {
                tooltip = Component.translatable("gui.appliedenergistics2.redstone.mode.active_with_signal");
                label = Component.translatable("gui.appliedenergistics2.redstone.mode.short.active_with_signal");
            }
            case LOW_SIGNAL -> {
                tooltip = Component.translatable("gui.appliedenergistics2.redstone.mode.active_without_signal");
                label = Component.translatable("gui.appliedenergistics2.redstone.mode.short.active_without_signal");
            }
            default -> {
                tooltip = Component.translatable("gui.appliedenergistics2.redstone.mode.always_active");
                label = Component.translatable("gui.appliedenergistics2.redstone.mode.short.always_active");
            }
        }

        this.redstoneButton.setMessage(label);
        this.redstoneButton.setTooltip(Tooltip.create(tooltip));
    }

    private void renderOfflineOverlay(GuiGraphics graphics) {
        int x = this.leftPos + 62;
        int y = this.topPos + 20;
        int width = 18 * 2;
        int height = 18 * 2;
        OfflineOverlayRenderer.renderForReason(graphics, this.font, this.menu.getOfflineReason(), x, y, width, height);
    }
}
