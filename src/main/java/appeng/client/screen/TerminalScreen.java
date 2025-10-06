package appeng.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import net.neoforged.neoforge.network.PacketDistributor;

import appeng.api.config.RedstoneMode;
import appeng.api.storage.ItemStackView;
import appeng.client.gui.OfflineOverlayRenderer;
import appeng.core.network.serverbound.TerminalExtractPacket;
import appeng.menu.terminal.TerminalMenu;

public class TerminalScreen extends AbstractContainerScreen<TerminalMenu> {
    private static final int SLOT_SIZE = 18;
    private static final int ITEMS_PER_ROW = 4;
    private static final int VISIBLE_ROWS = 3;
    private static final int LIST_LEFT_OFFSET = 7;
    private static final int LIST_TOP_OFFSET = 24;

    private EditBox searchBox;
    private Button redstoneButton;
    private int scrollIndex;
    private boolean scrolling;
    private List<ItemStackView> currentItems = List.of();

    protected int getSlotSize() {
        return SLOT_SIZE;
    }

    protected int getItemsPerRow() {
        return ITEMS_PER_ROW;
    }

    protected int getVisibleRows() {
        return VISIBLE_ROWS;
    }

    protected int getListLeftOffset() {
        return LIST_LEFT_OFFSET;
    }

    protected int getListTopOffset() {
        return LIST_TOP_OFFSET;
    }

    protected int getItemsPerPage() {
        return getItemsPerRow() * getVisibleRows();
    }

    protected int getListLeft() {
        return this.leftPos + getListLeftOffset();
    }

    protected int getListTop() {
        return this.topPos + getListTopOffset();
    }

    public TerminalScreen(TerminalMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 194;
        this.imageHeight = 190;
    }

    @Override
    protected void init() {
        super.init();
        this.searchBox = new EditBox(this.font, this.leftPos + 8, this.topPos + 6, 120, 12,
                Component.translatable("gui.appliedenergistics2.terminal.search"));
        this.searchBox.setHint(Component.translatable("gui.appliedenergistics2.terminal.search_hint"));
        this.searchBox.setResponder(value -> this.scrollIndex = 0);
        this.searchBox.setMaxLength(64);
        this.addRenderableWidget(this.searchBox);
        this.setInitialFocus(this.searchBox);

        int buttonSize = 20;
        int buttonX = this.leftPos + this.imageWidth - buttonSize - 8;
        int buttonY = this.topPos + 5;
        this.redstoneButton = Button.builder(Component.translatable("gui.appliedenergistics2.redstone.button"), btn -> {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId,
                        TerminalMenu.BUTTON_TOGGLE_REDSTONE);
            }
        }).bounds(buttonX, buttonY, buttonSize, buttonSize).build();
        this.addRenderableWidget(this.redstoneButton);
        updateRedstoneButton();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.searchBox.tick();
        updateRedstoneButton();
    }

    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int width, int height) {
        String previousSearch = this.searchBox.getValue();
        super.resize(minecraft, width, height);
        this.searchBox.setValue(previousSearch);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.searchBox.isFocused() && this.searchBox.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        int scrollbarX = getListLeft() + getItemsPerRow() * getSlotSize() + 4;
        int scrollbarY = getListTop();
        int scrollbarWidth = 6;
        int scrollbarHeight = getVisibleRows() * getSlotSize();
        if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarWidth && mouseY >= scrollbarY
                && mouseY <= scrollbarY + scrollbarHeight) {
            this.scrolling = true;
            this.updateScrollFromY(mouseY);
            return true;
        }
        if (isWithinItemArea(mouseX, mouseY) && (button == 0 || button == 1)) {
            handleItemClick(mouseX, mouseY, button);
            return true;
        }
        return handled;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling) {
            this.updateScrollFromY(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int listX = getListLeft();
        int listY = getListTop();
        int listWidth = getItemsPerRow() * getSlotSize();
        int listHeight = getVisibleRows() * getSlotSize();
        if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= listY + listHeight) {
            int maxScroll = getMaxScroll();
            if (maxScroll > 0) {
                this.scrollIndex = Mth.clamp(this.scrollIndex - (int) Math.signum(delta), 0, maxScroll);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.currentItems = computeFilteredItems();
        int maxScroll = getMaxScroll();
        if (this.scrollIndex > maxScroll) {
            this.scrollIndex = maxScroll;
        }

        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (!this.menu.isGridOnline()) {
            renderOfflineOverlay(graphics);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF2F2F2F);

        int listX = getListLeft();
        int listY = getListTop();
        int startIndex = this.scrollIndex;

        for (int row = 0; row < getVisibleRows(); row++) {
            for (int col = 0; col < getItemsPerRow(); col++) {
                int index = startIndex + row * getItemsPerRow() + col;
                int slotX = listX + col * getSlotSize();
                int slotY = listY + row * getSlotSize();
                graphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF3F3F3F);
                if (index < this.currentItems.size()) {
                    ItemStackView view = this.currentItems.get(index);
                    ItemStack stack = view.asStack();
                    int displayCount = view.count();
                    if (displayCount > stack.getMaxStackSize()) {
                        stack.setCount(stack.getMaxStackSize());
                    }
                    this.itemRenderer.renderAndDecorateItem(stack, slotX, slotY);
                    this.itemRenderer.renderGuiItemDecorations(this.font, stack, slotX, slotY,
                            formatAmount(displayCount));
                }
            }
        }

        drawScrollbar(graphics, listX, listY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFFFFF, false);
    }

    private List<ItemStackView> computeFilteredItems() {
        String query = this.searchBox.getValue().toLowerCase(Locale.ROOT).trim();
        List<ItemStackView> source = this.menu.getClientItems();
        if (query.isEmpty()) {
            return new ArrayList<>(source);
        }
        List<ItemStackView> filtered = new ArrayList<>();
        for (ItemStackView view : source) {
            ItemStack stack = view.asStack();
            String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
            if (name.contains(query)) {
                filtered.add(view);
            }
        }
        return filtered;
    }

    private void drawScrollbar(GuiGraphics graphics, int listX, int listY) {
        int barX = listX + getItemsPerRow() * getSlotSize() + 4;
        int barY = listY;
        int barWidth = 6;
        int barHeight = getVisibleRows() * getSlotSize();
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF1B1B1B);

        int handleHeight = getScrollHandleHeight(barHeight);
        int maxScroll = getMaxScroll();
        int handleOffset = maxScroll == 0 ? 0
                : (int) ((barHeight - handleHeight) * (this.scrollIndex / (double) maxScroll));
        graphics.fill(barX + 1, barY + handleOffset, barX + barWidth - 1, barY + handleOffset + handleHeight,
                0xFF8A8A8A);
    }

    private int getScrollHandleHeight(int barHeight) {
        if (this.currentItems.size() <= getItemsPerPage()) {
            return barHeight;
        }
        int handle = (int) Math.max(8,
                Math.floor((double) barHeight * getItemsPerPage() / (double) this.currentItems.size()));
        return Math.min(barHeight, handle);
    }

    private int getMaxScroll() {
        int max = this.currentItems.size() - getItemsPerPage();
        return Math.max(0, max);
    }

    private void updateScrollFromY(double mouseY) {
        int barTop = getListTop();
        int barHeight = getVisibleRows() * getSlotSize();
        int handleHeight = getScrollHandleHeight(barHeight);
        double relative = (mouseY - barTop - handleHeight / 2.0) / (barHeight - handleHeight);
        relative = Mth.clamp(relative, 0.0, 1.0);
        int maxScroll = getMaxScroll();
        this.scrollIndex = (int) Math.round(relative * maxScroll);
    }

    private void renderOfflineOverlay(GuiGraphics graphics) {
        int x = getListLeft();
        int y = getListTop();
        int width = getItemsPerRow() * getSlotSize();
        int height = getVisibleRows() * getSlotSize();
        OfflineOverlayRenderer.renderForReason(graphics, this.font, this.menu.getOfflineReason(), x, y, width, height);
    }

    private void updateRedstoneButton() {
        if (this.redstoneButton == null) {
            return;
        }
        var mode = this.menu.getRedstoneMode();
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

    private String formatAmount(int count) {
        if (count >= 1_000_000) {
            return String.format(Locale.ROOT, "%dM", count / 1_000_000);
        }
        if (count >= 1_000) {
            return String.format(Locale.ROOT, "%dk", count / 1_000);
        }
        return Integer.toString(count);
    }

    private boolean isWithinItemArea(double mouseX, double mouseY) {
        int listX = getListLeft();
        int listY = getListTop();
        int listWidth = getItemsPerRow() * getSlotSize();
        int listHeight = getVisibleRows() * getSlotSize();
        return mouseX >= listX && mouseX < listX + listWidth && mouseY >= listY && mouseY < listY + listHeight;
    }

    private void handleItemClick(double mouseX, double mouseY, int button) {
        if (!this.menu.isGridOnline()) {
            return;
        }

        int listX = getListLeft();
        int listY = getListTop();
        int col = (int) ((mouseX - listX) / getSlotSize());
        int row = (int) ((mouseY - listY) / getSlotSize());
        if (col < 0 || col >= getItemsPerRow() || row < 0 || row >= getVisibleRows()) {
            return;
        }

        int index = this.scrollIndex + row * getItemsPerRow() + col;
        if (index < 0 || index >= this.currentItems.size()) {
            return;
        }

        ItemStackView view = this.currentItems.get(index);
        int amount;
        if (button == 1) {
            amount = 1;
        } else if (hasShiftDown()) {
            amount = view.count();
        } else {
            amount = Math.min(view.count(), view.item().getDefaultMaxStackSize());
        }

        if (amount <= 0) {
            return;
        }

        PacketDistributor.sendToServer(new TerminalExtractPacket(this.menu.containerId,
                BuiltInRegistries.ITEM.getKey(view.item()), amount));
    }
}
