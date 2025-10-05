package appeng.storage.impl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import appeng.api.storage.FluidStackView;
import appeng.blockentity.simple.DriveBlockEntity;
import appeng.core.AELog;
import appeng.items.storage.fluid.BasicFluidCellItem;
import appeng.util.GridHelper;

final class NetworkFluidStorage {
    private final UUID gridId;
    private final Set<DriveBlockEntity> drives = new LinkedHashSet<>();
    private long totalCapacity;
    private long totalUsed;

    NetworkFluidStorage(UUID gridId) {
        this.gridId = gridId;
    }

    synchronized void mountDrive(DriveBlockEntity drive) {
        drives.add(drive);
        int totalCells = recalcTotals();
        AELog.debug("Mounted fluid drive %s on grid %s cells=%s capacity=%s", drive.getBlockPos(), gridId, totalCells,
                totalCapacity);
    }

    synchronized void unmountDrive(DriveBlockEntity drive) {
        if (drives.remove(drive)) {
            int totalCells = recalcTotals();
            AELog.debug("Unmounted fluid drive %s on grid %s cells=%s capacity=%s", drive.getBlockPos(), gridId,
                    totalCells, totalCapacity);
        }
    }

    synchronized void refreshDrive(DriveBlockEntity drive) {
        if (drives.contains(drive)) {
            int totalCells = recalcTotals();
            AELog.debug("Refreshed fluid drive %s on grid %s cells=%s capacity=%s", drive.getBlockPos(), gridId,
                    totalCells, totalCapacity);
        }
    }

    synchronized boolean isEmpty() {
        return drives.isEmpty();
    }

    synchronized StorageService.StorageTotals getTotals() {
        return new StorageService.StorageTotals(totalCapacity, totalUsed);
    }

    synchronized int insert(Fluid fluid, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int remaining = amount;
        for (var cell : cells()) {
            int inserted = cell.item().insert(cell.stack(), fluid, remaining, simulate);
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

    synchronized int extract(Fluid fluid, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int remaining = amount;
        for (var cell : cells()) {
            int extracted = cell.item().extract(cell.stack(), fluid, remaining, simulate);
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

    synchronized boolean contains(Fluid fluid) {
        for (var cell : cells()) {
            if (cell.item().contains(cell.stack(), fluid)) {
                return true;
            }
        }
        return false;
    }

    synchronized long getStoredAmount(Fluid fluid) {
        long total = 0;
        for (var cell : cells()) {
            total += cell.item().getFluidAmount(cell.stack(), fluid);
        }
        return total;
    }

    synchronized List<FluidStackView> getAll() {
        List<FluidStackView> result = new ArrayList<>();
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
                if (stack.isEmpty()) {
                    continue;
                }
                if (stack.getItem() instanceof BasicFluidCellItem cellItem) {
                    result.add(new Cell(cellItem, stack));
                }
            }
        }
        return result;
    }

    private int recalcTotals() {
        var cells = cells();
        long capacity = 0;
        long used = 0;
        for (var cell : cells) {
            capacity += cell.item().getBytes(cell.stack());
            used += cell.item().getTotalStored(cell.stack());
        }
        this.totalCapacity = capacity;
        this.totalUsed = used;
        return cells.size();
    }

    private record Cell(BasicFluidCellItem item, ItemStack stack) {
    }
}
