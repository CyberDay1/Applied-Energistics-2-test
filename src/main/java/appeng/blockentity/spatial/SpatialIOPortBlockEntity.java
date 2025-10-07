package appeng.blockentity.spatial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import appeng.block.spatial.SpatialIOPortBlock;
import appeng.items.storage.spatial.SpatialCellItem;
import appeng.registry.AE2BlockEntities;
import appeng.core.network.payload.SpatialOpCancelS2CPayload;
import appeng.core.network.payload.SpatialOpCompleteS2CPayload;
import appeng.core.network.payload.SpatialOpInProgressS2CPayload;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.core.AELog;
import appeng.menu.spatial.SpatialIOPortMenu;

import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Placeholder block entity for the spatial IO port. It currently just keeps track of the inserted spatial storage cell
 * and remembers the computed region size for the cell tier. Actual capture/restore logic will be implemented later.
 */
public class SpatialIOPortBlockEntity extends BlockEntity implements InternalInventoryHost {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_LAST_POWERED = "LastPowered";
    private static final String TAG_LAST_ACTION = "LastAction";
    private static final String TAG_IN_PROGRESS = "InProgress";

    private static final int OPERATION_DURATION_TICKS = 5;

    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, 2);
    private boolean lastPowered;
    private BlockPos cachedRegionSize = BlockPos.ZERO;
    private LastAction lastAction = LastAction.NONE;
    private boolean inProgress;
    private int ticksRemaining;

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
        tag.putBoolean(TAG_IN_PROGRESS, inProgress);
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
        inProgress = tag.getBoolean(TAG_IN_PROGRESS);
        if (!inProgress) {
            ticksRemaining = 0;
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
        if (!validateCellPresent(cell) || inProgress) {
            return;
        }

        cacheRegionSize(cell);
        onCapture();
    }

    public void restoreRegion() {
        ItemStack cell = getSpatialCell();
        if (!validateCellPresent(cell) || inProgress) {
            return;
        }

        cacheRegionSize(cell);
        onRestore();
    }

    public void onCapture() {
        if (!hasCachedRegion()) {
            return;
        }

        if (!beginOperation()) {
            return;
        }

        log(Component.translatable("log.ae2.spatial.capture_begin", getRegionEdge(cachedRegionSize)));
        lastAction = LastAction.CAPTURE;
        performCapture();
        setChanged();
    }

    public void onRestore() {
        if (!hasCachedRegion()) {
            return;
        }

        if (!beginOperation()) {
            return;
        }

        log(Component.translatable("log.ae2.spatial.restore_begin", getRegionEdge(cachedRegionSize)));
        lastAction = LastAction.RESTORE;
        performRestore();
        setChanged();
    }

    public boolean beginOperation() {
        if (inProgress) {
            return false;
        }

        inProgress = true;
        ticksRemaining = OPERATION_DURATION_TICKS;
        setChanged();
        broadcastInProgress(true);
        return true;
    }

    public void endOperation() {
        if (!inProgress) {
            return;
        }

        cancelOperation();
    }

    public void cancelOperation() {
        if (!inProgress) {
            return;
        }

        inProgress = false;
        ticksRemaining = 0;
        log(Component.translatable("log.ae2.spatial.cancelled"));
        rollbackOperation();
        setChanged();
        broadcastInProgress(false);
        broadcastCancellation();
    }

    private void onOperationComplete() {
        if (!inProgress) {
            return;
        }

        inProgress = false;
        ticksRemaining = 0;
        log(Component.translatable("log.ae2.spatial.complete"));
        setChanged();
        broadcastInProgress(false);
        broadcastCompletion();
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void tickOperation() {
        if (!inProgress) {
            return;
        }

        if (ticksRemaining > 0) {
            ticksRemaining--;
        }

        if (ticksRemaining <= 0) {
            onOperationComplete();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpatialIOPortBlockEntity blockEntity) {
        if (level != null && level.isClientSide) {
            return;
        }
        blockEntity.tickOperation();
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

    private void performCapture() {
        // Placeholder for the actual spatial capture implementation in Phase 5.
    }

    private void performRestore() {
        // Placeholder for the actual spatial restore implementation in Phase 5.
    }

    protected void rollbackOperation() {
        AELog.info("Spatial IO rollback stub invoked.");
    }

    private void broadcastInProgress(boolean inProgress) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (ServerPlayer player : serverLevel.players()) {
            if (player.containerMenu instanceof SpatialIOPortMenu menu && menu.getBlockEntity() == this) {
                menu.setInProgress(inProgress);
                PacketDistributor.sendToPlayer(player, new SpatialOpInProgressS2CPayload(menu.containerId,
                        worldPosition, inProgress));
            }
        }
    }

    private void broadcastCompletion() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (ServerPlayer player : serverLevel.players()) {
            if (player.containerMenu instanceof SpatialIOPortMenu menu && menu.getBlockEntity() == this) {
                menu.handleOperationComplete();
                PacketDistributor.sendToPlayer(player,
                        new SpatialOpCompleteS2CPayload(menu.containerId, worldPosition));
            }
        }
    }

    private void broadcastCancellation() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (ServerPlayer player : serverLevel.players()) {
            if (player.containerMenu instanceof SpatialIOPortMenu menu && menu.getBlockEntity() == this) {
                menu.handleOperationCancelled();
                PacketDistributor.sendToPlayer(player,
                        new SpatialOpCancelS2CPayload(menu.containerId, worldPosition));
            }
        }
    }

    private static String formatRegionSize(BlockPos size) {
        if (size.equals(BlockPos.ZERO)) {
            return "0x0x0";
        }
        return size.getX() + "x" + size.getY() + "x" + size.getZ();
    }

    private static int getRegionEdge(BlockPos size) {
        return size.getX();
    }

    public enum LastAction {
        NONE,
        CAPTURE,
        RESTORE
    }
}
