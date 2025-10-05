package appeng.blockentity.simple;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.wrapper.InvWrapper;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.items.storage.BasicCellItem;
import appeng.items.storage.fluid.BasicFluidCellItem;
import appeng.grid.NodeType;
import appeng.grid.SimpleGridNode;
import appeng.registry.AE2BlockEntities;
import appeng.storage.impl.StorageService;
import appeng.util.GridHelper;

public class DriveBlockEntity extends BlockEntity implements IGridHost {
    private static final int SLOT_COUNT = 4;

    private final NonNullList<ItemStack> cells = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final Container container = new Container() {
        @Override
        public int getContainerSize() {
            return SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            for (var stack : cells) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return cells.get(index);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            ItemStack removed = ContainerHelper.removeItem(cells, index, count);
            if (!removed.isEmpty()) {
                onCellsChanged();
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            ItemStack stack = cells.get(index);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            cells.set(index, ItemStack.EMPTY);
            onCellsChanged();
            return stack;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            cells.set(index, stack);
            if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
            onCellsChanged();
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void setChanged() {
            DriveBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < SLOT_COUNT; i++) {
                cells.set(i, ItemStack.EMPTY);
            }
            onCellsChanged();
        }

        @Override
        public void startOpen(Player player) {
        }

        @Override
        public void stopOpen(Player player) {
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return stack.isEmpty() || stack.getItem() instanceof BasicCellItem
                    || stack.getItem() instanceof BasicFluidCellItem;
        }
    };
    private final InvWrapper itemHandler = new InvWrapper(container);
    private final SimpleGridNode gridNode = new SimpleGridNode(NodeType.MACHINE);
    private UUID mountedGridId;

    public DriveBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.DRIVE_SIMPLE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        GridHelper.discover(this);
        ensureMounted();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (mountedGridId != null) {
            StorageService.unmountDrive(mountedGridId, this);
            GridHelper.updateSetMetadata(mountedGridId);
            mountedGridId = null;
        }
    }

    private void onCellsChanged() {
        setChanged();
        ensureMounted();
    }

    public NonNullList<ItemStack> getCells() {
        return cells;
    }

    public InvWrapper getItemHandler() {
        return itemHandler;
    }

    @Override
    public IGridNode getGridNode() {
        return gridNode;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, cells);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, cells);
    }

    public int getCellSlotCount() {
        return SLOT_COUNT;
    }

    public ItemStack getCellInSlot(int slot) {
        return cells.get(slot);
    }

    public void notifyGridChanged() {
        ensureMounted();
    }

    private void ensureMounted() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        UUID grid = gridNode.getGridId();
        if (grid == null) {
            if (mountedGridId != null) {
                StorageService.unmountDrive(mountedGridId, this);
                GridHelper.updateSetMetadata(mountedGridId);
                mountedGridId = null;
            }
            return;
        }

        if (!grid.equals(mountedGridId)) {
            if (mountedGridId != null) {
                StorageService.unmountDrive(mountedGridId, this);
            }
            mountedGridId = grid;
            StorageService.mountDrive(grid, this);
        } else {
            StorageService.refreshDrive(grid, this);
        }

        GridHelper.updateSetMetadata(grid);
    }
}
