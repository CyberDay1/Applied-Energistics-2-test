/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.gui.implementations;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.YesNo;
import appeng.client.gui.OfflineOverlayRenderer;
import appeng.client.gui.Icon;
import appeng.client.gui.style.PaletteColor;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.core.localization.GuiText;
import appeng.menu.implementations.StorageBusMenu;

import net.minecraft.client.renderer.Rect2i;

public class StorageBusScreen extends UpgradeableScreen<StorageBusMenu> {

    private final SettingToggleButton<AccessRestriction> rwMode;
    private final SettingToggleButton<StorageFilter> storageFilter;
    private final SettingToggleButton<YesNo> filterOnExtract;
    private final SettingToggleButton<FuzzyMode> fuzzyMode;
    private final SettingToggleButton<IncludeExclude> filterMode;

    public StorageBusScreen(StorageBusMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        widgets.addOpenPriorityButton();

        addToLeftToolbar(new ActionButton(ActionItems.CLOSE, btn -> menu.clear()));
        addToLeftToolbar(new ActionButton(ActionItems.COG, btn -> menu.partition()));
        this.rwMode = new ServerSettingToggleButton<>(Settings.ACCESS, AccessRestriction.READ_WRITE);
        this.storageFilter = new ServerSettingToggleButton<>(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.filterOnExtract = new ServerSettingToggleButton<>(Settings.FILTER_ON_EXTRACT, YesNo.YES);
        this.fuzzyMode = new ServerSettingToggleButton<>(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);

        this.addToLeftToolbar(this.storageFilter);
        this.addToLeftToolbar(this.filterOnExtract);
        this.addToLeftToolbar(this.fuzzyMode);
        this.filterMode = new ServerSettingToggleButton<>(Settings.PARTITION_MODE, IncludeExclude.WHITELIST);
        this.addToLeftToolbar(this.filterMode);
        this.addToLeftToolbar(this.rwMode);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.storageFilter.set(this.menu.getStorageFilter());
        this.rwMode.set(this.menu.getReadWriteMode());
        this.filterOnExtract.set(this.menu.getFilterOnExtract());
        this.fuzzyMode.set(this.menu.getFuzzyMode());
        this.fuzzyMode.setVisibility(menu.supportsFuzzySearch());
        this.filterMode.set(menu.getPartitionMode());
        this.filterMode.setVisibility(menu.canEditFilterMode());
    }

    @Override
    public void drawFG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY) {
        super.drawFG(guiGraphics, offsetX, offsetY, mouseX, mouseY);

        var poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(10, 17, 0);
        poseStack.scale(0.6f, 0.6f, 1);
        var color = style.getColor(PaletteColor.DEFAULT_TEXT_COLOR);
        if (menu.getConnectedTo() != null) {
            guiGraphics.drawString(font, GuiText.AttachedTo.text(menu.getConnectedTo()), 0, 0, color.toARGB(), false);
        } else {
            guiGraphics.drawString(font, GuiText.Unattached.text(), 0, 0, color.toARGB(), false);
        }
        poseStack.popPose();

        drawIndicator(guiGraphics, offsetX, offsetY, "transferCooldown", Icon.SLOT_BACKGROUND,
                Component.translatable("gui.ae2.io_bus.filter_slots", menu.getActiveFilterSlots()));

        var filterKey = menu.getPartitionMode() == IncludeExclude.BLACKLIST
                ? "gui.ae2.io_bus.filter_mode.blacklist"
                : "gui.ae2.io_bus.filter_mode.whitelist";
        drawIndicator(guiGraphics, offsetX, offsetY, "operationsPerTransfer",
                menu.getPartitionMode() == IncludeExclude.BLACKLIST ? Icon.BLACKLIST : Icon.WHITELIST,
                Component.translatable(filterKey));

        var fuzzyText = menu.supportsFuzzySearch()
                ? Component.translatable("gui.ae2.io_bus.fuzzy.enabled")
                : Component.translatable("gui.ae2.io_bus.fuzzy.disabled");
        drawIndicator(guiGraphics, offsetX, offsetY, "fuzzyIndicator",
                menu.supportsFuzzySearch() ? Icon.FUZZY_PERCENT_99 : Icon.FUZZY_IGNORE,
                fuzzyText);

        var toggleText = menu.canEditFilterMode()
                ? Component.translatable("gui.ae2.io_bus.filter_toggle.available")
                : Component.translatable("gui.ae2.io_bus.filter_toggle.unavailable");
        drawIndicator(guiGraphics, offsetX, offsetY, "filterMode",
                menu.canEditFilterMode() ? Icon.BLACKLIST : Icon.WHITELIST,
                toggleText);

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
