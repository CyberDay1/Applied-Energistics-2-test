package appeng.storage.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.IItemStorageChannel;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageService;
import appeng.api.storage.ItemStackView;
import appeng.blockentity.simple.DriveBlockEntity;

public class StorageService implements IStorageService {
    private static final Map<UUID, NetworkItemStorage> NETWORKS = new ConcurrentHashMap<>();

    private final Map<Class<?>, IStorageChannel<?>> channels = new HashMap<>();
    private final ItemStorageChannel itemChannel;
    private UUID gridId;

    public StorageService() {
        this.itemChannel = new ItemStorageChannel(this);
        channels.put(ItemStack.class, itemChannel);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> IStorageChannel<T> getChannel(Class<T> type) {
        return (IStorageChannel<T>) channels.get(type);
    }

    @Override
    public List<IStorageChannel<?>> getAllChannels() {
        return List.copyOf(channels.values());
    }

    public IItemStorageChannel getItemChannel() {
        return itemChannel;
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        if (getItemChannel() instanceof ItemStorageChannel impl) {
            tag.put("ItemChannel", impl.saveNBT());
        }
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        if (tag.contains("ItemChannel")) {
            if (getItemChannel() instanceof ItemStorageChannel impl) {
                impl.loadNBT(tag.getCompound("ItemChannel"));
            }
        }
    }

    void attachToGrid(UUID gridId) {
        if (!Objects.equals(this.gridId, gridId)) {
            this.gridId = gridId;
        }
    }

    void detachFromGrid(UUID gridId) {
        if (Objects.equals(this.gridId, gridId)) {
            this.gridId = null;
        }
    }

    UUID getGridId() {
        return gridId;
    }

    static void mountDrive(UUID gridId, DriveBlockEntity drive) {
        if (gridId == null) {
            return;
        }
        getOrCreateNetwork(gridId).mountDrive(drive);
    }

    static void refreshDrive(UUID gridId, DriveBlockEntity drive) {
        if (gridId == null) {
            return;
        }
        var network = NETWORKS.get(gridId);
        if (network != null) {
            network.refreshDrive(drive);
        }
    }

    static void unmountDrive(UUID gridId, DriveBlockEntity drive) {
        if (gridId == null) {
            return;
        }
        var network = NETWORKS.get(gridId);
        if (network != null) {
            network.unmountDrive(drive);
            if (network.isEmpty()) {
                NETWORKS.remove(gridId);
            }
        }
    }

    static StorageTotals getTotals(UUID gridId) {
        if (gridId == null) {
            return new StorageTotals(0, 0);
        }
        var network = NETWORKS.get(gridId);
        if (network == null) {
            return new StorageTotals(0, 0);
        }
        return network.getTotals();
    }

    static int insertIntoNetwork(UUID gridId, Item item, int amount, boolean simulate) {
        if (gridId == null || amount <= 0) {
            return 0;
        }
        return getOrCreateNetwork(gridId).insert(item, amount, simulate);
    }

    static int extractFromNetwork(UUID gridId, Item item, int amount, boolean simulate) {
        if (gridId == null || amount <= 0) {
            return 0;
        }
        var network = NETWORKS.get(gridId);
        if (network == null) {
            return 0;
        }
        return network.extract(item, amount, simulate);
    }

    static boolean networkContains(UUID gridId, Item item) {
        if (gridId == null) {
            return false;
        }
        var network = NETWORKS.get(gridId);
        return network != null && network.contains(item);
    }

    static long getNetworkStored(UUID gridId, Item item) {
        if (gridId == null) {
            return 0;
        }
        var network = NETWORKS.get(gridId);
        if (network == null) {
            return 0;
        }
        return network.getStoredAmount(item);
    }

    static List<ItemStackView> getNetworkContents(UUID gridId) {
        if (gridId == null) {
            return List.of();
        }
        var network = NETWORKS.get(gridId);
        if (network == null) {
            return List.of();
        }
        return network.getAll();
    }

    private static NetworkItemStorage getOrCreateNetwork(UUID gridId) {
        return NETWORKS.computeIfAbsent(gridId, NetworkItemStorage::new);
    }

    static record StorageTotals(long totalCapacity, long used) {
    }
}
