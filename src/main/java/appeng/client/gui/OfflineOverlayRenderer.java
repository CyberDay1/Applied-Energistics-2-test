package appeng.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import appeng.grid.SimpleGridNode.OfflineReason;

/**
 * Renders the common offline overlay that is used across multiple AE2 screens. Centralizing the
 * rendering keeps the tint, text color and alignment consistent between terminals, monitors and
 * other client screens that want to communicate that their backing grid is unavailable.
 */
public final class OfflineOverlayRenderer {
    private static final int DEFAULT_OVERLAY_COLOR = 0xAA000000;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private OfflineOverlayRenderer() {
    }

    public static void render(GuiGraphics graphics, int x, int y, int width, int height) {
        render(graphics, x, y, width, height, DEFAULT_OVERLAY_COLOR);
    }

    public static void render(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }

    public static void renderWithMessage(GuiGraphics graphics, Font font, Component message, int x, int y, int width,
            int height) {
        render(graphics, x, y, width, height);

        if (font == null || message == null) {
            return;
        }

        int textWidth = font.width(message);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - font.lineHeight) / 2;
        graphics.drawString(font, message, textX, textY, TEXT_COLOR, false);
    }

    public static void renderForReason(GuiGraphics graphics, Font font, OfflineReason reason, int x, int y, int width,
            int height) {
        renderWithMessage(graphics, font, getMessageForReason(reason), x, y, width, height);
    }

    public static Component getMessageForReason(OfflineReason reason) {
        return switch (reason) {
            case REDSTONE -> Component.translatable("gui.appliedenergistics2.offline.redstone");
            case CHANNELS -> Component.translatable("gui.appliedenergistics2.offline.channels");
            case NONE -> Component.translatable("gui.appliedenergistics2.offline.power");
        };
    }
}
