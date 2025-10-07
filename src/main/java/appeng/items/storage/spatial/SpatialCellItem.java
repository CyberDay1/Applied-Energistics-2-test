/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2024, TeamAppliedEnergistics, All rights reserved.
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

package appeng.items.storage.spatial;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import appeng.items.storage.SpatialStorageCellItem;

/**
 * Base class for spatial storage cells that records placeholder capture data.
 */
public class SpatialCellItem extends SpatialStorageCellItem {
    private static final String CAPTURED_REGION_TAG = "CapturedRegion";
    private static final String SIZE_X_TAG = "SizeX";
    private static final String SIZE_Y_TAG = "SizeY";
    private static final String SIZE_Z_TAG = "SizeZ";

    protected SpatialCellItem(Properties properties, int spatialScale) {
        super(properties, spatialScale);
    }

    @Override
    public void setStoredDimension(ItemStack stack, int plotId, BlockPos size) {
        super.setStoredDimension(stack, plotId, size);

        if (plotId >= 0) {
            storeCapturedRegion(stack, size);
        }
    }

    @Override
    public boolean doSpatialTransition(ItemStack stack, ServerLevel level, BlockPos min, BlockPos max, int playerId) {
        boolean success = super.doSpatialTransition(stack, level, min, max, playerId);
        if (success && getAllocatedPlotId(stack) == -1) {
            clearCapturedRegion(stack);
        }
        return success;
    }

    /**
     * Stores simple placeholder information about the captured region in the cell's NBT.
     */
    protected void storeCapturedRegion(ItemStack stack, BlockPos size) {
        CompoundTag regionTag = new CompoundTag();
        regionTag.putInt(SIZE_X_TAG, size.getX());
        regionTag.putInt(SIZE_Y_TAG, size.getY());
        regionTag.putInt(SIZE_Z_TAG, size.getZ());

        stack.getOrCreateTag().put(CAPTURED_REGION_TAG, regionTag);
    }

    /**
     * Removes any stored capture data from the item stack.
     */
    public void clearCapturedRegion(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(CAPTURED_REGION_TAG);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }

    /**
     * Returns whether this cell currently contains captured region information.
     */
    public boolean hasCapturedRegion(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(CAPTURED_REGION_TAG, Tag.TAG_COMPOUND);
    }

    /**
     * Retrieves the stored capture data, if present.
     */
    public CompoundTag getCapturedRegion(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(CAPTURED_REGION_TAG, Tag.TAG_COMPOUND)) {
            return tag.getCompound(CAPTURED_REGION_TAG);
        }
        return null;
    }
}
