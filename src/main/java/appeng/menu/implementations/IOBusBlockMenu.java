package appeng.menu.implementations;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.IncludeExclude;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.blockentity.io.IOBusBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.grid.SimpleGridNode.OfflineReason;
import appeng.menu.guisync.GuiSync;

public abstract class IOBusBlockMenu<T extends IOBusBlockEntity> extends UpgradeableMenu<T> {

    private static final Supplier<List<Component>> IO_BUS_BLOCK_UPGRADE_TOOLTIP = IOBusMenu.IO_BUS_UPGRADE_TOOLTIP;

    @GuiSync(10)
    @Nullable
    public OfflineReason offlineReason;

    @GuiSync(11)
    public int transferCooldownTicks;

    @GuiSync(12)
    public int operationsPerTransfer;

    @GuiSync(13)
    public int activeFilterSlots;

    @GuiSync(14)
    public IncludeExclude partitionMode = IncludeExclude.WHITELIST;

    @GuiSync(15)
    public boolean hasRedstoneUpgrade;

    @GuiSync(16)
    public boolean redstoneActive;

    @GuiSync(17)
    public boolean hasFuzzyCard;

    @GuiSync(18)
    public boolean hasInverterUpgrade;

    protected IOBusBlockMenu(MenuType<? extends IOBusBlockMenu<T>> menuType, int id, Inventory inventory, T host) {
        super(menuType, id, inventory, host);
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
    protected void setupUpgrades() {
        super.setupUpgrades();
        setUpgradeSlotTooltip(IO_BUS_BLOCK_UPGRADE_TOOLTIP);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (isServerSide()) {
            var newOfflineReason = computeOfflineReason();
            if (!Objects.equals(offlineReason, newOfflineReason)) {
                offlineReason = newOfflineReason;
            }

            transferCooldownTicks = getHost().getTransferCooldownTicks();
            operationsPerTransfer = getHost().getOperationsPerTransfer();
            activeFilterSlots = getHost().getActiveConfigSlots();
            partitionMode = getHost().getPartitionMode();
            hasRedstoneUpgrade = getHost().hasRedstoneCard();
            redstoneActive = getHost().isRedstoneActive();
            hasFuzzyCard = getHost().hasFuzzyCard();
            hasInverterUpgrade = getHost().hasInverterCard();
        }
    }

    @Nullable
    public OfflineReason getOfflineReason() {
        return offlineReason;
    }

    public int getTransferCooldownTicks() {
        return transferCooldownTicks;
    }

    public int getOperationsPerTransfer() {
        return operationsPerTransfer;
    }

    public int getActiveFilterSlots() {
        return activeFilterSlots;
    }

    public IncludeExclude getPartitionMode() {
        return partitionMode;
    }

    public boolean hasRedstoneUpgrade() {
        return hasRedstoneUpgrade;
    }

    public boolean isRedstoneActive() {
        return redstoneActive;
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

        if (hasUpgrade(AEItems.REDSTONE_CARD)) {
            var mode = host.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
            if (mode != RedstoneMode.IGNORE) {
                var level = host.getLevel();
                var powered = level != null && level.hasNeighborSignal(host.getBlockPos());
                boolean enabled = switch (mode) {
                    case HIGH_SIGNAL, SIGNAL_PULSE -> powered;
                    case LOW_SIGNAL -> !powered;
                    case IGNORE -> true;
                };
                if (!enabled) {
                    return OfflineReason.REDSTONE;
                }
            }
        }

        return null;
    }
}
