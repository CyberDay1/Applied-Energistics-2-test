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

package appeng.block.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.crafting.CraftingMonitorMenu;
import appeng.util.InteractionUtil;

public class CraftingMonitorBlock extends AbstractCraftingUnitBlock<CraftingMonitorBlockEntity> {
    public CraftingMonitorBlock(ICraftingUnitType type) {
        super(metalProps(), type);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.full();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (InteractionUtil.isInAlternateUseMode(player)) {
            var result = this.removeUpgrade(level, player, pos, AEBlocks.CRAFTING_UNIT.block().defaultBlockState());
            if (result != InteractionResult.FAIL) {
                return result;
            }
        }

        if (level.getBlockEntity(pos) instanceof CraftingMonitorBlockEntity monitor && monitor.isFormed()
                && monitor.isActive()) {
            if (!level.isClientSide()) {
                MenuOpener.open(CraftingMonitorMenu.TYPE, player, MenuLocators.forBlockEntity(monitor));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
