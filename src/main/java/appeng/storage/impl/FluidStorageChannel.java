package appeng.storage.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import net.neoforged.neoforge.fluids.FluidStack;

import appeng.api.storage.FluidStackView;
import appeng.api.storage.IFluidStorageChannel;

public class FluidStorageChannel implements IFluidStorageChannel {
    private final StorageService service;
    private final List<FluidStack> contents = new ArrayList<>();

    FluidStorageChannel(StorageService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    @Override
    public FluidStack insert(FluidStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }

        var gridId = service.getGridId();
        if (gridId != null) {
            int accepted = StorageService.insertFluidIntoNetwork(gridId, stack.getFluid(), stack.getAmount(), simulate);
            if (accepted <= 0) {
                return stack;
            }
            if (accepted >= stack.getAmount()) {
                return FluidStack.EMPTY;
            }
            var remainder = stack.copy();
            remainder.setAmount(stack.getAmount() - accepted);
            return remainder;
        }

        return insertLocal(stack, simulate);
    }

    @Override
    public FluidStack extract(FluidStack filter, int amount, boolean simulate) {
        if (filter.isEmpty() || amount <= 0) {
            return FluidStack.EMPTY;
        }

        var gridId = service.getGridId();
        if (gridId != null) {
            int removed = StorageService.extractFluidFromNetwork(gridId, filter.getFluid(), amount, simulate);
            if (removed <= 0) {
                return FluidStack.EMPTY;
            }
            var result = filter.copy();
            result.setAmount(removed);
            return result;
        }

        return extractLocal(filter, amount, simulate);
    }

    @Override
    public Iterable<FluidStackView> getAll() {
        var gridId = service.getGridId();
        if (gridId != null) {
            return StorageService.getNetworkFluidContents(gridId);
        }

        List<FluidStackView> result = new ArrayList<>(contents.size());
        for (var stack : contents) {
            if (!stack.isEmpty()) {
                result.add(new FluidStackView(stack.getFluid(), stack.getAmount()));
            }
        }
        return result;
    }

    @Override
    public long insert(FluidStack resource, long amount, boolean simulate) {
        if (resource.isEmpty() || amount <= 0) {
            return 0;
        }

        var gridId = service.getGridId();
        if (gridId != null) {
            long inserted = 0;
            long remaining = amount;
            while (remaining > 0) {
                int attempt = (int) Math.min(Integer.MAX_VALUE, remaining);
                int accepted = StorageService.insertFluidIntoNetwork(gridId, resource.getFluid(), attempt, simulate);
                if (accepted <= 0) {
                    break;
                }
                inserted += accepted;
                remaining -= accepted;
                if (accepted < attempt) {
                    break;
                }
            }
            return inserted;
        }

        return insertLocal(resource, amount, simulate);
    }

    @Override
    public long extract(FluidStack resource, long amount, boolean simulate) {
        if (resource.isEmpty() || amount <= 0) {
            return 0;
        }

        var gridId = service.getGridId();
        if (gridId != null) {
            long extracted = 0;
            long remaining = amount;
            while (remaining > 0) {
                int attempt = (int) Math.min(Integer.MAX_VALUE, remaining);
                int removed = StorageService.extractFluidFromNetwork(gridId, resource.getFluid(), attempt, simulate);
                if (removed <= 0) {
                    break;
                }
                extracted += removed;
                remaining -= removed;
                if (removed < attempt) {
                    break;
                }
            }
            return extracted;
        }

        return extractLocal(resource, amount, simulate);
    }

    @Override
    public long getStoredAmount(FluidStack resource) {
        if (resource.isEmpty()) {
            return 0;
        }

        var gridId = service.getGridId();
        if (gridId != null) {
            return StorageService.getNetworkStoredFluid(gridId, resource.getFluid());
        }

        long total = 0;
        for (var stack : contents) {
            if (stack.isFluidEqual(resource)) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (var stack : contents) {
            CompoundTag stackTag = new CompoundTag();
            stack.writeToNBT(stackTag);
            list.add(stackTag);
        }
        tag.put("Fluids", list);
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        contents.clear();
        if (tag.contains("Fluids", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Fluids", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag stackTag = list.getCompound(i);
                contents.add(FluidStack.loadFluidStackFromNBT(stackTag));
            }
        }
    }

    private FluidStack insertLocal(FluidStack stack, boolean simulate) {
        var existing = findMatching(stack);
        if (!simulate) {
            if (existing != null) {
                existing.setAmount(existing.getAmount() + stack.getAmount());
            } else {
                contents.add(stack.copy());
            }
        }
        return FluidStack.EMPTY;
    }

    private long insertLocal(FluidStack stack, long amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        if (!simulate) {
            var existing = findMatching(stack);
            if (existing != null) {
                existing.setAmount(existing.getAmount() + (int) amount);
            } else {
                var copy = stack.copy();
                copy.setAmount((int) amount);
                contents.add(copy);
            }
        }
        return amount;
    }

    private FluidStack extractLocal(FluidStack filter, int amount, boolean simulate) {
        var existing = findMatching(filter);
        if (existing == null) {
            return FluidStack.EMPTY;
        }

        int available = existing.getAmount();
        int toExtract = Math.min(available, amount);
        if (toExtract <= 0) {
            return FluidStack.EMPTY;
        }

        var result = filter.copy();
        result.setAmount(toExtract);

        if (!simulate) {
            existing.setAmount(available - toExtract);
            if (existing.getAmount() <= 0) {
                contents.remove(existing);
            }
        }

        return result;
    }

    private long extractLocal(FluidStack filter, long amount, boolean simulate) {
        var existing = findMatching(filter);
        if (existing == null || amount <= 0) {
            return 0;
        }

        int available = existing.getAmount();
        int toExtract = (int) Math.min(available, amount);
        if (toExtract <= 0) {
            return 0;
        }

        if (!simulate) {
            existing.setAmount(available - toExtract);
            if (existing.getAmount() <= 0) {
                contents.remove(existing);
            }
        }

        return toExtract;
    }

    private FluidStack findMatching(FluidStack stack) {
        for (var existing : contents) {
            if (existing.isFluidEqual(stack)) {
                return existing;
            }
        }
        return null;
    }
}
