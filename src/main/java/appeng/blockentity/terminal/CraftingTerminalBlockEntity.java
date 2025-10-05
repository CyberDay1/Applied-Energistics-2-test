package appeng.blockentity.terminal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import appeng.registry.AE2BlockEntities;

public class CraftingTerminalBlockEntity extends TerminalBlockEntity {
    private static final int MATRIX_SIZE = 9;
    private static final int RESULT_SLOT = 9;

    private final NonNullList<ItemStack> items = NonNullList.withSize(MATRIX_SIZE + 1, ItemStack.EMPTY);

    private final Container craftingMatrix = new Container() {
        @Override
        public int getContainerSize() {
            return MATRIX_SIZE;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < MATRIX_SIZE; i++) {
                if (!items.get(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return items.get(index);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            ItemStack removed = ContainerHelper.removeItem(items, index, count);
            if (!removed.isEmpty()) {
                setChanged();
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            ItemStack stack = items.get(index);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            items.set(index, ItemStack.EMPTY);
            setChanged();
            return stack;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            items.set(index, stack);
            if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
            setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public void setChanged() {
            CraftingTerminalBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < MATRIX_SIZE; i++) {
                items.set(i, ItemStack.EMPTY);
            }
            setChanged();
        }

        @Override
        public void startOpen(Player player) {
        }

        @Override
        public void stopOpen(Player player) {
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return true;
        }
    };

    private final Container resultInventory = new Container() {
        @Override
        public int getContainerSize() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return items.get(RESULT_SLOT).isEmpty();
        }

        @Override
        public ItemStack getItem(int index) {
            return items.get(RESULT_SLOT);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            ItemStack removed = ContainerHelper.removeItem(items, RESULT_SLOT, count);
            if (!removed.isEmpty()) {
                setChanged();
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            ItemStack stack = items.get(RESULT_SLOT);
            items.set(RESULT_SLOT, ItemStack.EMPTY);
            if (!stack.isEmpty()) {
                setChanged();
            }
            return stack;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            items.set(RESULT_SLOT, stack);
            if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
            setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public void setChanged() {
            CraftingTerminalBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            items.set(RESULT_SLOT, ItemStack.EMPTY);
            setChanged();
        }

        @Override
        public void startOpen(Player player) {
        }

        @Override
        public void stopOpen(Player player) {
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return false;
        }
    };

    public CraftingTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.CRAFTING_TERMINAL.get(), pos, state);
    }

    public Container getCraftingMatrix() {
        return craftingMatrix;
    }

    public Container getResultInventory() {
        return resultInventory;
    }

    public NonNullList<ItemStack> getMatrixItems() {
        NonNullList<ItemStack> list = NonNullList.withSize(MATRIX_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < MATRIX_SIZE; i++) {
            list.set(i, items.get(i));
        }
        return list;
    }

    public ItemStack getResultItem() {
        return items.get(RESULT_SLOT);
    }

    public void setResultItem(ItemStack stack) {
        items.set(RESULT_SLOT, stack);
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }
}
