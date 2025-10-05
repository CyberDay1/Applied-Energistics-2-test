package appeng.storage.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import appeng.blockentity.simple.DriveBlockEntity;
import appeng.core.AELog;
import appeng.items.storage.BasicCell1kItem;
import appeng.api.storage.ItemStackView;
import appeng.util.GridHelper;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

final class NetworkItemStorage {
    private final UUID gridId;
    private final Set<DriveBlockEntity> drives = new LinkedHashSet<>();
    private long totalCapacity;
    private long totalUsed;

    NetworkItemStorage(UUID gridId) {
        this.gridId = gridId;
    }

    synchronized void mountDrive(DriveBlockEntity drive) {
        drives.add(drive);
        recalcTotals();
        AELog.debug("Mounted drive %s on grid %s capacity=%s", drive.getBlockPos(), gridId, totalCapacity);
    }

    synchronized void unmountDrive(DriveBlockEntity drive) {
        if (drives.remove(drive)) {
            recalcTotals();
            AELog.debug("Unmounted drive %s on grid %s capacity=%s", drive.getBlockPos(), gridId, totalCapacity);
        }
    }

    synchronized void refreshDrive(DriveBlockEntity drive) {
        if (drives.contains(drive)) {
            recalcTotals();
            AELog.debug("Refreshed drive %s on grid %s capacity=%s", drive.getBlockPos(), gridId, totalCapacity);
        }
    }

    synchronized boolean isEmpty() {
        return drives.isEmpty();
    }

    synchronized StorageService.StorageTotals getTotals() {
        return new StorageService.StorageTotals(totalCapacity, totalUsed);
    }

    synchronized int insert(Item item, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int remaining = amount;
        for (var cell : cells()) {
            int inserted = cell.item().insert(cell.stack(), item, remaining, simulate);
            remaining -= inserted;
            if (remaining <= 0) {
                break;
            }
        }
        int accepted = amount - remaining;
        if (!simulate && accepted > 0) {
            recalcTotals();
            GridHelper.updateSetMetadata(gridId);
        }
        return accepted;
    }

    synchronized int extract(Item item, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int remaining = amount;
        for (var cell : cells()) {
            int extracted = cell.item().extract(cell.stack(), item, remaining, simulate);
            remaining -= extracted;
            if (remaining <= 0) {
                break;
            }
        }
        int removed = amount - remaining;
        if (!simulate && removed > 0) {
            recalcTotals();
            GridHelper.updateSetMetadata(gridId);
        }
        return removed;
    }

    synchronized boolean contains(Item item) {
        for (var cell : cells()) {
            if (cell.item().contains(cell.stack(), item)) {
                return true;
            }
        }
        return false;
    }

    synchronized long getStoredAmount(Item item) {
        long total = 0;
        for (var cell : cells()) {
            total += cell.item().getItemCount(cell.stack(), item);
        }
        return total;
    }

    synchronized List<ItemStackView> getAll() {
        List<ItemStackView> result = new ArrayList<>();
        for (var cell : cells()) {
            result.addAll(cell.item().getAll(cell.stack()));
        }
        return result;
    }

    private List<Cell> cells() {
        List<Cell> result = new ArrayList<>();
        for (var drive : drives) {
            int slots = drive.getCellSlotCount();
            for (int i = 0; i < slots; i++) {
                ItemStack stack = drive.getCellInSlot(i);
                if (stack.isEmpty() || !(stack.getItem() instanceof BasicCell1kItem cellItem)) {
                    continue;
                }
                result.add(new Cell(cellItem, stack));
            }
        }
        return result;
    }

    private void recalcTotals() {
        long capacity = 0;
        long used = 0;
        for (var cell : cells()) {
            capacity += cell.item().getBytes(cell.stack());
            used += cell.item().getTotalStored(cell.stack());
        }
        this.totalCapacity = capacity;
        this.totalUsed = used;
    }

    private record Cell(BasicCell1kItem item, ItemStack stack) {
    }
}
