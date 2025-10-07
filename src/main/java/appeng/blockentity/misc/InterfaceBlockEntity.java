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

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.inventories.InternalInventory;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.helpers.IPriorityHost;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.InterfaceLogicHost;
import appeng.me.helpers.BlockEntityNodeListener;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

public class InterfaceBlockEntity extends AENetworkedBlockEntity
        implements IPriorityHost, IUpgradeableObject, IConfigurableObject, InterfaceLogicHost, InternalInventoryHost {

    private static final IGridNodeListener<InterfaceBlockEntity> NODE_LISTENER = new BlockEntityNodeListener<>() {
        @Override
        public void onGridChanged(InterfaceBlockEntity nodeOwner, IGridNode node) {
            nodeOwner.logic.gridChanged();
        }
    };

    private final InterfaceLogic logic = createLogic();
    /**
     * Temporary inventory that backs the Interface menu until the full IO logic is
     * implemented. This allows the UI to present a tangible slot grid without yet
     * synchronizing with the networked storage backend.
     */
    private final AppEngInternalInventory menuStorage = new AppEngInternalInventory(this, 9);

    public InterfaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.menuStorage.setEnableClientEvents(true);
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
        for (int i = 0; i < menuStorage.size(); i++) {
            var stack = menuStorage.getStackInSlot(i);
            if (!stack.isEmpty()) {
                drops.add(stack);
                menuStorage.setItemDirect(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.logic.clearContent();
        for (int i = 0; i < menuStorage.size(); i++) {
            menuStorage.setItemDirect(i, ItemStack.EMPTY);
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        this.logic.writeToNBT(data, registries);
        menuStorage.writeToNBT(data, "MenuStorage", registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.logic.readFromNBT(data, registries);
        menuStorage.readFromNBT(data, "MenuStorage", registries);
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

    /**
     * Provides the temporary inventory exposed via the Interface menu. This will
     * later be replaced with the real storage bridge when the IO logic is
     * implemented.
     */
    public AppEngInternalInventory getMenuStorage() {
        return menuStorage;
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
    }

    @Override
    public boolean isClientSide() {
        return level != null && level.isClientSide();
    }
}
