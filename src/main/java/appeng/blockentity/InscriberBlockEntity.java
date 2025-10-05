package appeng.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.wrapper.InvWrapper;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.api.storage.IStorageHost;
import appeng.api.storage.IStorageService;
import appeng.grid.SimpleGridNode;
import appeng.registry.AE2BlockEntities;
import appeng.recipe.AE2RecipeTypes;
import appeng.recipe.InscriberRecipe;
import appeng.storage.impl.StorageService;
import appeng.util.GridHelper;

public class InscriberBlockEntity extends BlockEntity implements IStorageHost, IGridHost {
    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final Container container = new Container() {
        @Override
        public int getContainerSize() {
            return items.size();
        }

        @Override
        public boolean isEmpty() {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
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
            InscriberBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < items.size(); i++) {
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
    private final InvWrapper itemHandler = new InvWrapper(this.container);
    private final IGridNode gridNode = new SimpleGridNode();
    private final IStorageService storageService = new StorageService();

    public InscriberBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.INSCRIBER_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        GridHelper.discover(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // TODO: Prune connections when nodes are removed
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public InvWrapper getItemHandler() {
        return this.itemHandler;
    }

    @Override
    public IGridNode getGridNode() {
        return gridNode;
    }

    @Override
    public IStorageService getStorageService() {
        return storageService;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, this.items);
        if (this.storageService instanceof StorageService impl) {
            if (tag.contains("StorageService")) {
                impl.loadNBT(tag.getCompound("StorageService"));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        if (this.storageService instanceof StorageService impl) {
            tag.put("StorageService", impl.saveNBT());
        }
    }

    public static void tick(BlockPos pos, BlockState state, InscriberBlockEntity be) {
        Level level = be.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        ItemStack top = be.items.get(0);
        ItemStack bottom = be.items.get(1);
        ItemStack middle = be.items.get(2);
        ItemStack output = be.items.get(3);

        if (!output.isEmpty()) {
            return;
        }

        RecipeManager recipeManager = level.getRecipeManager();
        for (var holder : recipeManager.getAllRecipesFor(AE2RecipeTypes.INSCRIBER.get())) {
            InscriberRecipe recipe = holder.value();
            if (matchesSlot(top, recipe.top()) && matchesSlot(middle, recipe.middle())
                    && matchesSlot(bottom, recipe.bottom())) {
                be.items.set(3, recipe.result().copy());
                consumeSlot(be.items, 0, recipe.top());
                consumeSlot(be.items, 2, recipe.middle());
                consumeSlot(be.items, 1, recipe.bottom());
                be.setChanged();
                break;
            }
        }
    }

    private static boolean matchesSlot(ItemStack slot, ItemStack requirement) {
        if (requirement.isEmpty()) {
            return slot.isEmpty();
        }
        return !slot.isEmpty() && slot.getCount() >= requirement.getCount()
                && ItemStack.isSameItemSameComponents(slot, requirement);
    }

    private static void consumeSlot(NonNullList<ItemStack> items, int index, ItemStack requirement) {
        if (!requirement.isEmpty()) {
            items.get(index).shrink(requirement.getCount());
        }
    }
}
