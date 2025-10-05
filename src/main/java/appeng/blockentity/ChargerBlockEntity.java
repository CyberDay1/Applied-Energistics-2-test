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
import appeng.recipe.ChargerRecipe;
import appeng.storage.impl.StorageService;
import appeng.util.GridHelper;

public class ChargerBlockEntity extends BlockEntity implements IStorageHost, IGridHost {
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
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
            ChargerBlockEntity.this.setChanged();
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

    private int chargeTime = 0;

    public ChargerBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.CHARGER_BE.get(), pos, state);
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
        return this.storageService;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, this.items);
        this.chargeTime = tag.getInt("ChargeTime");
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
        tag.putInt("ChargeTime", this.chargeTime);
        if (this.storageService instanceof StorageService impl) {
            tag.put("StorageService", impl.saveNBT());
        }
    }

    public static void tick(BlockPos pos, BlockState state, ChargerBlockEntity be) {
        Level level = be.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        ItemStack in = be.items.get(0);
        ItemStack out = be.items.get(1);

        if (!out.isEmpty()) {
            be.chargeTime = 0;
            return;
        }

        RecipeManager recipeManager = level.getRecipeManager();
        boolean matched = false;
        for (var holder : recipeManager.getAllRecipesFor(AE2RecipeTypes.CHARGER.get())) {
            ChargerRecipe recipe = holder.value();
            if (matchesInput(in, recipe.input())) {
                matched = true;
                be.chargeTime++;
                if (be.chargeTime >= recipe.time()) {
                    be.items.set(1, recipe.result().copy());
                    consumeInput(be.items, 0, recipe.input());
                    be.chargeTime = 0;
                    be.setChanged();
                }
                break;
            }
        }

        if (!matched) {
            be.chargeTime = 0;
        }
    }

    private static boolean matchesInput(ItemStack slot, ItemStack requirement) {
        if (requirement.isEmpty()) {
            return slot.isEmpty();
        }
        return !slot.isEmpty() && slot.getCount() >= requirement.getCount()
                && ItemStack.isSameItemSameComponents(slot, requirement);
    }

    private static void consumeInput(NonNullList<ItemStack> items, int index, ItemStack requirement) {
        if (!requirement.isEmpty()) {
            items.get(index).shrink(requirement.getCount());
        }
    }
}
