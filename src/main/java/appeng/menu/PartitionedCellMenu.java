package appeng.menu;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.core.network.AE2Packets;
import appeng.grid.SimpleGridNode.OfflineReason;
import appeng.items.contents.PartitionedCellMenuHost;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.FakeSlot;
import appeng.util.ConfigMenuInventory;

/**
 * Menu for configuring partitioned storage cells. Acts as a lightweight façade over the legacy
 * whitelist editor so the NeoForge port can hook into the same sync logic while polish work
 * continues.
 */
public class PartitionedCellMenu extends AEBaseMenu {
    private static final String ACTION_CLEAR_WHITELIST = "clearWhitelist";

    public static final MenuType<PartitionedCellMenu> TYPE = MenuTypeBuilder
            .create(PartitionedCellMenu::new, PartitionedCellMenuHost.class)
            .withInitialData(PartitionedCellMenuHost::writeInitialData,
                    (host, menu, buffer) -> host.readInitialData(buffer))
            .build("partitioned_cell");

    private final PartitionedCellMenuHost host;
    private final ConfigMenuInventory whitelistInventory;
    private int priority;
    private boolean whitelistMode;
    private int lastSyncedPriority = Integer.MIN_VALUE;
    private boolean lastSyncedWhitelistMode = true;
    private List<ResourceLocation> lastSyncedWhitelist = List.of();

    public PartitionedCellMenu(int id, Inventory playerInventory, PartitionedCellMenuHost host) {
        super(TYPE, id, playerInventory, host);
        this.host = host;
        this.whitelistInventory = host.getMenuInventory();
        this.priority = host.getPriority();
        this.whitelistMode = host.isWhitelist();

        addWhitelistSlots();
        createPlayerInventorySlots(playerInventory);

        registerClientAction(ACTION_CLEAR_WHITELIST, this::clearWhitelist);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (!isClientSide() && getPlayer() instanceof ServerPlayer serverPlayer) {
            var whitelist = List.copyOf(host.getWhitelistEntries());
            if (priority != lastSyncedPriority || whitelistMode != lastSyncedWhitelistMode
                    || !whitelist.equals(lastSyncedWhitelist)) {
                AE2Packets.sendPartitionedCellSync(serverPlayer, containerId, priority, whitelistMode, whitelist);
                lastSyncedPriority = priority;
                lastSyncedWhitelistMode = whitelistMode;
                lastSyncedWhitelist = whitelist;
            }
        }
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
        saveToItem();
        broadcastChanges();
    }

    public void updateWhitelistFromClient(List<ResourceLocation> whitelist) {
        if (!isClientSide()) {
            host.updateFromClient(whitelist);
            saveToItem();
            broadcastChanges();
        }
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (isClientSide()) {
            this.priority = priority;
            host.setPriority(priority);
            return;
        }

        if (this.priority == priority) {
            return;
        }

        this.priority = priority;
        host.acceptPriority(priority);
        saveToItem();
        broadcastChanges();
    }

    public void updatePriorityFromClient(int priority) {
        if (!isClientSide()) {
            setPriority(priority);
        }
    }

    public boolean isWhitelist() {
        return whitelistMode;
    }

    public void setWhitelist(boolean whitelistMode) {
        if (isClientSide()) {
            this.whitelistMode = whitelistMode;
            host.setWhitelist(whitelistMode);
            return;
        }

        if (this.whitelistMode == whitelistMode) {
            return;
        }

        this.whitelistMode = whitelistMode;
        host.acceptWhitelist(whitelistMode);
        saveToItem();
        broadcastChanges();
    }

    public void updateWhitelistModeFromClient(boolean whitelistMode) {
        if (!isClientSide()) {
            setWhitelist(whitelistMode);
        }
    }

    private void saveToItem() {
        if (isClientSide()) {
            return;
        }

        var stack = host.getItemStack();
        var item = host.getItem();
        item.setPriority(stack, priority);
        item.setWhitelistMode(stack, whitelistMode);
        var whitelistEntries = host.getWhitelistEntries();
        if (whitelistEntries.isEmpty()) {
            item.clearWhitelist(stack);
        } else {
            item.setWhitelist(stack, whitelistEntries);
        }

        lastSyncedWhitelist = List.of();
    }

    public OfflineReason getOfflineReason() {
        return null;
    }

    public void applySync(int priority, boolean whitelistMode, List<ResourceLocation> whitelist) {
        this.priority = priority;
        this.whitelistMode = whitelistMode;
        host.acceptPriority(priority);
        host.acceptWhitelist(whitelistMode);
        host.applyWhitelist(whitelist);
    }
}
