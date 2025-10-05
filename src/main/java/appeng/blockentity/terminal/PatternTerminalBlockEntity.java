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

public class PatternTerminalBlockEntity extends CraftingTerminalBlockEntity {
    private static final int PATTERN_SLOT_COUNT = 2;
    private static final int BLANK_PATTERN_SLOT = 0;
    private static final int ENCODED_PATTERN_SLOT = 1;

    private final NonNullList<ItemStack> patternItems =
            NonNullList.withSize(PATTERN_SLOT_COUNT, ItemStack.EMPTY);

    private final Container patternInventory = new Container() {
        @Override
        public int getContainerSize() {
            return PATTERN_SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < PATTERN_SLOT_COUNT; i++) {
                if (!patternItems.get(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return patternItems.get(index);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            ItemStack removed = ContainerHelper.removeItem(patternItems, index, count);
            if (!removed.isEmpty()) {
                setChanged();
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            ItemStack stack = patternItems.get(index);
            if (!stack.isEmpty()) {
                patternItems.set(index, ItemStack.EMPTY);
                setChanged();
            }
            return stack;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            patternItems.set(index, stack);
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
            PatternTerminalBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < PATTERN_SLOT_COUNT; i++) {
                patternItems.set(i, ItemStack.EMPTY);
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

    private boolean processingMode;

    public PatternTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.PATTERN_TERMINAL.get(), pos, state);
    }

    public Container getPatternInventory() {
        return patternInventory;
    }

    public ItemStack getBlankPatternStack() {
        return patternItems.get(BLANK_PATTERN_SLOT);
    }

    public void setBlankPatternStack(ItemStack stack) {
        patternItems.set(BLANK_PATTERN_SLOT, stack);
        setChanged();
    }

    public ItemStack getEncodedPatternStack() {
        return patternItems.get(ENCODED_PATTERN_SLOT);
    }

    public void setEncodedPatternStack(ItemStack stack) {
        patternItems.set(ENCODED_PATTERN_SLOT, stack);
        setChanged();
    }

    public boolean isProcessingMode() {
        return processingMode;
    }

    public void setProcessingMode(boolean processingMode) {
        this.processingMode = processingMode;
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag.getCompound("PatternInventory"), patternItems);
        processingMode = tag.getBoolean("ProcessingMode");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag patternTag = new CompoundTag();
        ContainerHelper.saveAllItems(patternTag, patternItems);
        tag.put("PatternInventory", patternTag);
        tag.putBoolean("ProcessingMode", processingMode);
    }
}
