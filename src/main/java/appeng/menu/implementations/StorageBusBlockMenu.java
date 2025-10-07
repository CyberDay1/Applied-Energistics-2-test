package appeng.menu.implementations;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.IncludeExclude;
import appeng.blockentity.storage.StorageBusBlockEntity;
import appeng.grid.SimpleGridNode.OfflineReason;
import appeng.menu.guisync.GuiSync;

public class StorageBusBlockMenu extends UpgradeableMenu<StorageBusBlockEntity> {

    public static final MenuType<StorageBusBlockMenu> TYPE = MenuTypeBuilder
            .create(StorageBusBlockMenu::new, StorageBusBlockEntity.class)
            .build("storage_bus_block");

    @GuiSync(11)
    @Nullable
    public OfflineReason offlineReason;

    @GuiSync(12)
    public IncludeExclude partitionMode = IncludeExclude.WHITELIST;

    @GuiSync(13)
    public int activeFilterSlots = 18;

    @GuiSync(14)
    public boolean hasFuzzyCard;

    @GuiSync(15)
    public boolean hasInverterUpgrade;

    public StorageBusBlockMenu(MenuType<? extends StorageBusBlockMenu> menuType, int id, Inventory inventory,
            StorageBusBlockEntity host) {
        super(menuType, id, inventory, host);
    }

    @Override
    protected void setupUpgrades() {
        super.setupUpgrades();
        setUpgradeSlotTooltip(StorageBusMenu.STORAGE_BUS_UPGRADE_TOOLTIP);
    }

    @Override
    protected void setupConfig() {
        addExpandableConfigSlots(getHost().getConfig(), 2, 9, 5);
    }

    @Override
    public boolean isSlotEnabled(int idx) {
        int rowsUnlocked = Math.max(0, (getHost().getActiveConfigSlots() - 18) / 9);
        return idx < rowsUnlocked;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (isServerSide()) {
            var newOfflineReason = computeOfflineReason();
            if (!Objects.equals(offlineReason, newOfflineReason)) {
                offlineReason = newOfflineReason;
            }

            partitionMode = getHost().getPartitionMode();
            activeFilterSlots = getHost().getActiveConfigSlots();
            hasFuzzyCard = getHost().hasFuzzyCard();
            hasInverterUpgrade = getHost().hasInverterCard();
        }
    }

    @Nullable
    public OfflineReason getOfflineReason() {
        return offlineReason;
    }

    public IncludeExclude getPartitionMode() {
        return partitionMode;
    }

    public int getActiveFilterSlots() {
        return activeFilterSlots;
    }

    public boolean hasFuzzyUpgrade() {
        return hasFuzzyCard;
    }

    public boolean hasInverterUpgrade() {
        return hasInverterUpgrade;
    }

    public boolean canEditFilterMode() {
        return hasInverterUpgrade;
    }

    @Nullable
    private OfflineReason computeOfflineReason() {
        var host = getHost();
        var managedNode = host.getMainNode();

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
