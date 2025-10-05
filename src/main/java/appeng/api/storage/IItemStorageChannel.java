package appeng.api.storage;

import net.minecraft.world.item.ItemStack;

import appeng.api.storage.ItemStackView;

public interface IItemStorageChannel extends IStorageChannel<ItemStack> {
    ItemStack insert(ItemStack stack, boolean simulate);

    ItemStack extract(ItemStack filter, int amount, boolean simulate);

    boolean contains(ItemStack filter);

    Iterable<ItemStackView> getAll();
}
