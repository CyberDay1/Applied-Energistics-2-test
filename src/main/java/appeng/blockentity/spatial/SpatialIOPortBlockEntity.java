package appeng.blockentity.spatial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import appeng.block.spatial.SpatialIOPortBlock;
import appeng.items.storage.spatial.SpatialCellItem;
import appeng.registry.AE2BlockEntities;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.core.AELog;

/**
 * Placeholder block entity for the spatial IO port. It currently just keeps track of the inserted spatial storage cell
 * and remembers the computed region size for the cell tier. Actual capture/restore logic will be implemented later.
 */
public class SpatialIOPortBlockEntity extends BlockEntity implements InternalInventoryHost {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_LAST_POWERED = "LastPowered";
    private static final String TAG_LAST_ACTION = "LastAction";

    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 2);
    private boolean lastPowered;
    private BlockPos cachedRegionSize = BlockPos.ZERO;
    private LastAction lastAction = LastAction.NONE;

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
        tag.putString(TAG_LAST_ACTION, lastAction.name());
    }

    @Override
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        super.load(tag, registries);
        inventory.readFromNBT(tag, TAG_INVENTORY, registries);
        lastPowered = tag.getBoolean(TAG_LAST_POWERED);
        if (tag.contains(TAG_LAST_ACTION, Tag.TAG_STRING)) {
            try {
                lastAction = LastAction.valueOf(tag.getString(TAG_LAST_ACTION));
            } catch (IllegalArgumentException ignored) {
                lastAction = LastAction.NONE;
            }
        } else {
            lastAction = LastAction.NONE;
        }
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
        if (!validateCellPresent(cell)) {
            return;
        }

        cacheRegionSize(cell);
        onCapture();
    }

    public void restoreRegion() {
        ItemStack cell = getSpatialCell();
        if (!validateCellPresent(cell)) {
            return;
        }

        cacheRegionSize(cell);
        onRestore();
    }

    public void onCapture() {
        if (!hasCachedRegion()) {
            return;
        }

        log(Component.translatable("log.ae2.spatial.capture_start", formatRegionSize(cachedRegionSize)));
        lastAction = LastAction.CAPTURE;
        setChanged();
    }

    public void onRestore() {
        if (!hasCachedRegion()) {
            return;
        }

        log(Component.translatable("log.ae2.spatial.restore_start", formatRegionSize(cachedRegionSize)));
        lastAction = LastAction.RESTORE;
        setChanged();
    }

    public BlockPos getRegionSize() {
        return getCachedSize();
    }

    public BlockPos getCachedSize() {
        return cachedRegionSize;
    }

    private void updateRegionSize() {
        ItemStack cell = getSpatialCell();
        cacheRegionSize(cell);
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

    private void cacheRegionSize(ItemStack stack) {
        cachedRegionSize = getRegionSizeFromCell(stack);
        if (!hasCachedRegion()) {
            lastAction = LastAction.NONE;
        }
    }

    public boolean hasSpatialCell() {
        return !getSpatialCell().isEmpty() && getSpatialCell().getItem() instanceof SpatialCellItem;
    }

    public boolean isLastPowered() {
        return lastPowered;
    }

    public @Nullable BlockPos getCachedRegionSizeIfPresent() {
        return cachedRegionSize.equals(BlockPos.ZERO) ? null : cachedRegionSize;
    }

    public LastAction getLastAction() {
        return lastAction;
    }

    public static BlockPos getRegionSizeFromCell(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof SpatialCellItem spatialCell)) {
            return BlockPos.ZERO;
        }

        int size = spatialCell.getMaxStoredDim(stack);
        return size > 0 ? new BlockPos(size, size, size) : BlockPos.ZERO;
    }

    private boolean validateCellPresent(ItemStack cell) {
        if (cell.isEmpty() || !(cell.getItem() instanceof SpatialCellItem)) {
            log(Component.translatable("tooltip.ae2.spatial.no_cell"));
            cachedRegionSize = BlockPos.ZERO;
            return false;
        }
        return true;
    }

    private boolean hasCachedRegion() {
        return !cachedRegionSize.equals(BlockPos.ZERO);
    }

    private static void log(Component message) {
        AELog.info(message.getString());
    }

    private static String formatRegionSize(BlockPos size) {
        if (size.equals(BlockPos.ZERO)) {
            return "0x0x0";
        }
        return size.getX() + "x" + size.getY() + "x" + size.getZ();
    }

    public enum LastAction {
        NONE,
        CAPTURE,
        RESTORE
    }
}
