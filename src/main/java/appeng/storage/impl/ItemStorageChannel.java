package appeng.storage.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import appeng.api.storage.IItemStorageChannel;
import appeng.api.storage.ItemStackView;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemStorageChannel implements IItemStorageChannel {
    private final StorageService service;
    private final List<ItemStack> contents = new ArrayList<>();

    ItemStorageChannel(StorageService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }

        var gridId = service.getGridId();
        if (gridId != null) {
            int accepted = StorageService.insertIntoNetwork(gridId, stack.getItem(), stack.getCount(), simulate);
            stack.shrink(accepted);
            return stack.isEmpty() ? ItemStack.EMPTY : stack;
        }

        return insertLocal(stack, simulate);
    }

    @Override
    public ItemStack extract(ItemStack filter, int amount, boolean simulate) {
        if (filter.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        var gridId = service.getGridId();
        if (gridId != null) {
            int removed = StorageService.extractFromNetwork(gridId, filter.getItem(), amount, simulate);
            if (removed <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack result = filter.copy();
            result.setCount(removed);
            return result;
        }

        return extractLocal(filter, amount, simulate);
    }

    @Override
    public boolean contains(ItemStack filter) {
        if (filter.isEmpty()) {
            return false;
        }

        var gridId = service.getGridId();
        if (gridId != null) {
            return StorageService.networkContains(gridId, filter.getItem());
        }

        for (var stack : contents) {
            if (ItemStack.isSameItemSameComponents(stack, filter)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long insert(ItemStack resource, long amount, boolean simulate) {
        if (resource.isEmpty() || amount <= 0) {
            return 0;
        }

        var gridId = service.getGridId();
        if (gridId != null) {
            long inserted = 0;
            long remaining = amount;
            while (remaining > 0) {
                int attempt = (int) Math.min(Integer.MAX_VALUE, remaining);
                int accepted = StorageService.insertIntoNetwork(gridId, resource.getItem(), attempt, simulate);
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
    public long extract(ItemStack resource, long amount, boolean simulate) {
        if (resource.isEmpty() || amount <= 0) {
            return 0;
        }

        var gridId = service.getGridId();
        if (gridId != null) {
            long extracted = 0;
            long remaining = amount;
            while (remaining > 0) {
                int attempt = (int) Math.min(Integer.MAX_VALUE, remaining);
                int removed = StorageService.extractFromNetwork(gridId, resource.getItem(), attempt, simulate);
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
    public long getStoredAmount(ItemStack resource) {
        if (resource.isEmpty()) {
            return 0;
        }

        var gridId = service.getGridId();
        if (gridId != null) {
            return StorageService.getNetworkStored(gridId, resource.getItem());
        }

        long total = 0;
        for (var stack : contents) {
            if (ItemStack.isSameItemSameComponents(stack, resource)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    @Override
    public Iterable<ItemStackView> getAll() {
        var gridId = service.getGridId();
        if (gridId != null) {
            return StorageService.getNetworkContents(gridId);
        }

        List<ItemStackView> result = new ArrayList<>(contents.size());
        for (var stack : contents) {
            if (!stack.isEmpty()) {
                result.add(new ItemStackView(stack.getItem(), stack.getCount()));
            }
        }
        return result;
    }

    public CompoundTag saveNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (ItemStack stack : contents) {
            CompoundTag stackTag = new CompoundTag();
            stack.save(stackTag);
            list.add(stackTag);
        }
        tag.put("Items", list);
        return tag;
    }

    public void loadNBT(CompoundTag tag) {
        contents.clear();
        if (tag.contains("Items", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag stackTag = list.getCompound(i);
                contents.add(ItemStack.of(stackTag));
            }
        }
    }

    private ItemStack insertLocal(ItemStack stack, boolean simulate) {
        for (int i = 0; i < contents.size(); i++) {
            var existing = contents.get(i);
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int space = existing.getMaxStackSize() - existing.getCount();
                if (space <= 0) {
                    continue;
                }

                int toInsert = Math.min(space, stack.getCount());
                if (toInsert > 0 && !simulate) {
                    existing.grow(toInsert);
                }
                stack.shrink(toInsert);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (!stack.isEmpty() && !simulate) {
            contents.add(stack.copy());
            return ItemStack.EMPTY;
        }

        return stack;
    }

    private long insertLocal(ItemStack resource, long amount, boolean simulate) {
        long inserted = 0;
        long remaining = amount;
        while (remaining > 0) {
            int attemptCount = (int) Math.min(remaining, resource.getMaxStackSize());
            if (attemptCount <= 0) {
                break;
            }

            ItemStack attempt = resource.copy();
            attempt.setCount(attemptCount);
            int before = attempt.getCount();
            ItemStack leftover = insertLocal(attempt, simulate);
            int after = leftover.isEmpty() ? 0 : leftover.getCount();
            inserted += before - after;
            if (after > 0) {
                break;
            }
            remaining -= before;
        }

        return inserted;
    }

    private ItemStack extractLocal(ItemStack filter, int amount, boolean simulate) {
        Iterator<ItemStack> iterator = contents.iterator();
        while (iterator.hasNext()) {
            var existing = iterator.next();
            if (ItemStack.isSameItemSameComponents(existing, filter)) {
                int toExtract = Math.min(existing.getCount(), amount);
                if (toExtract <= 0) {
                    return ItemStack.EMPTY;
                }

                ItemStack result = existing.copy();
                result.setCount(toExtract);

                if (!simulate) {
                    existing.shrink(toExtract);
                    if (existing.isEmpty()) {
                        iterator.remove();
                    }
                }

                return result;
            }
        }

        return ItemStack.EMPTY;
    }

    private long extractLocal(ItemStack resource, long amount, boolean simulate) {
        long extracted = 0;
        long remaining = amount;
        while (remaining > 0) {
            int attemptAmount = (int) Math.min(remaining, resource.getMaxStackSize());
            if (attemptAmount <= 0) {
                break;
            }

            ItemStack result = extractLocal(resource, attemptAmount, simulate);
            if (result.isEmpty()) {
                break;
            }

            extracted += result.getCount();
            remaining -= result.getCount();
            if (result.getCount() < attemptAmount) {
                break;
            }
        }

        return extracted;
    }
}
