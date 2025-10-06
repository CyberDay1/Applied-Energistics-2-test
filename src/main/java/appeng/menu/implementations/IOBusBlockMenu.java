package appeng.menu.implementations;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

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

    protected IOBusBlockMenu(MenuType<? extends IOBusBlockMenu<T>> menuType, int id, Inventory inventory, T host) {
        super(menuType, id, inventory, host);
    }

    @Override
    protected void setupConfig() {
        addExpandableConfigSlots(getHost().getConfig(), 2, 9, 5);
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
        }
    }

    @Nullable
    public OfflineReason getOfflineReason() {
        return offlineReason;
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
