package appeng.items.storage;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.api.storage.ItemStackView;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.stacks.AEKeyType;
import appeng.items.AEBaseItem;

/**
 * Shared base implementation for AE's basic item storage cells.
 */
public class BasicCellItem extends AEBaseItem implements IBasicCellItem {
    private static final String CELL_TAG = "CellData";
    private static final String ITEMS_TAG = "Items";

    private final int capacity;

    public BasicCellItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return capacity;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return 1;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 63;
    }

    @Override
    public double getIdleDrain() {
        return 1.0;
    }

    public int getRemainingCapacity(ItemStack cell) {
        return capacity - getTotalStored(cell);
    }

    public int getTotalStored(ItemStack cell) {
        var itemsTag = getItemsTag(cell, false);
        if (itemsTag == null) {
            return 0;
        }
        int total = 0;
        for (var key : itemsTag.getAllKeys()) {
            total += Math.max(0, itemsTag.getInt(key));
        }
        return total;
    }

    public boolean contains(ItemStack cell, Item item) {
        return getItemCount(cell, item) > 0;
    }

    public int getItemCount(ItemStack cell, Item item) {
        var itemsTag = getItemsTag(cell, false);
        if (itemsTag == null) {
            return 0;
        }
        var id = key(item);
        return Math.max(0, itemsTag.getInt(id));
    }

    public int insert(ItemStack cell, Item item, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int space = getRemainingCapacity(cell);
        if (space <= 0) {
            return 0;
        }
        int toInsert = Math.min(space, amount);
        if (toInsert <= 0) {
            return 0;
        }
        if (!simulate) {
            var itemsTag = getItemsTag(cell, true);
            var id = key(item);
            int current = Math.max(0, itemsTag.getInt(id));
            itemsTag.putInt(id, current + toInsert);
        }
        return toInsert;
    }

    public int extract(ItemStack cell, Item item, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        var itemsTag = getItemsTag(cell, false);
        if (itemsTag == null) {
            return 0;
        }
        var id = key(item);
        int current = Math.max(0, itemsTag.getInt(id));
        if (current <= 0) {
            return 0;
        }
        int toExtract = Math.min(current, amount);
        if (toExtract <= 0) {
            return 0;
        }
        if (!simulate) {
            if (current == toExtract) {
                itemsTag.remove(id);
            } else {
                itemsTag.putInt(id, current - toExtract);
            }
            cleanup(cell, itemsTag);
        }
        return toExtract;
    }

    public List<ItemStackView> getAll(ItemStack cell) {
        var result = new ArrayList<ItemStackView>();
        var itemsTag = getItemsTag(cell, false);
        if (itemsTag == null) {
            return result;
        }
        for (var key : itemsTag.getAllKeys()) {
            int amount = Math.max(0, itemsTag.getInt(key));
            if (amount <= 0) {
                continue;
            }
            var item = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(key)).orElse(null);
            if (item == null || item == Items.AIR) {
                continue;
            }
            result.add(new ItemStackView(item, amount));
        }
        return result;
    }

    private static String key(Item item) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        return id.toString();
    }

    private static void cleanup(ItemStack cell, CompoundTag itemsTag) {
        if (!itemsTag.getAllKeys().isEmpty()) {
            return;
        }
        var tag = cell.getTag();
        if (tag == null) {
            return;
        }
        var cellTag = tag.getCompound(CELL_TAG);
        cellTag.remove(ITEMS_TAG);
        if (cellTag.isEmpty()) {
            tag.remove(CELL_TAG);
        }
        if (tag.isEmpty()) {
            cell.setTag(null);
        }
    }

    @Nullable
    private static CompoundTag getItemsTag(ItemStack cell, boolean create) {
        CompoundTag tag = cell.getTag();
        if (tag == null) {
            if (!create) {
                return null;
            }
            tag = new CompoundTag();
            cell.setTag(tag);
        }

        CompoundTag cellTag;
        if (tag.contains(CELL_TAG, Tag.TAG_COMPOUND)) {
            cellTag = tag.getCompound(CELL_TAG);
        } else {
            if (!create) {
                return null;
            }
            cellTag = new CompoundTag();
            tag.put(CELL_TAG, cellTag);
        }

        if (cellTag.contains(ITEMS_TAG, Tag.TAG_COMPOUND)) {
            return cellTag.getCompound(ITEMS_TAG);
        }

        if (!create) {
            return null;
        }

        CompoundTag itemsTag = new CompoundTag();
        cellTag.put(ITEMS_TAG, itemsTag);
        return itemsTag;
    }
}
