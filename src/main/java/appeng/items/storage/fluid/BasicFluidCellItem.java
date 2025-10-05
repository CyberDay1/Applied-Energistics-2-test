package appeng.items.storage.fluid;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import appeng.api.storage.FluidStackView;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.stacks.AEKeyType;
import appeng.items.AEBaseItem;

public class BasicFluidCellItem extends AEBaseItem implements IBasicCellItem {
    private static final String CELL_TAG = "FluidCellData";
    private static final String FLUIDS_TAG = "Fluids";

    private final int capacity;

    public BasicFluidCellItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.fluids();
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
        var fluidsTag = getFluidsTag(cell, false);
        if (fluidsTag == null) {
            return 0;
        }
        int total = 0;
        for (var key : fluidsTag.getAllKeys()) {
            total += Math.max(0, fluidsTag.getInt(key));
        }
        return total;
    }

    public boolean contains(ItemStack cell, Fluid fluid) {
        return getFluidAmount(cell, fluid) > 0;
    }

    public int getFluidAmount(ItemStack cell, Fluid fluid) {
        var fluidsTag = getFluidsTag(cell, false);
        if (fluidsTag == null) {
            return 0;
        }
        var id = key(fluid);
        return Math.max(0, fluidsTag.getInt(id));
    }

    public int insert(ItemStack cell, Fluid fluid, int amount, boolean simulate) {
        if (amount <= 0 || fluid == Fluids.EMPTY) {
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
            var fluidsTag = getFluidsTag(cell, true);
            var id = key(fluid);
            int current = Math.max(0, fluidsTag.getInt(id));
            fluidsTag.putInt(id, current + toInsert);
        }
        return toInsert;
    }

    public int extract(ItemStack cell, Fluid fluid, int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        var fluidsTag = getFluidsTag(cell, false);
        if (fluidsTag == null) {
            return 0;
        }
        var id = key(fluid);
        int current = Math.max(0, fluidsTag.getInt(id));
        if (current <= 0) {
            return 0;
        }
        int toExtract = Math.min(current, amount);
        if (toExtract <= 0) {
            return 0;
        }
        if (!simulate) {
            if (current == toExtract) {
                fluidsTag.remove(id);
            } else {
                fluidsTag.putInt(id, current - toExtract);
            }
            cleanup(cell, fluidsTag);
        }
        return toExtract;
    }

    public List<FluidStackView> getAll(ItemStack cell) {
        var result = new ArrayList<FluidStackView>();
        var fluidsTag = getFluidsTag(cell, false);
        if (fluidsTag == null) {
            return result;
        }
        for (var key : fluidsTag.getAllKeys()) {
            int amount = Math.max(0, fluidsTag.getInt(key));
            if (amount <= 0) {
                continue;
            }
            var fluid = BuiltInRegistries.FLUID.getOptional(ResourceLocation.parse(key)).orElse(null);
            if (fluid == null || fluid == Fluids.EMPTY) {
                continue;
            }
            result.add(new FluidStackView(fluid, amount));
        }
        return result;
    }

    private static String key(Fluid fluid) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        return id.toString();
    }

    private static void cleanup(ItemStack cell, CompoundTag fluidsTag) {
        if (!fluidsTag.getAllKeys().isEmpty()) {
            return;
        }
        var tag = cell.getTag();
        if (tag == null) {
            return;
        }
        var cellTag = tag.getCompound(CELL_TAG);
        cellTag.remove(FLUIDS_TAG);
        if (cellTag.isEmpty()) {
            tag.remove(CELL_TAG);
        }
        if (tag.isEmpty()) {
            cell.setTag(null);
        }
    }

    @Nullable
    private static CompoundTag getFluidsTag(ItemStack cell, boolean create) {
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

        if (cellTag.contains(FLUIDS_TAG, Tag.TAG_COMPOUND)) {
            return cellTag.getCompound(FLUIDS_TAG);
        }

        if (!create) {
            return null;
        }

        CompoundTag fluidsTag = new CompoundTag();
        cellTag.put(FLUIDS_TAG, fluidsTag);
        return fluidsTag;
    }
}
