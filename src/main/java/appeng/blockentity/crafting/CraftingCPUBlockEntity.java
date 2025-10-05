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

package appeng.blockentity.crafting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.crafting.CraftingJob;
import appeng.crafting.cpu.CraftingCPUMultiblock;

/**
 * Block entity backing the crafting CPU controller block.
 */
public class CraftingCPUBlockEntity extends AENetworkedBlockEntity
        implements IGridHost, ServerTickingBlockEntity {

    private static final Logger LOG = LoggerFactory.getLogger(CraftingCPUBlockEntity.class);

    private static final int BASE_CAPACITY = 1000;

    private boolean multiblockDirty = true;

    @Nullable
    private CraftingCPUMultiblock multiblock;

    private boolean controller;

    private final Map<UUID, Integer> activeReservations = new HashMap<>();

    private int reservedCapacity;

    public CraftingCPUBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode().setIdlePowerUsage(1.0);
        this.getMainNode().setVisualRepresentation(AEBlocks.CRAFTING_CPU.stack());
    }

    @Override
    public void onReady() {
        super.onReady();
        markStructureDirty();
    }

    public void markStructureDirty() {
        this.multiblockDirty = true;
    }

    @Override
    public void serverTick() {
        if (this.level instanceof ServerLevel serverLevel) {
            if (this.multiblockDirty) {
                this.multiblockDirty = false;

                if (this.multiblock != null) {
                    this.multiblock.detach();
                }

                var newCluster = CraftingCPUMultiblock.build(serverLevel, this);
                newCluster.attach();
                validateReservations();
            }
        }
    }

    public int getBaseCapacity() {
        return BASE_CAPACITY;
    }

    public int getTotalCapacity() {
        if (this.multiblock != null) {
            return this.multiblock.getTotalCapacity();
        }
        return BASE_CAPACITY;
    }

    public int getAvailableCapacity() {
        return Math.max(0, getTotalCapacity() - reservedCapacity);
    }

    public int getReservedCapacity() {
        return reservedCapacity;
    }

    public boolean isController() {
        return controller;
    }

    public CraftingCPUBlockEntity getController() {
        if (this.multiblock != null) {
            return this.multiblock.getController();
        }
        return this;
    }

    @Nullable
    public CraftingCPUMultiblock getMultiblock() {
        return this.multiblock;
    }

    public void setCurrentMultiblock(CraftingCPUMultiblock cluster, boolean controller) {
        this.multiblock = cluster;
        this.controller = controller;
    }

    public void clearCurrentMultiblock(CraftingCPUMultiblock cluster) {
        if (this.multiblock == cluster) {
            this.multiblock = null;
            this.controller = false;
            clearReservations();
        }
    }

    public boolean reserveJob(CraftingJob job, int requiredCapacity) {
        if (job == null) {
            return false;
        }

        if (!isController() && this.multiblock != null) {
            return this.multiblock.getController().reserveJob(job, requiredCapacity);
        }

        if (activeReservations.containsKey(job.getId())) {
            return true;
        }

        int normalizedCapacity = Math.max(0, requiredCapacity);
        if (normalizedCapacity > getAvailableCapacity()) {
            return false;
        }

        activeReservations.put(job.getId(), normalizedCapacity);
        reservedCapacity += normalizedCapacity;
        setChanged();
        return true;
    }

    public void releaseReservation(UUID jobId) {
        Integer reserved = activeReservations.remove(jobId);
        if (reserved != null) {
            reservedCapacity -= reserved;
            if (reservedCapacity < 0) {
                reservedCapacity = recalculateReservedCapacity();
            }
            setChanged();
        }
    }

    public Map<UUID, Integer> getActiveReservations() {
        return Collections.unmodifiableMap(activeReservations);
    }

    private int recalculateReservedCapacity() {
        int total = activeReservations.values().stream().mapToInt(Integer::intValue).sum();
        reservedCapacity = total;
        return total;
    }

    private void validateReservations() {
        recalculateReservedCapacity();
        int totalCapacity = getTotalCapacity();
        if (reservedCapacity > totalCapacity) {
            LOG.debug("Crafting CPU at {} has {} units reserved but only {} available; clamping.",
                    getBlockPos(), reservedCapacity, totalCapacity);
            reservedCapacity = Math.min(reservedCapacity, totalCapacity);
        }
    }

    private void clearReservations() {
        if (!activeReservations.isEmpty()) {
            LOG.debug("Clearing {} crafting job reservations at {} due to cluster change.",
                    activeReservations.size(), getBlockPos());
            activeReservations.clear();
            reservedCapacity = 0;
        }
    }

    public void handleRemoved() {
        if (this.multiblock != null) {
            this.multiblock.detach();
        }
        clearReservations();
    }

    @Nullable
    @Override
    public IGridNode getGridNode() {
        return getMainNode().getNode();
    }
}
