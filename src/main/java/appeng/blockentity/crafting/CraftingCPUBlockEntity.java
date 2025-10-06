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
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.network.AE2Packets;
import appeng.crafting.CraftingJob;
import appeng.crafting.CraftingJobManager;
import appeng.crafting.cpu.CraftingCPUMultiblock;
import appeng.api.storage.ItemStackView;
import appeng.storage.impl.StorageService;

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

    @Nullable
    private UUID runningJobId;
    private int runningJobProgress;
    @Nullable
    private MolecularAssemblerBlockEntity runningAssembler;

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
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.multiblockDirty) {
            this.multiblockDirty = false;

            if (this.multiblock != null) {
                this.multiblock.detach();
            }

            var newCluster = CraftingCPUMultiblock.build(serverLevel, this);
            newCluster.attach();
            validateReservations();
        }

        if (isController()) {
            tickCraftingJobs();
        }
    }

    public int getBaseCapacity() {
        return BASE_CAPACITY;
    }

    public int getCoProcessorCount() {
        return 0;
    }

    public int getTotalCapacity() {
        if (this.multiblock != null) {
            return this.multiblock.getTotalCapacity();
        }
        return BASE_CAPACITY;
    }

    public int getTotalCoProcessors() {
        if (this.multiblock != null) {
            return this.multiblock.getTotalCoProcessors();
        }
        return getCoProcessorCount();
    }

    public int getMaxParallelJobCount() {
        return 1 + getTotalCoProcessors();
    }

    public int getAvailableJobSlots() {
        return Math.max(0, getMaxParallelJobCount() - activeReservations.size());
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

        if (getAvailableJobSlots() <= 0) {
            return false;
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

        int maxParallel = getMaxParallelJobCount();
        int reservations = activeReservations.size();
        if (reservations > maxParallel) {
            LOG.debug(
                    "Crafting CPU at {} has {} job reservations but supports {} parallel jobs; additional reservations will be processed sequentially.",
                    getBlockPos(), reservations, maxParallel);
        }
    }

    private void clearReservations() {
        if (!activeReservations.isEmpty()) {
            LOG.debug("Clearing {} crafting job reservations at {} due to cluster change.",
                    activeReservations.size(), getBlockPos());
            activeReservations.clear();
            reservedCapacity = 0;
            if (runningJobId != null) {
                CraftingJobManager.getInstance().releaseAssembler(runningJobId);
                CraftingJobManager.getInstance().releaseMachine(runningJobId);
            }
            runningJobId = null;
            runningJobProgress = 0;
            runningAssembler = null;
        }
    }

    private void tickCraftingJobs() {
        CraftingJobManager manager = CraftingJobManager.getInstance();
        ServerLevel serverLevel = (ServerLevel) this.level;

        if (runningJobId == null) {
            CraftingJob job = manager.claimReservedJob(this);
            if (job != null) {
                runningJobId = job.getId();
                runningJobProgress = job.getTicksCompleted();
                runningAssembler = null;
                AE2Packets.sendCraftingJobUpdate(serverLevel, getBlockPos(), job);
                setChanged();
            }
        }

        if (runningJobId == null) {
            return;
        }

        CraftingJob job = manager.getJob(runningJobId);
        if (job == null) {
            LOG.debug("Running job {} disappeared from manager; releasing reservation.", runningJobId);
            manager.releaseAssembler(runningJobId);
            manager.releaseMachine(runningJobId);
            releaseReservation(runningJobId);
            runningJobId = null;
            runningJobProgress = 0;
            runningAssembler = null;
            setChanged();
            return;
        }

        if (runningAssembler != null
                && !manager.isAssemblerAssignedToJob(runningJobId, runningAssembler)) {
            runningAssembler = null;
        }

        if (runningAssembler == null) {
            runningAssembler = manager.allocateAssembler(job);
            if (runningAssembler == null) {
                return;
            }
        }

        int previous = runningJobProgress;
        runningJobProgress = job.getTicksCompleted();
        if (runningJobProgress != previous) {
            AE2Packets.sendCraftingJobUpdate(serverLevel, getBlockPos(), job);
        }

        if (runningJobProgress >= job.getTicksRequired()) {
            job.setTicksCompleted(job.getTicksRequired());
            if (!job.isProcessing()) {
                var delivery = deliverJobOutputs(job);
                job.recordOutputDelivery(delivery.inserted(), delivery.dropped());
            }
            manager.jobExecutionCompleted(job, this);
            AE2Packets.sendCraftingJobUpdate(serverLevel, getBlockPos(), job);
            releaseReservation(job.getId());
            manager.releaseAssembler(job.getId());
            manager.releaseMachine(job.getId());
            runningJobId = null;
            runningJobProgress = 0;
            runningAssembler = null;
            setChanged();
            return;
        }
        setChanged();
    }

    public void handleRemoved() {
        if (this.multiblock != null) {
            this.multiblock.detach();
        }
        if (runningJobId != null) {
            CraftingJobManager.getInstance().releaseAssembler(runningJobId);
            CraftingJobManager.getInstance().releaseMachine(runningJobId);
            runningJobId = null;
        }
        runningAssembler = null;
        clearReservations();
    }

    @Nullable
    @Override
    public IGridNode getGridNode() {
        return getMainNode().getNode();
    }

    private OutputDeliveryResult deliverJobOutputs(CraftingJob job) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return new OutputDeliveryResult(0, 0);
        }

        var node = getMainNode().getNode();
        var gridId = node != null ? node.getGridId() : null;

        int inserted = 0;
        int dropped = 0;

        for (ItemStackView view : job.getOutputs()) {
            int remaining = view.count();

            if (gridId != null) {
                int accepted = StorageService.insertIntoNetwork(gridId, view.item(), remaining, false);
                if (accepted > 0) {
                    inserted += accepted;
                    remaining -= accepted;
                }
            }

            if (remaining > 0) {
                dropped += dropRemainder(serverLevel, view, remaining);
            }
        }

        return new OutputDeliveryResult(inserted, dropped);
    }

    private int dropRemainder(ServerLevel level, ItemStackView view, int amount) {
        int dropped = 0;
        int maxStackSize = Math.max(1, new ItemStack(view.item()).getMaxStackSize());
        int remaining = amount;

        while (remaining > 0) {
            int toDrop = Math.min(remaining, maxStackSize);
            ItemStack stack = new ItemStack(view.item(), toDrop);
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, stack);
            dropped += toDrop;
            remaining -= toDrop;
        }

        return dropped;
    }

    private record OutputDeliveryResult(int inserted, int dropped) {
    }
}
