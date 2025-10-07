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

package appeng.menu.implementations;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.stacks.AEKey;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.FormationPlaneScreen;
import appeng.core.definitions.AEItems;
import appeng.core.network.AE2Packets;
import appeng.menu.guisync.GuiSync;

/**
 * @see FormationPlaneScreen
 */
public class FormationPlaneMenu extends UpgradeableMenu<FormationPlaneMenuHost> {

    public static final MenuType<FormationPlaneMenu> TYPE = MenuTypeBuilder
            .create(FormationPlaneMenu::new, FormationPlaneMenuHost.class)
            .build("formationplane");

    @GuiSync(7)
    public YesNo placeMode;

    @GuiSync(8)
    public boolean planeActive;

    private Boolean lastSentPlaneState;

    public FormationPlaneMenu(MenuType<FormationPlaneMenu> type, int id, Inventory ip,
            FormationPlaneMenuHost host) {
        super(type, id, ip, host);
    }

    @Override
    protected void setupConfig() {
        addExpandableConfigSlots(getHost().getConfig(), 2, 9, 5);
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        if (supportsFuzzyRangeSearch()) {
            this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        }
        this.setPlaceMode(cm.getSetting(Settings.PLACE_BLOCK));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!isClientSide() && getPlayer() instanceof ServerPlayer serverPlayer) {
            var active = getHost().isPlaneActive();
            planeActive = active;

            if (lastSentPlaneState == null || lastSentPlaneState != active) {
                AE2Packets.sendPlaneActivity(serverPlayer, containerId, active);
                lastSentPlaneState = active;
            }
        }
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        final int upgrades = getUpgrades().getInstalledUpgrades(AEItems.CAPACITY_CARD);
        return upgrades > idx;
    }

    public YesNo getPlaceMode() {
        return this.placeMode;
    }

    private void setPlaceMode(YesNo placeMode) {
        this.placeMode = placeMode;
    }

    public boolean supportsFuzzyMode() {
        return hasUpgrade(AEItems.FUZZY_CARD) && supportsFuzzyRangeSearch();
    }

    private boolean supportsFuzzyRangeSearch() {
        for (AEKey key : this.getHost().getConfig().keySet()) {
            if (key.supportsFuzzyRangeSearch()) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlaneOnline() {
        return planeActive;
    }

    public void applyPlaneActivity(boolean active) {
        this.planeActive = active;
    }
}
