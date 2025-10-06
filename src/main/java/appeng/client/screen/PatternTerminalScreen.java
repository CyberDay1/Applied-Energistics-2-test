package appeng.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.OfflineOverlayRenderer;
import appeng.core.network.AE2Packets;
import appeng.menu.terminal.PatternTerminalMenu;

public class PatternTerminalScreen extends CraftingTerminalScreen {
    private static final int BLANK_PATTERN_X = 110;
    private static final int PATTERN_SLOT_Y = 46;
    private static final int ENCODED_PATTERN_X = BLANK_PATTERN_X + 18;

    private static final Component CRAFTING_MODE =
            Component.translatable("gui.ae2.pattern_terminal.mode.crafting");
    private static final Component PROCESSING_MODE =
            Component.translatable("gui.ae2.pattern_terminal.mode.processing");

    private Button modeButton;
    private Button planButton;

    public PatternTerminalScreen(PatternTerminalMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        int buttonX = this.leftPos + BLANK_PATTERN_X;
        int buttonY = this.topPos + PATTERN_SLOT_Y + 24;
        this.modeButton = Button.builder(Component.empty(), btn -> sendToggleRequest())
                .bounds(buttonX - 2, buttonY, 40, 20)
                .build();
        addRenderableWidget(this.modeButton);

        int planButtonX = this.leftPos + ENCODED_PATTERN_X - 2;
        this.planButton = Button.builder(Component.literal("Plan"), btn -> sendPlanRequest())
                .bounds(planButtonX, buttonY, 40, 20)
                .build();
        addRenderableWidget(this.planButton);
        updateModeButton();
        updatePlanButton();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        updateModeButton();
        updatePlanButton();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!this.menu.isGridOnline()) {
            renderPatternOfflineOverlay(graphics);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        int left = this.leftPos + BLANK_PATTERN_X;
        int top = this.topPos + PATTERN_SLOT_Y;
        graphics.fill(left, top, left + 16, top + 16, 0xFF3F3F3F);

        int encodedLeft = this.leftPos + ENCODED_PATTERN_X;
        graphics.fill(encodedLeft, top, encodedLeft + 16, top + 16, 0xFF3F3F3F);
    }

    private void renderPatternOfflineOverlay(GuiGraphics graphics) {
        int left = this.leftPos + BLANK_PATTERN_X;
        int top = this.topPos + PATTERN_SLOT_Y;
        OfflineOverlayRenderer.renderForReason(graphics, this.font, this.menu.getOfflineReason(), left, top, 16, 16);

        int encodedLeft = this.leftPos + ENCODED_PATTERN_X;
        OfflineOverlayRenderer.renderForReason(graphics, this.font, this.menu.getOfflineReason(), encodedLeft, top, 16,
                16);
    }

    private void sendToggleRequest() {
        Minecraft minecraft = this.minecraft;
        if (minecraft != null && minecraft.gameMode != null && minecraft.player != null) {
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }
    }

    private void sendPlanRequest() {
        if (this.menu.hasEncodedPattern()) {
            AE2Packets.planCraftingJob(this.menu.containerId, this.menu.getEncodedPatternSlotIndex());
        }
    }

    private void updateModeButton() {
        if (this.modeButton != null) {
            this.modeButton.setMessage(this.menu.isProcessingMode() ? PROCESSING_MODE : CRAFTING_MODE);
        }
    }

    private void updatePlanButton() {
        if (this.planButton != null) {
            this.planButton.active = this.menu.hasEncodedPattern();
        }
    }
}
