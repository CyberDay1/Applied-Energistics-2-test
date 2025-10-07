package appeng.blockentity.spatial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.block.spatial.SpatialIOPortBlock;
import appeng.items.storage.spatial.SpatialCellItem;
import appeng.registry.AE2BlockEntities;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

/**
 * Placeholder block entity for the spatial IO port. It currently just keeps track of the inserted spatial storage cell
 * and remembers the computed region size for the cell tier. Actual capture/restore logic will be implemented later.
 */
public class SpatialIOPortBlockEntity extends BlockEntity implements InternalInventoryHost {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_LAST_POWERED = "LastPowered";

    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 2);
    private boolean lastPowered;
    private BlockPos cachedRegionSize = BlockPos.ZERO;

    public SpatialIOPortBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.SPATIAL_IO_PORT.get(), pos, state);
        inventory.setMaxStackSize(0, 1);
        inventory.setMaxStackSize(1, 1);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        inventory.writeToNBT(tag, TAG_INVENTORY, registries);
        tag.putBoolean(TAG_LAST_POWERED, lastPowered);
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        super.load(tag, registries);
        inventory.readFromNBT(tag, TAG_INVENTORY, registries);
        lastPowered = tag.getBoolean(TAG_LAST_POWERED);
        updateRegionSize();
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        setChanged();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv == inventory && slot == 0) {
            updateRegionSize();
        }
    }

    @Override
    public boolean isClientSide() {
        return level != null && level.isClientSide;
    }

    public AppEngInternalInventory getInternalInventory() {
        return inventory;
    }

    public void onRedstoneChanged(boolean powered) {
        if (powered && !lastPowered) {
            if (hasCapturedRegion()) {
                restoreRegion();
            } else {
                captureRegion();
            }
        }
        lastPowered = powered;
        if (level != null && !level.isClientSide) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.getBlock() instanceof SpatialIOPortBlock && state.getValue(SpatialIOPortBlock.POWERED) != powered) {
                level.setBlock(worldPosition, state.setValue(SpatialIOPortBlock.POWERED, powered), 3);
            }
        }
    }

    public void captureRegion() {
        ItemStack cell = getSpatialCell();
        if (cell.isEmpty()) {
            return;
        }
        updateRegionSize();
        // TODO: Perform capture logic using the computed region size.
    }

    public void restoreRegion() {
        ItemStack cell = getSpatialCell();
        if (cell.isEmpty()) {
            return;
        }
        updateRegionSize();
        // TODO: Perform restore logic using the computed region size.
    }

    public BlockPos getRegionSize() {
        return cachedRegionSize;
    }

    private void updateRegionSize() {
        ItemStack cell = getSpatialCell();
        cachedRegionSize = computeRegionSize(cell);
    }

    private ItemStack getSpatialCell() {
        return inventory.getStackInSlot(0);
    }

    private boolean hasCapturedRegion() {
        ItemStack cell = getSpatialCell();
        if (cell.isEmpty() || !(cell.getItem() instanceof SpatialCellItem spatialCell)) {
            return false;
        }
        return spatialCell.hasCapturedRegion(cell);
    }

    private static BlockPos computeRegionSize(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ISpatialStorageCell spatialCell)) {
            return BlockPos.ZERO;
        }
        int size = spatialCell.getMaxStoredDim(stack);
        return new BlockPos(size, size, size);
    }

    public boolean hasSpatialCell() {
        return !getSpatialCell().isEmpty();
    }

    public boolean isLastPowered() {
        return lastPowered;
    }

    public @Nullable BlockPos getCachedRegionSizeIfPresent() {
        return cachedRegionSize.equals(BlockPos.ZERO) ? null : cachedRegionSize;
    }
}
