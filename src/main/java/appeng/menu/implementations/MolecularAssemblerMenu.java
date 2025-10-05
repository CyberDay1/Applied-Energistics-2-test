/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.menu.implementations;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.menu.AEBaseMenu;
import appeng.menu.MenuTypeBuilder;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.RestrictedInputSlot;

public class MolecularAssemblerMenu extends AEBaseMenu implements IProgressProvider {

    public static final MenuType<MolecularAssemblerMenu> TYPE = MenuTypeBuilder
            .create(MolecularAssemblerMenu::new, MolecularAssemblerBlockEntity.class)
            .build("molecular_assembler");

    private final MolecularAssemblerBlockEntity assembler;

    @GuiSync(0)
    private int craftProgress;

    public MolecularAssemblerMenu(int id, Inventory playerInventory, MolecularAssemblerBlockEntity be) {
        super(TYPE, id, playerInventory, be);
        this.assembler = be;

        var patternInventory = be.getInternalInventory();
        this.addSlot(new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.MOLECULAR_ASSEMBLER_PATTERN,
                patternInventory, 0), SlotSemantics.ENCODED_PATTERN);

        this.createPlayerInventorySlots(playerInventory);
    }

    @Override
    public void broadcastChanges() {
        this.craftProgress = assembler.getCraftingProgress();
        super.broadcastChanges();
    }

    @Override
    public int getCurrentProgress() {
        return this.craftProgress;
    }

    @Override
    public int getMaxProgress() {
        return 100;
    }
}
