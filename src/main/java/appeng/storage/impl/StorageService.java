package appeng.storage.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.storage.FluidStackView;
import appeng.api.storage.IFluidStorageChannel;
import appeng.api.storage.IItemStorageChannel;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageService;
import appeng.api.storage.ItemStackView;
import appeng.blockentity.simple.DriveBlockEntity;
import appeng.core.AELog;

public class StorageService implements IStorageService {
    private static final Map<UUID, NetworkStorageData> NETWORKS = new ConcurrentHashMap<>();

    private final Map<Class<?>, IStorageChannel<?>> channels = new HashMap<>();
    private final ItemStorageChannel itemChannel;
    private final FluidStorageChannel fluidChannel;
    private UUID gridId;

    public StorageService() {
        this.itemChannel = new ItemStorageChannel(this);
        this.fluidChannel = new FluidStorageChannel(this);
        channels.put(ItemStack.class, itemChannel);
        channels.put(FluidStack.class, fluidChannel);
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

    public IFluidStorageChannel getFluidChannel() {
        return fluidChannel;
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        if (getItemChannel() instanceof ItemStorageChannel impl) {
            tag.put("ItemChannel", impl.saveNBT());
        }
        if (getFluidChannel() instanceof FluidStorageChannel impl) {
            tag.put("FluidChannel", impl.saveNBT());
        }
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        if (tag.contains("ItemChannel")) {
            if (getItemChannel() instanceof ItemStorageChannel impl) {
                impl.loadNBT(tag.getCompound("ItemChannel"));
            }
        }
        if (tag.contains("FluidChannel")) {
            if (getFluidChannel() instanceof FluidStorageChannel impl) {
                impl.loadNBT(tag.getCompound("FluidChannel"));
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
        if (!isItemAllowedByWhitelist(gridId, item)) {
            logPartitionedInsert(gridId, item, simulate);
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

    static boolean hasItemWhitelist(UUID gridId) {
        if (gridId == null) {
            return false;
        }
        var network = NETWORKS.get(gridId);
        if (network == null) {
            return false;
        }
        return network.getItemWhitelistSummary().restricted();
    }

    static Set<ResourceLocation> getItemWhitelist(UUID gridId) {
        if (gridId == null) {
            return Set.of();
        }
        var network = NETWORKS.get(gridId);
        if (network == null) {
            return Set.of();
        }
        return network.getItemWhitelistSummary().allowedItems();
    }

    static boolean isItemAllowedByWhitelist(UUID gridId, Item item) {
        if (gridId == null) {
            return true;
        }
        var network = NETWORKS.get(gridId);
        if (network == null) {
            return true;
        }
        var summary = network.getItemWhitelistSummary();
        if (!summary.restricted()) {
            return true;
        }
        var itemId = BuiltInRegistries.ITEM.getKey(item);
        return summary.allowedItems().contains(itemId);
    }

    static void logPartitionedInsert(UUID gridId, Item item, boolean simulate) {
        if (gridId == null) {
            return;
        }
        var itemId = BuiltInRegistries.ITEM.getKey(item);
        var whitelist = getItemWhitelist(gridId);
        AELog.debug("Rejected %s insert of %s on grid %s due to whitelist %s",
                simulate ? "simulated" : "real",
                itemId,
                gridId,
                whitelist);
    }

    static int insertFluidIntoNetwork(UUID gridId, Fluid fluid, int amount, boolean simulate) {
        if (gridId == null || amount <= 0) {
            return 0;
        }
        return getOrCreateNetwork(gridId).insertFluid(fluid, amount, simulate);
    }

    static int extractFluidFromNetwork(UUID gridId, Fluid fluid, int amount, boolean simulate) {
        if (gridId == null || amount <= 0) {
            return 0;
        }
        var network = NETWORKS.get(gridId);
        if (network == null) {
            return 0;
        }
        return network.extractFluid(fluid, amount, simulate);
    }

    static boolean networkContainsFluid(UUID gridId, Fluid fluid) {
        if (gridId == null) {
            return false;
        }
        var network = NETWORKS.get(gridId);
        return network != null && network.containsFluid(fluid);
    }

    static long getNetworkStoredFluid(UUID gridId, Fluid fluid) {
        if (gridId == null) {
            return 0;
        }
        var network = NETWORKS.get(gridId);
        if (network == null) {
            return 0;
        }
        return network.getStoredFluidAmount(fluid);
    }

    static List<FluidStackView> getNetworkFluidContents(UUID gridId) {
        if (gridId == null) {
            return List.of();
        }
        var network = NETWORKS.get(gridId);
        if (network == null) {
            return List.of();
        }
        return network.getAllFluids();
    }

    private static NetworkStorageData getOrCreateNetwork(UUID gridId) {
        return NETWORKS.computeIfAbsent(gridId, NetworkStorageData::new);
    }

    static record StorageTotals(long totalCapacity, long used) {
    }

    private static final class NetworkStorageData {
        private final NetworkItemStorage itemStorage;
        private final NetworkFluidStorage fluidStorage;

        private NetworkStorageData(UUID gridId) {
            this.itemStorage = new NetworkItemStorage(gridId);
            this.fluidStorage = new NetworkFluidStorage(gridId);
        }

        void mountDrive(DriveBlockEntity drive) {
            itemStorage.mountDrive(drive);
            fluidStorage.mountDrive(drive);
        }

        void refreshDrive(DriveBlockEntity drive) {
            itemStorage.refreshDrive(drive);
            fluidStorage.refreshDrive(drive);
        }

        void unmountDrive(DriveBlockEntity drive) {
            itemStorage.unmountDrive(drive);
            fluidStorage.unmountDrive(drive);
        }

        boolean isEmpty() {
            return itemStorage.isEmpty() && fluidStorage.isEmpty();
        }

        StorageTotals getTotals() {
            var itemTotals = itemStorage.getTotals();
            var fluidTotals = fluidStorage.getTotals();
            return new StorageTotals(itemTotals.totalCapacity() + fluidTotals.totalCapacity(),
                    itemTotals.used() + fluidTotals.used());
        }

        int insert(Item item, int amount, boolean simulate) {
            return itemStorage.insert(item, amount, simulate);
        }

        int extract(Item item, int amount, boolean simulate) {
            return itemStorage.extract(item, amount, simulate);
        }

        boolean contains(Item item) {
            return itemStorage.contains(item);
        }

        long getStoredAmount(Item item) {
            return itemStorage.getStoredAmount(item);
        }

        List<ItemStackView> getAll() {
            return itemStorage.getAll();
        }

        NetworkItemStorage.ItemWhitelistSummary getItemWhitelistSummary() {
            return itemStorage.getWhitelistSummary();
        }

        int insertFluid(Fluid fluid, int amount, boolean simulate) {
            return fluidStorage.insert(fluid, amount, simulate);
        }

        int extractFluid(Fluid fluid, int amount, boolean simulate) {
            return fluidStorage.extract(fluid, amount, simulate);
        }

        boolean containsFluid(Fluid fluid) {
            return fluidStorage.contains(fluid);
        }

        long getStoredFluidAmount(Fluid fluid) {
            return fluidStorage.getStoredAmount(fluid);
        }

        List<FluidStackView> getAllFluids() {
            return fluidStorage.getAll();
        }
    }
}
