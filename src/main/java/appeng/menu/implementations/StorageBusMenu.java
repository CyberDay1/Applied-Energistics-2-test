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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.collect.Iterators;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.YesNo;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.util.IConfigManager;
import appeng.client.gui.implementations.StorageBusScreen;
import appeng.core.definitions.AEItems;
import appeng.core.localization.Tooltips;
import appeng.core.network.AE2Packets;
import appeng.grid.SimpleGridNode.OfflineReason;
import appeng.menu.guisync.GuiSync;
import appeng.parts.storagebus.StorageBusPart;

/**
 * @see StorageBusScreen
 * @see StorageBusScreen
 */
public class StorageBusMenu extends UpgradeableMenu<StorageBusPart> {

    static final Supplier<List<Component>> STORAGE_BUS_UPGRADE_TOOLTIP = () -> Tooltips.slotTooltip(
            Component.translatable("gui.ae2.upgrades.capacity"),
            Component.translatable("gui.ae2.upgrades.fuzzy"),
            Component.translatable("gui.ae2.upgrades.inverter"));

    private static final String ACTION_CLEAR = "clear";
    private static final String ACTION_PARTITION = "partition";

    public static final MenuType<StorageBusMenu> TYPE = MenuTypeBuilder
            .create(StorageBusMenu::new, StorageBusPart.class)
            .build("storagebus");

    @GuiSync(3)
    public AccessRestriction rwMode = AccessRestriction.READ_WRITE;

    @GuiSync(4)
    public StorageFilter storageFilter = StorageFilter.EXTRACTABLE_ONLY;

    @GuiSync(7)
    public YesNo filterOnExtract = YesNo.YES;

    @GuiSync(8)
    @Nullable
    public Component connectedTo;

    @GuiSync(9)
    @Nullable
    public OfflineReason offlineReason;

    private AccessRestriction lastSentAccessMode;
    private StorageFilter lastSentStorageFilter;
    private YesNo lastSentFilterOnExtract;
    @Nullable
    private Component lastSentConnectedTo;

    public StorageBusMenu(MenuType<StorageBusMenu> menuType, int id, Inventory ip, StorageBusPart te) {
        super(menuType, id, ip, te);

        registerClientAction(ACTION_CLEAR, this::clear);
        registerClientAction(ACTION_PARTITION, this::partition);

        this.connectedTo = te.getConnectedToDescription();
    }

    @Override
    protected void setupConfig() {
        addExpandableConfigSlots(getHost().getConfig(), 2, 9, 5);
    }

    @Override
    protected void setupUpgrades() {
        super.setupUpgrades();
        setUpgradeSlotTooltip(STORAGE_BUS_UPGRADE_TOOLTIP);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        this.connectedTo = getHost().getConnectedToDescription();

        if (!isClientSide() && getPlayer() instanceof ServerPlayer serverPlayer) {
            var configManager = getHost().getConfigManager();
            var accessMode = configManager.getSetting(Settings.ACCESS);
            var storageFilter = configManager.getSetting(Settings.STORAGE_FILTER);
            var filterOnExtract = configManager.getSetting(Settings.FILTER_ON_EXTRACT);
            var connectedToDescription = getHost().getConnectedToDescription();

            boolean changed = accessMode != lastSentAccessMode
                    || storageFilter != lastSentStorageFilter
                    || filterOnExtract != lastSentFilterOnExtract
                    || !Objects.equals(connectedToDescription, lastSentConnectedTo);

            if (changed) {
                AE2Packets.sendStorageBusState(serverPlayer, containerId, accessMode, storageFilter, filterOnExtract,
                        connectedToDescription);
                lastSentAccessMode = accessMode;
                lastSentStorageFilter = storageFilter;
                lastSentFilterOnExtract = filterOnExtract;
                lastSentConnectedTo = connectedToDescription;
            }

            var newOfflineReason = computeOfflineReason();
            if (!Objects.equals(offlineReason, newOfflineReason)) {
                offlineReason = newOfflineReason;
            }
        }
    }

    @Override
    protected void loadSettingsFromHost(IConfigManager cm) {
        this.setFuzzyMode(cm.getSetting(Settings.FUZZY_MODE));
        this.setReadWriteMode(cm.getSetting(Settings.ACCESS));
        this.setStorageFilter(cm.getSetting(Settings.STORAGE_FILTER));
        this.setFilterOnExtract(cm.getSetting(Settings.FILTER_ON_EXTRACT));
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        final int upgrades = getUpgrades().getInstalledUpgrades(AEItems.CAPACITY_CARD);

        return upgrades > idx;
    }

    public void clear() {
        if (isClientSide()) {
            sendClientAction(ACTION_CLEAR);
            return;
        }

        getHost().getConfig().clear();
        this.broadcastChanges();
    }

    public void partition() {
        if (isClientSide()) {
            sendClientAction(ACTION_PARTITION);
            return;
        }

        var inv = getHost().getConfig();
        var cellInv = getHost().getInternalHandler();

        Iterator<AEKey> i = Collections.emptyIterator();
        if (cellInv != null) {
            i = Iterators.transform(cellInv.getAvailableStacks().iterator(), Map.Entry::getKey);
        }

        inv.beginBatch();
        try {
            for (int x = 0; x < inv.size(); x++) {
                if (i.hasNext() && this.isSlotEnabled(x / 9 - 2)) {
                    inv.setStack(x, new GenericStack(i.next(), 1));
                } else {
                    inv.setStack(x, null);
                }
            }
        } finally {
            inv.endBatch();
        }

        this.broadcastChanges();
    }

    public AccessRestriction getReadWriteMode() {
        return this.rwMode;
    }

    private void setReadWriteMode(AccessRestriction rwMode) {
        this.rwMode = rwMode;
    }

    public StorageFilter getStorageFilter() {
        return this.storageFilter;
    }

    private void setStorageFilter(StorageFilter storageFilter) {
        this.storageFilter = storageFilter;
    }

    public YesNo getFilterOnExtract() {
        return this.filterOnExtract;
    }

    public void setFilterOnExtract(YesNo filterOnExtract) {
        this.filterOnExtract = filterOnExtract;
    }

    public void applyState(AccessRestriction accessMode, StorageFilter storageFilter, YesNo filterOnExtract,
            @Nullable Component connectedTo) {
        setReadWriteMode(accessMode);
        setStorageFilter(storageFilter);
        setFilterOnExtract(filterOnExtract);
        this.connectedTo = connectedTo;
    }

    public boolean supportsFuzzySearch() {
        return hasUpgrade(AEItems.FUZZY_CARD);
    }

    @Nullable
    public Component getConnectedTo() {
        return connectedTo;
    }

    @Nullable
    public OfflineReason getOfflineReason() {
        return offlineReason;
    }

    @Nullable
    private OfflineReason computeOfflineReason() {
        var managedNode = getHost().getMainNode();

        if (!managedNode.isPowered()) {
            return OfflineReason.NONE;
        }

        var node = managedNode.getNode();
        if (node == null) {
            return OfflineReason.NONE;
        }

        if (!node.meetsChannelRequirements()) {
            return OfflineReason.CHANNELS;
        }

        if (!managedNode.hasGridBooted()) {
            return OfflineReason.NONE;
        }

        return null;
    }
}
