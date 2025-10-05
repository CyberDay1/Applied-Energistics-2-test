package appeng.menu;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.items.contents.PartitionedCellMenuHost;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;
import appeng.util.ConfigMenuInventory;

public class PartitionedCellMenu extends AEBaseMenu {
    private static final String ACTION_CLEAR_WHITELIST = "clearWhitelist";

    public static final MenuType<PartitionedCellMenu> TYPE = MenuTypeBuilder
            .create(PartitionedCellMenu::new, PartitionedCellMenuHost.class)
            .withInitialData(PartitionedCellMenuHost::writeInitialData,
                    (host, menu, buffer) -> host.readInitialData(buffer))
            .build("partitioned_cell");

    private final PartitionedCellMenuHost host;
    private final ConfigMenuInventory whitelistInventory;

    public PartitionedCellMenu(int id, Inventory playerInventory, PartitionedCellMenuHost host) {
        super(TYPE, id, playerInventory, host);
        this.host = host;
        this.whitelistInventory = host.getMenuInventory();

        addWhitelistSlots();
        createPlayerInventorySlots(playerInventory);

        registerClientAction(ACTION_CLEAR_WHITELIST, this::clearWhitelist);
    }

    private void addWhitelistSlots() {
        for (int slot = 0; slot < PartitionedCellMenuHost.FILTER_SLOT_COUNT; slot++) {
            addSlot(new FakeSlot(whitelistInventory, slot), SlotSemantics.CONFIG);
        }
    }

    public PartitionedCellMenuHost getHost() {
        return host;
    }

    public void clearWhitelist() {
        if (isClientSide()) {
            sendClientAction(ACTION_CLEAR_WHITELIST);
            return;
        }

        host.clearWhitelist();
        broadcastChanges();
    }

    public void updateWhitelistFromClient(List<ResourceLocation> whitelist) {
        if (!isClientSide()) {
            host.updateFromClient(whitelist);
            broadcastChanges();
        }
    }

    public int getPriority() {
        return host.getPriority();
    }

    public void setPriority(int priority) {
        host.setPriority(priority);
        if (!isClientSide()) {
            broadcastChanges();
        }
    }

    public void updatePriorityFromClient(int priority) {
        if (!isClientSide()) {
            host.acceptPriority(priority);
            broadcastChanges();
        }
    }
}
