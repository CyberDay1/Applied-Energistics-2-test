package appeng.menu.implementations;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.blockentity.storage.StorageBusBlockEntity;
import appeng.grid.SimpleGridNode.OfflineReason;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.FakeSlot;

public class StorageBusBlockMenu extends UpgradeableMenu<StorageBusBlockEntity> {

    public static final MenuType<StorageBusBlockMenu> TYPE = MenuTypeBuilder
            .create(StorageBusBlockMenu::new, StorageBusBlockEntity.class)
            .build("storage_bus_block");

    @GuiSync(11)
    @Nullable
    public OfflineReason offlineReason;

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
        var wrapper = getHost().getConfig().createMenuWrapper();
        for (int i = 0; i < wrapper.size(); i++) {
            addSlot(new FakeSlot(wrapper, i), SlotSemantics.CONFIG);
        }
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

        return null;
    }

}
