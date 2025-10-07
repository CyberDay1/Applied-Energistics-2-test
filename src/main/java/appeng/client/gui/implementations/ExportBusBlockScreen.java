package appeng.client.gui.implementations;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.OfflineOverlayRenderer;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.definitions.AEItems;
import appeng.menu.implementations.ExportBusBlockMenu;

import net.minecraft.client.renderer.Rect2i;
import appeng.client.gui.style.PaletteColor;

public class ExportBusBlockScreen extends UpgradeableScreen<ExportBusBlockMenu> {

    private final SettingToggleButton<RedstoneMode> redstoneMode;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final SettingToggleButton<YesNo> craftMode;
    private final SettingToggleButton<SchedulingMode> schedulingMode;
    private final SettingToggleButton<IncludeExclude> filterMode;

    public ExportBusBlockScreen(ExportBusBlockMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.redstoneMode = new ServerSettingToggleButton<>(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        addToLeftToolbar(this.redstoneMode);

        this.fuzzyMode = new ServerSettingToggleButton<>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        addToLeftToolbar(this.fuzzyMode);

        if (menu.getHost().getConfigManager().hasSetting(Settings.CRAFT_ONLY)) {
            this.craftMode = new ServerSettingToggleButton<>(Settings.CRAFT_ONLY, YesNo.NO);
            addToLeftToolbar(this.craftMode);
        } else {
            this.craftMode = null;
        }

        if (menu.getHost().getConfigManager().hasSetting(Settings.SCHEDULING_MODE)) {
            this.schedulingMode = new ServerSettingToggleButton<>(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
            addToLeftToolbar(this.schedulingMode);
        } else {
            this.schedulingMode = null;
        }

        this.filterMode = new ServerSettingToggleButton<>(Settings.PARTITION_MODE, IncludeExclude.WHITELIST);
        addToLeftToolbar(this.filterMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.redstoneMode.set(menu.getRedStoneMode());
        this.redstoneMode.setVisibility(true);
        this.redstoneMode.setActive(menu.hasRedstoneUpgrade());

        this.fuzzyMode.set(menu.getFuzzyMode());
        this.fuzzyMode.setVisibility(true);
        this.fuzzyMode.setActive(menu.hasFuzzyUpgrade());

        if (this.craftMode != null) {
            this.craftMode.set(menu.getCraftingMode());
            this.craftMode.setVisibility(menu.hasUpgrade(AEItems.CRAFTING_CARD));
        }

        if (this.schedulingMode != null) {
            this.schedulingMode.set(menu.getSchedulingMode());
        }

        this.filterMode.set(menu.getPartitionMode());
        this.filterMode.setVisibility(true);
        this.filterMode.setActive(menu.canEditFilterMode());
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);

        drawIndicator(guiGraphics, offsetX, offsetY, "transferCooldown", Icon.LEVEL_ENERGY,
                Component.translatable("gui.ae2.io_bus.transfer_cooldown", menu.getTransferCooldownTicks()));
        drawIndicator(guiGraphics, offsetX, offsetY, "operationsPerTransfer", Icon.LEVEL_ITEM,
                Component.translatable("gui.ae2.io_bus.operations_per_transfer", menu.getOperationsPerTransfer()));
        drawIndicator(guiGraphics, offsetX, offsetY, "slotIndicator", Icon.SLOT_BACKGROUND,
                Component.translatable("gui.ae2.io_bus.filter_slots", menu.getActiveFilterSlots()));

        var filterKey = menu.getPartitionMode() == IncludeExclude.BLACKLIST
                ? "gui.ae2.io_bus.filter_mode.blacklist"
                : "gui.ae2.io_bus.filter_mode.whitelist";
        var filterIcon = menu.getPartitionMode() == IncludeExclude.BLACKLIST ? Icon.BLACKLIST : Icon.WHITELIST;
        drawIndicator(guiGraphics, offsetX, offsetY, "filterMode", filterIcon, Component.translatable(filterKey));

        var redstoneIcon = menu.hasRedstoneUpgrade() && menu.isRedstoneActive() ? Icon.REDSTONE_ON : Icon.REDSTONE_OFF;
        Component redstoneText;
        if (!menu.hasRedstoneUpgrade()) {
            redstoneText = Component.translatable("gui.ae2.io_bus.redstone_control.disabled");
        } else if (menu.isRedstoneActive()) {
            redstoneText = Component.translatable("gui.ae2.io_bus.redstone_control.enabled");
        } else {
            redstoneText = Component.translatable("gui.ae2.io_bus.redstone_control.blocked");
        }
        drawIndicator(guiGraphics, offsetX, offsetY, "redstoneIndicator", redstoneIcon, redstoneText);

        var fuzzyText = menu.hasFuzzyUpgrade()
                ? Component.translatable("gui.ae2.io_bus.fuzzy.enabled")
                : Component.translatable("gui.ae2.io_bus.fuzzy.disabled");
        var fuzzyIcon = menu.hasFuzzyUpgrade() ? Icon.FUZZY_PERCENT_99 : Icon.FUZZY_IGNORE;
        drawIndicator(guiGraphics, offsetX, offsetY, "fuzzyIndicator", fuzzyIcon, fuzzyText);

        var toggleText = menu.canEditFilterMode()
                ? Component.translatable("gui.ae2.io_bus.filter_toggle.available")
                : Component.translatable("gui.ae2.io_bus.filter_toggle.unavailable");
        var toggleIcon = menu.canEditFilterMode() ? Icon.VALID : Icon.INVALID;
        drawIndicator(guiGraphics, offsetX, offsetY, "filterToggle", toggleIcon, toggleText);

        OfflineOverlayRenderer.drawIfOffline(guiGraphics, this.font, menu.getOfflineReason(),
                offsetX + 8, offsetY + 29, 18 * 9, 18 * 7);
    }

    private void drawIndicator(GuiGraphics guiGraphics, int offsetX, int offsetY, String widgetId, Icon icon,
            Component text) {
        var widget = style.getWidget(widgetId);
        var bounds = new Rect2i(offsetX, offsetY, this.imageWidth, this.imageHeight);
        var point = widget.resolve(bounds);
        icon.getBlitter().dest(point.getX(), point.getY()).blit(guiGraphics);
        var color = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR);
        guiGraphics.drawString(font, text, point.getX() + 12, point.getY() + 1, color.toARGB(), false);
    }
}
