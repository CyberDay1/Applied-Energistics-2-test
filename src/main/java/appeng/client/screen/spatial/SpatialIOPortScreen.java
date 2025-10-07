package appeng.client.screen.spatial;

import java.util.ArrayList;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.blockentity.spatial.SpatialIOPortBlockEntity;
import appeng.core.network.AE2Packets;
import appeng.menu.spatial.SpatialIOPortMenu;

public class SpatialIOPortScreen extends AbstractContainerScreen<SpatialIOPortMenu> {
    private Button captureButton;
    private Button restoreButton;
    private Button cancelButton;
    private static final int STATUS_COLOR = 0x55FF55;

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

        captureButton = addRenderableWidget(Button.builder(Component.translatable("gui.ae2.spatial.capture"), button -> {
            AE2Packets.sendSpatialCapture(menu.containerId, menu.getBlockPos());
        }).bounds(left + 10, top + 20, 80, 20).build());

        restoreButton = addRenderableWidget(Button.builder(Component.translatable("gui.ae2.spatial.restore"), button -> {
            AE2Packets.sendSpatialRestore(menu.containerId, menu.getBlockPos());
        }).bounds(left + 10, top + 50, 80, 20).build());

        cancelButton = addRenderableWidget(Button.builder(Component.translatable("gui.ae2.Cancel"), button -> {
            AE2Packets.sendSpatialCancel(menu.containerId, menu.getBlockPos());
        }).bounds(left + 100, top + 35, 66, 20).build());

        updateButtonStates();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        menu.clientTick();
        updateButtonStates();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderRegionSize(graphics);
        renderStatus(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        // TODO: Draw spatial IO port background once textures are available.
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFFFFF, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.hoveredSlot != null && this.hoveredSlot.index == 0) {
            var stack = this.hoveredSlot.getItem();
            if (stack.isEmpty()) {
                graphics.renderTooltip(this.font, Component.translatable("tooltip.ae2.spatial.no_cell"), mouseX,
                        mouseY);
            } else {
                var tooltip = new ArrayList<>(this.getTooltipFromItem(stack));
                var regionSize = SpatialIOPortBlockEntity.getRegionSizeFromCell(stack);
                if (!regionSize.equals(net.minecraft.core.BlockPos.ZERO)) {
                    tooltip.add(Component.translatable("gui.ae2.spatial.region_size",
                            formatRegionSize(regionSize)));
                }
                graphics.renderTooltip(this.font, tooltip, stack.getTooltipImage(), mouseX, mouseY);
            }
        } else {
            super.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    private void renderRegionSize(GuiGraphics graphics) {
        var regionSize = menu.getRegionSize();
        Component text;
        if (regionSize.equals(BlockPos.ZERO)) {
            text = Component.translatable("gui.ae2.spatial.region_size", "-");
        } else {
            text = Component.translatable("gui.ae2.spatial.region_size", formatRegionSize(regionSize));
        }

        graphics.drawString(this.font, text, this.leftPos + 10, this.topPos + 80, 0xFFFFFF, false);
    }

    private void renderStatus(GuiGraphics graphics) {
        if (menu.isShowingCompletionMessage()) {
            var text = Component.translatable("gui.ae2.spatial.complete");
            graphics.drawString(this.font, text, this.leftPos + 10, this.topPos + 100, STATUS_COLOR, false);
        } else if (menu.isShowingCancelledMessage()) {
            var text = Component.translatable("gui.ae2.spatial.cancelled");
            graphics.drawString(this.font, text, this.leftPos + 10, this.topPos + 100, STATUS_COLOR, false);
        }
    }

    private static String formatRegionSize(BlockPos regionSize) {
        return regionSize.getX() + "x" + regionSize.getY() + "x" + regionSize.getZ();
    }

    private void updateButtonStates() {
        if (captureButton == null || restoreButton == null || cancelButton == null) {
            return;
        }

        var cellStack = this.menu.getSlot(0).getItem();
        var hasCell = !cellStack.isEmpty();
        var hasSize = !this.menu.getRegionSize().equals(BlockPos.ZERO);

        boolean active = hasCell && hasSize;
        boolean inProgress = menu.isInProgress();

        captureButton.active = active && !inProgress;
        restoreButton.active = active && !inProgress;
        cancelButton.active = inProgress;
        cancelButton.setTooltip(inProgress ? null : Tooltip.create(Component.translatable("gui.ae2.spatial.in_progress")));

        Tooltip tooltip = inProgress ? Tooltip.create(Component.translatable("gui.ae2.spatial.in_progress")) : null;
        captureButton.setTooltip(tooltip);
        restoreButton.setTooltip(tooltip);
    }
}
