package appeng.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.client.gui.OfflineOverlayRenderer;
import appeng.menu.terminal.CraftingTerminalMenu;

public class CraftingTerminalScreen extends TerminalScreen {
    private static final int GRID_SIZE = 3;
    private static final int SLOT_SIZE = 18;
    private static final int GRID_LEFT_OFFSET = 8;
    private static final int GRID_TOP_OFFSET = 28;
    private static final int RESULT_LEFT_OFFSET = GRID_LEFT_OFFSET + GRID_SIZE * SLOT_SIZE + 12;
    private static final int RESULT_TOP_OFFSET = GRID_TOP_OFFSET + SLOT_SIZE;
    private static final int GRID_BOTTOM_MARGIN = 12;

    public CraftingTerminalScreen(CraftingTerminalMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageHeight = 190 + GRID_SIZE * SLOT_SIZE + GRID_BOTTOM_MARGIN;
    }

    @Override
    protected int getListTopOffset() {
        return GRID_TOP_OFFSET + GRID_SIZE * SLOT_SIZE + GRID_BOTTOM_MARGIN;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!this.menu.isGridOnline()) {
            renderGridOfflineOverlay(graphics);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(graphics, partialTicks, mouseX, mouseY);

        int left = this.leftPos + GRID_LEFT_OFFSET;
        int top = this.topPos + GRID_TOP_OFFSET;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int slotX = left + col * SLOT_SIZE;
                int slotY = top + row * SLOT_SIZE;
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF3F3F3F);
            }
        }

        int resultX = this.leftPos + RESULT_LEFT_OFFSET;
        int resultY = this.topPos + RESULT_TOP_OFFSET;
        graphics.fill(resultX, resultY, resultX + 16, resultY + 16, 0xFF3F3F3F);
    }

    private void renderGridOfflineOverlay(GuiGraphics graphics) {
        int left = this.leftPos + GRID_LEFT_OFFSET;
        int top = this.topPos + GRID_TOP_OFFSET;
        OfflineOverlayRenderer.renderForReason(graphics, this.font, this.menu.getOfflineReason(), left, top,
                GRID_SIZE * SLOT_SIZE, GRID_SIZE * SLOT_SIZE);

        int resultX = this.leftPos + RESULT_LEFT_OFFSET;
        int resultY = this.topPos + RESULT_TOP_OFFSET;
        OfflineOverlayRenderer.render(graphics, resultX, resultY, 16, 16);
    }
}
