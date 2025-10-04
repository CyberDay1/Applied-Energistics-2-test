package appeng.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.items.wrapper.InvWrapper;

import appeng.registry.AE2BlockEntities;

public class ChargerBlockEntity extends BlockEntity {
    private static final int MAX_CHARGE = 200;

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

    private int chargeTime = 0;

    public ChargerBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.CHARGER_BE.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public InvWrapper getItemHandler() {
        return this.itemHandler;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, this.items);
        this.chargeTime = tag.getInt("ChargeTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putInt("ChargeTime", this.chargeTime);
    }

    public static void tick(BlockPos pos, BlockState state, ChargerBlockEntity be) {
        ItemStack in = be.items.get(0);
        ItemStack out = be.items.get(1);

        if (!in.isEmpty() && out.isEmpty()) {
            be.chargeTime++;
            if (be.chargeTime >= MAX_CHARGE) {
                be.items.set(1, new ItemStack(net.minecraft.world.item.Items.DIAMOND));
                in.shrink(1);
                be.chargeTime = 0;
                be.setChanged();
            }
        } else {
            be.chargeTime = 0;
        }
    }
}
