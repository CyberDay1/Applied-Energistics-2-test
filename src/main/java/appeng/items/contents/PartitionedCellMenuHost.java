package appeng.items.contents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import net.neoforged.neoforge.network.PacketDistributor;

import appeng.api.config.Actionable;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.items.storage.PartitionedCellItem;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.util.ConfigInventory;
import appeng.util.ConfigMenuInventory;
import appeng.core.network.serverbound.UpdatePartitionedCellPriorityPacket;
import appeng.core.network.serverbound.UpdatePartitionedCellWhitelistPacket;

/**
 * Handles syncing the whitelist configuration inventory for partitioned storage cells.
 */
public class PartitionedCellMenuHost extends ItemMenuHost<PartitionedCellItem> {
    public static final int FILTER_SLOT_COUNT = 27;

    private final ConfigInventory whitelistInventory;
    private boolean syncing;
    private int priority;

    public PartitionedCellMenuHost(PartitionedCellItem item, Player player, ItemMenuHostLocator locator) {
        super(item, player, locator);

        this.whitelistInventory = ConfigInventory.configTypes(FILTER_SLOT_COUNT)
                .supportedType(AEKeyType.items())
                .changeListener(this::onWhitelistChanged)
                .build();

        this.priority = item.getPriority(getItemStack());

        if (!isClientSide()) {
            applyWhitelist(getItem().getWhitelist(getItemStack()));
        }
    }

    public ConfigInventory getWhitelistInventory() {
        return whitelistInventory;
    }

    public ConfigMenuInventory getMenuInventory() {
        return whitelistInventory.createMenuWrapper();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (this.priority == priority) {
            return;
        }

        setPriorityInternal(priority);

        if (isClientSide()) {
            PacketDistributor.sendToServer(new UpdatePartitionedCellPriorityPacket(priority));
        }
    }

    public void acceptPriority(int priority) {
        setPriorityInternal(priority);
    }

    private void setPriorityInternal(int priority) {
        if (this.priority == priority) {
            return;
        }

        this.priority = priority;

        if (!isClientSide()) {
            getItem().setPriority(getItemStack(), priority);
        }
    }

    public void writeInitialData(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(priority);
        var whitelist = getWhitelistEntries();
        buffer.writeVarInt(whitelist.size());
        for (var id : whitelist) {
            buffer.writeResourceLocation(id);
        }
    }

    public void readInitialData(RegistryFriendlyByteBuf buffer) {
        setPriorityInternal(buffer.readVarInt());
        int size = buffer.readVarInt();
        List<ResourceLocation> whitelist = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            whitelist.add(buffer.readResourceLocation());
        }
        applyWhitelist(whitelist);
    }

    public void applyWhitelist(Collection<ResourceLocation> whitelist) {
        syncing = true;
        whitelistInventory.beginBatch();
        try {
            whitelistInventory.clear();
            int slot = 0;
            for (var id : whitelist) {
                if (slot >= whitelistInventory.size()) {
                    break;
                }
                Item item = BuiltInRegistries.ITEM.get(id);
                if (item == Items.AIR) {
                    continue;
                }
                var key = AEItemKey.of(item);
                whitelistInventory.setStack(slot++, new GenericStack(key, 1));
            }
        } finally {
            whitelistInventory.endBatch();
            syncing = false;
        }

        if (!isClientSide()) {
            saveWhitelistToItem();
        }
    }

    public void clearWhitelist() {
        syncing = true;
        try {
            whitelistInventory.clear();
        } finally {
            syncing = false;
        }

        if (isClientSide()) {
            PacketDistributor.sendToServer(new UpdatePartitionedCellWhitelistPacket(List.of()));
        } else {
            getItem().clearWhitelist(getItemStack());
        }
    }

    public void updateFromClient(Collection<ResourceLocation> whitelist) {
        if (!isClientSide()) {
            applyWhitelist(whitelist);
        }
    }

    private void onWhitelistChanged() {
        if (syncing) {
            return;
        }

        if (isClientSide()) {
            PacketDistributor.sendToServer(new UpdatePartitionedCellWhitelistPacket(getWhitelistEntries()));
        } else {
            saveWhitelistToItem();
        }
    }

    private void saveWhitelistToItem() {
        var stack = getItemStack();
        var whitelist = getWhitelistEntries();
        if (whitelist.isEmpty()) {
            getItem().clearWhitelist(stack);
        } else {
            getItem().setWhitelist(stack, whitelist);
        }
    }

    private List<ResourceLocation> getWhitelistEntries() {
        List<ResourceLocation> whitelist = new ArrayList<>();
        for (int i = 0; i < whitelistInventory.size(); i++) {
            AEKey key = whitelistInventory.getKey(i);
            if (key instanceof AEItemKey itemKey) {
                whitelist.add(itemKey.getId());
            }
        }
        return whitelist;
    }

    @Override
    public long insert(Player player, AEKey what, long amount, Actionable mode) {
        return 0;
    }
}
