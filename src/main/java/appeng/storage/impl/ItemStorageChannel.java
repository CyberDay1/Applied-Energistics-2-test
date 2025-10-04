package appeng.storage.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import appeng.api.storage.IItemStorageChannel;
import net.minecraft.world.item.ItemStack;

public class ItemStorageChannel implements IItemStorageChannel {
    private final List<ItemStack> contents = new ArrayList<>();

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return stack;
        }

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

    @Override
    public ItemStack extract(ItemStack filter, int amount, boolean simulate) {
        if (filter.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }

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

    @Override
    public boolean contains(ItemStack filter) {
        if (filter.isEmpty()) {
            return false;
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
            ItemStack leftover = insert(attempt, simulate);
            int after = leftover.isEmpty() ? 0 : leftover.getCount();
            inserted += before - after;
            if (after > 0) {
                break;
            }
            remaining -= before;
        }

        return inserted;
    }

    @Override
    public long extract(ItemStack resource, long amount, boolean simulate) {
        if (resource.isEmpty() || amount <= 0) {
            return 0;
        }

        long extracted = 0;
        long remaining = amount;
        while (remaining > 0) {
            int attemptAmount = (int) Math.min(remaining, resource.getMaxStackSize());
            if (attemptAmount <= 0) {
                break;
            }

            ItemStack result = extract(resource, attemptAmount, simulate);
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

    @Override
    public long getStoredAmount(ItemStack resource) {
        if (resource.isEmpty()) {
            return 0;
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
    public Collection<ItemStack> getAll() {
        List<ItemStack> copy = new ArrayList<>(contents.size());
        for (var stack : contents) {
            copy.add(stack.copy());
        }
        return Collections.unmodifiableList(copy);
    }
}
