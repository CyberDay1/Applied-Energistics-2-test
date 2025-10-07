/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.blockentity.misc;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.Nullable;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.network.AE2Packets;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.IPriorityHost;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.me.helpers.MachineSource;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

public class InterfaceBlockEntity extends AENetworkedBlockEntity
        implements IPriorityHost, IUpgradeableObject, IConfigurableObject, InterfaceLogicHost, InternalInventoryHost,
        ServerTickingBlockEntity {

    private static final IGridNodeListener<InterfaceBlockEntity> NODE_LISTENER = new BlockEntityNodeListener<>() {
        @Override
        public void onGridChanged(InterfaceBlockEntity nodeOwner, IGridNode node) {
            nodeOwner.logic.gridChanged();
        }
    };

    private final InterfaceLogic logic = createLogic();
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 0;

    private final AppEngInternalInventory inputBuffer = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory outputBuffer = new AppEngInternalInventory(this, 1);
    private final IActionSource machineSource = new MachineSource(getMainNode()::getNode);
    private ItemStack lastSyncedInput = ItemStack.EMPTY;
    private ItemStack lastSyncedOutput = ItemStack.EMPTY;

    public InterfaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.inputBuffer.setEnableClientEvents(true);
        this.outputBuffer.setEnableClientEvents(true);
    }

    protected InterfaceLogic createLogic() {
        return new InterfaceLogic(getMainNode(), this, getItemFromBlockEntity().asItem());
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return GridHelper.createManagedNode(this, NODE_LISTENER);
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (getMainNode().hasGridBooted()) {
            this.logic.notifyNeighbors();
        }
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        this.logic.addDrops(drops);
        dropBufferContents(inputBuffer, drops);
        dropBufferContents(outputBuffer, drops);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.logic.clearContent();
        inputBuffer.setItemDirect(INPUT_SLOT, ItemStack.EMPTY);
        outputBuffer.setItemDirect(OUTPUT_SLOT, ItemStack.EMPTY);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.logic.writeToNBT(data, registries);
        inputBuffer.writeToNBT(data, "InputBuffer", registries);
        outputBuffer.writeToNBT(data, "OutputBuffer", registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.logic.readFromNBT(data, registries);
        inputBuffer.readFromNBT(data, "InputBuffer", registries);
        outputBuffer.readFromNBT(data, "OutputBuffer", registries);
        this.lastSyncedInput = ItemStack.EMPTY;
        this.lastSyncedOutput = ItemStack.EMPTY;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return this.logic.getCableConnectionType(dir);
    }

    @Override
    public InterfaceLogic getInterfaceLogic() {
        return this.logic;
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AEBlocks.INTERFACE.stack();
    }

    public AppEngInternalInventory getInputBuffer() {
        return inputBuffer;
    }

    public AppEngInternalInventory getOutputBuffer() {
        return outputBuffer;
    }

    @Nullable
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(UPGRADES)) {
            return logic.getUpgrades();
        }
        return super.getSubInventory(id);
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        setChanged();
        if (!isClientSide()) {
            syncBuffers();
        }
    }

    @Override
    public boolean isClientSide() {
        return level != null && level.isClientSide();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (!isClientSide()) {
            syncBuffers();
        }
    }

    @Override
    public void onReady() {
        super.onReady();
        if (!isClientSide()) {
            syncBuffers();
        }
    }

    @Override
    public void serverTick() {
        if (level == null || level.isClientSide()) {
            return;
        }

        var node = getMainNode();
        if (!node.isActive()) {
            return;
        }

        var grid = node.getGrid();
        if (grid == null) {
            return;
        }

        var storage = grid.getStorageService().getInventory();
        var energy = grid.getEnergyService();

        var changed = false;
        changed |= pushBufferToNetwork(inputBuffer, INPUT_SLOT, storage, energy);
        changed |= balanceOutputBuffer(storage, energy);

        if (changed) {
            setChanged();
        }
    }

    public void applyClientBuffers(ItemStack input, ItemStack output) {
        inputBuffer.setItemDirect(INPUT_SLOT, input.copy());
        outputBuffer.setItemDirect(OUTPUT_SLOT, output.copy());
    }

    private void dropBufferContents(AppEngInternalInventory buffer, List<ItemStack> drops) {
        for (int i = 0; i < buffer.size(); i++) {
            var stack = buffer.getStackInSlot(i);
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
                buffer.setItemDirect(i, ItemStack.EMPTY);
            }
        }
    }

    private boolean pushBufferToNetwork(AppEngInternalInventory buffer, int slot, MEStorage network,
            IEnergyService energy) {
        var stack = buffer.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return false;
        }

        var key = AEItemKey.of(stack);
        if (key == null) {
            return false;
        }

        var inserted = StorageHelper.poweredInsert(energy, network, key, stack.getCount(), machineSource);
        if (inserted <= 0) {
            return false;
        }

        var newStack = stack.copy();
        newStack.shrink((int) inserted);
        buffer.setItemDirect(slot, newStack);
        return true;
    }

    private boolean pushPartialToNetwork(AppEngInternalInventory buffer, int slot, MEStorage network,
            IEnergyService energy, int amount) {
        if (amount <= 0) {
            return false;
        }

        var stack = buffer.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return false;
        }

        var key = AEItemKey.of(stack);
        if (key == null) {
            return false;
        }

        var inserted = StorageHelper.poweredInsert(energy, network, key, Math.min(amount, stack.getCount()),
                machineSource);
        if (inserted <= 0) {
            return false;
        }

        var newStack = stack.copy();
        newStack.shrink((int) inserted);
        buffer.setItemDirect(slot, newStack);
        return true;
    }

    private boolean balanceOutputBuffer(MEStorage network, IEnergyService energy) {
        var request = getConfiguredOutput();
        if (request == null || !(request.what() instanceof AEItemKey itemKey)) {
            return pushBufferToNetwork(outputBuffer, OUTPUT_SLOT, network, energy);
        }

        var slotLimit = outputBuffer.getSlotLimit(OUTPUT_SLOT);
        int desiredCount = (int) Math.min(request.amount(), slotLimit);
        if (desiredCount <= 0) {
            return pushBufferToNetwork(outputBuffer, OUTPUT_SLOT, network, energy);
        }

        var currentStack = outputBuffer.getStackInSlot(OUTPUT_SLOT);
        if (!currentStack.isEmpty() && !itemKey.matches(currentStack)) {
            if (!pushBufferToNetwork(outputBuffer, OUTPUT_SLOT, network, energy)) {
                return false;
            }
            currentStack = outputBuffer.getStackInSlot(OUTPUT_SLOT);
        }

        int currentCount = currentStack.isEmpty() ? 0 : currentStack.getCount();
        if (currentCount > desiredCount) {
            return pushPartialToNetwork(outputBuffer, OUTPUT_SLOT, network, energy, currentCount - desiredCount);
        }

        if (currentCount == desiredCount) {
            return false;
        }

        int missing = desiredCount - currentCount;
        var extracted = StorageHelper.poweredExtraction(energy, network, itemKey, missing, machineSource);
        if (extracted <= 0) {
            return false;
        }

        if (currentStack.isEmpty()) {
            outputBuffer.setItemDirect(OUTPUT_SLOT, itemKey.toStack((int) extracted));
        } else {
            var newStack = currentStack.copy();
            newStack.grow((int) extracted);
            outputBuffer.setItemDirect(OUTPUT_SLOT, newStack);
        }
        return true;
    }

    @Nullable
    private GenericStack getConfiguredOutput() {
        var config = logic.getConfig();
        for (int i = 0; i < config.size(); i++) {
            var stack = config.getStack(i);
            if (stack != null && stack.amount() > 0 && stack.what() instanceof AEItemKey) {
                return stack;
            }
        }
        return null;
    }

    private void syncBuffers() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        var input = inputBuffer.getStackInSlot(INPUT_SLOT);
        var output = outputBuffer.getStackInSlot(OUTPUT_SLOT);

        if (stacksEqual(lastSyncedInput, input) && stacksEqual(lastSyncedOutput, output)) {
            return;
        }

        lastSyncedInput = input.copy();
        lastSyncedOutput = output.copy();

        AE2Packets.sendInterfaceBuffers(serverLevel, getBlockPos(), lastSyncedInput, lastSyncedOutput);
    }

    private boolean stacksEqual(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) {
            return true;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }
        return ItemStack.isSameItemSameComponents(a, b) && a.getCount() == b.getCount();
    }
}
