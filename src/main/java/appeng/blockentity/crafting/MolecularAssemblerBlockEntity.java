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

package appeng.blockentity.crafting;

import java.util.Objects;
import java.util.UUID;

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

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.client.render.crafting.AssemblerAnimationStatus;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.crafting.CraftingJob;
import appeng.util.inv.AppEngInternalInventory;
import appeng.crafting.CraftingJobManager;
import appeng.api.storage.ItemStackView;
import appeng.storage.impl.StorageService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;

/**
 * Simplified molecular assembler implementation used by the NeoForge port. It exposes a single pattern slot and keeps
 * track of crafting job progress on behalf of the {@link CraftingJob} manager.
 */
public class MolecularAssemblerBlockEntity extends AENetworkedInvBlockEntity
        implements ISegmentedInventory, IUpgradeableObject, ServerTickingBlockEntity, ICraftingMachine, IPowerChannelState {

    public static final ResourceLocation INV_MAIN = AppEng.makeId("molecular_assembler");

    private static final String TAG_POWERED = "Powered";
    private static final String TAG_JOB_ID = "Job";
    private static final String TAG_JOB_PROGRESS = "JobProgress";
    private static final String TAG_JOB_REQUIRED = "JobRequired";

    private final AppEngInternalInventory patternInventory = new AppEngInternalInventory(this, 1);
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(AEBlocks.MOLECULAR_ASSEMBLER, 0,
            this::saveChanges);

    @Nullable
    private UUID activeJobId;
    private int jobProgress;
    private int jobTicksRequired;
    private boolean powered;
    private boolean registeredWithManager;
    private boolean processingJob;
    private boolean processingInputsExtracted;
    private boolean processingOutputsDelivered;

    @OnlyIn(Dist.CLIENT)
    @Nullable
    private AssemblerAnimationStatus animationStatus;

    public MolecularAssemblerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
    }

    private void registerWithManager() {
        Level level = getLevel();
        if (!registeredWithManager && level != null && !level.isClientSide()) {
            CraftingJobManager.getInstance().registerAssembler(this);
            registeredWithManager = true;
        }
    }

    private void unregisterFromManager() {
        if (registeredWithManager) {
            CraftingJobManager.getInstance().unregisterAssembler(this);
            registeredWithManager = false;
        }
    }

    /**
     * @return {@code true} if this assembler is not currently executing a job.
     */
    public boolean isIdle() {
        return this.activeJobId == null;
    }

    /**
     * @return {@code true} if the assembler is currently executing a job and should render as powered.
     */
    @Override
    public boolean isPowered() {
        return powered;
    }

    private void setPowered(boolean powered) {
        if (this.powered != powered) {
            this.powered = powered;
            markForUpdate();
        }
    }

    /**
     * Returns the encoded pattern stored in the assembler.
     */
    public ItemStack getStoredPattern() {
        return patternInventory.getStackInSlot(0);
    }

    /**
     * Returns {@code true} if the stored pattern matches the supplied crafting job.
     */
    public boolean canAcceptJob(CraftingJob job) {
        if (job == null) {
            return false;
        }

        ItemStack pattern = getStoredPattern();
        if (pattern.isEmpty()) {
            return false;
        }

        return ItemStack.isSameItemSameComponents(pattern, job.getPatternStack());
    }

    /**
     * Assigns a crafting job to this assembler.
     */
    public boolean beginJob(CraftingJob job) {
        if (!isIdle() || !canAcceptJob(job)) {
            return false;
        }

        this.activeJobId = job.getId();
        this.jobTicksRequired = Math.max(1, job.getTicksRequired());
        this.jobProgress = Math.min(jobTicksRequired, Math.max(0, job.getTicksCompleted()));
        this.processingJob = job.isProcessing();
        this.processingInputsExtracted = !processingJob;
        this.processingOutputsDelivered = !processingJob;
        job.setTicksRequired(this.jobTicksRequired);
        job.setTicksCompleted(this.jobProgress);
        setPowered(true);
        setChanged();
        return true;
    }

    /**
     * Clears the currently running job and resets the assembler.
     */
    public void clearJob() {
        this.activeJobId = null;
        this.jobProgress = 0;
        this.jobTicksRequired = 0;
        this.processingJob = false;
        this.processingInputsExtracted = false;
        this.processingOutputsDelivered = false;
        setPowered(false);
        setChanged();
    }

    /**
     * Cancels the currently running job if it matches the supplied identifier.
     */
    public void cancelJob(@Nullable UUID jobId) {
        if (jobId != null && Objects.equals(jobId, this.activeJobId)) {
            if (processingJob && processingInputsExtracted && !processingOutputsDelivered) {
                var job = CraftingJobManager.getInstance().getJob(jobId);
                if (job != null) {
                    refundProcessingInputs(job);
                }
            }
            clearJob();
        }
    }

    @Nullable
    public UUID getActiveJobId() {
        return activeJobId;
    }

    public int getJobProgress() {
        return jobProgress;
    }

    public int getJobTicksRequired() {
        return jobTicksRequired;
    }

    /**
     * Called when the crafting job completes successfully.
     */
    public void onJobCompleted() {
        clearJob();
    }

    @Override
    public void onReady() {
        super.onReady();
        updatePoweredFromState();
        registerWithManager();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        CraftingJobManager.getInstance().releaseAssembler(activeJobId);
        unregisterFromManager();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        CraftingJobManager.getInstance().releaseAssembler(activeJobId);
        unregisterFromManager();
    }

    private void updatePoweredFromState() {
        setPowered(activeJobId != null);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.powered = data.getBoolean(TAG_POWERED);
        if (data.hasUUID(TAG_JOB_ID)) {
            this.activeJobId = data.getUUID(TAG_JOB_ID);
            this.jobProgress = data.getInt(TAG_JOB_PROGRESS);
            this.jobTicksRequired = data.getInt(TAG_JOB_REQUIRED);
        } else {
            this.activeJobId = null;
            this.jobProgress = 0;
            this.jobTicksRequired = 0;
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putBoolean(TAG_POWERED, this.powered);
        if (this.activeJobId != null) {
            data.putUUID(TAG_JOB_ID, this.activeJobId);
            data.putInt(TAG_JOB_PROGRESS, this.jobProgress);
            data.putInt(TAG_JOB_REQUIRED, this.jobTicksRequired);
        }
    }

    @Override
    public InternalInventory getInternalInventory() {
        return this.patternInventory;
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (INV_MAIN.equals(id)) {
            return this.patternInventory;
        }
        if (ISegmentedInventory.UPGRADES.equals(id)) {
            return this.upgrades;
        }

        return null;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        saveChanges();
        if (inv == this.patternInventory && this.patternInventory.getStackInSlot(0).isEmpty()) {
            CraftingJobManager.getInstance().releaseAssembler(activeJobId);
            clearJob();
        }
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @OnlyIn(Dist.CLIENT)
    public void setAnimationStatus(@Nullable AssemblerAnimationStatus status) {
        this.animationStatus = status;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public AssemblerAnimationStatus getAnimationStatus() {
        return this.animationStatus;
    }

    @Nullable
    public IMolecularAssemblerSupportedPattern getCurrentPattern() {
        return null;
    }

    @Override
    public PatternContainerGroup getCraftingMachineInfo() {
        Level level = getLevel();
        if (level != null) {
            return PatternContainerGroup.fromMachine(level, worldPosition, Direction.UP);
        }
        return PatternContainerGroup.nothing();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputs, Direction ejectionDirection) {
        return false;
    }

    @Override
    public boolean acceptsPlans() {
        return false;
    }

    /**
     * Returns the crafting progress in the range {@code [0, 100]} used by the menu and screen.
     */
    public int getCraftingProgress() {
        if (this.activeJobId == null || this.jobTicksRequired <= 0) {
            return 0;
        }

        return Math.min(100, (int) Math.round((this.jobProgress * 100.0) / this.jobTicksRequired));
    }

    @Override
    public void serverTick() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        registerWithManager();

        if (this.activeJobId == null) {
            if (this.powered) {
                setPowered(false);
            }
            return;
        }

        var manager = CraftingJobManager.getInstance();
        var job = manager.getJob(activeJobId);
        if (job == null) {
            manager.releaseAssembler(activeJobId);
            return;
        }

        setPowered(true);
        this.jobTicksRequired = Math.max(1, job.getTicksRequired());
        this.processingJob = job.isProcessing();

        if (!processingJob) {
            this.jobProgress = Math.max(this.jobProgress, job.getTicksCompleted());

            if (this.jobProgress < this.jobTicksRequired) {
                this.jobProgress++;
                job.setTicksCompleted(this.jobProgress);
                setChanged();
            } else if (job.getTicksCompleted() < this.jobTicksRequired) {
                job.setTicksCompleted(this.jobTicksRequired);
            }
            return;
        }

        if (!processingInputsExtracted) {
            if (!tryExtractProcessingInputs(job)) {
                return;
            }
            processingInputsExtracted = true;
            this.jobProgress = 0;
            job.setTicksCompleted(0);
            setChanged();
        }

        if (this.jobProgress < this.jobTicksRequired) {
            this.jobProgress++;
            job.setTicksCompleted(this.jobProgress);
            setChanged();
            return;
        }

        if (!processingOutputsDelivered) {
            deliverProcessingOutputs(job);
            setChanged();
        } else if (job.getTicksCompleted() < this.jobTicksRequired) {
            job.setTicksCompleted(this.jobTicksRequired);
        }
    }

    @Override
    public boolean isActive() {
        return isPowered();
    }

    private boolean tryExtractProcessingInputs(CraftingJob job) {
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        var node = getMainNode().getNode();
        UUID gridId = node != null ? node.getGridId() : null;
        if (gridId == null) {
            return false;
        }

        var inputs = job.getInputs();
        if (inputs.isEmpty()) {
            return true;
        }

        for (ItemStackView view : inputs) {
            int available = StorageService.extractFromNetwork(gridId, view.item(), view.count(), true);
            if (available < view.count()) {
                return false;
            }
        }

        for (int i = 0; i < inputs.size(); i++) {
            var view = inputs.get(i);
            int removed = StorageService.extractFromNetwork(gridId, view.item(), view.count(), false);
            if (removed < view.count()) {
                if (removed > 0) {
                    int reinjected = StorageService.insertIntoNetwork(gridId, view.item(), removed, false);
                    int leftover = removed - reinjected;
                    if (leftover > 0) {
                        dropItems(serverLevel, view, leftover);
                    }
                }
                for (int j = 0; j < i; j++) {
                    var previous = inputs.get(j);
                    int reinjected = StorageService.insertIntoNetwork(gridId, previous.item(), previous.count(),
                            false);
                    int leftover = previous.count() - reinjected;
                    if (leftover > 0) {
                        dropItems(serverLevel, previous, leftover);
                    }
                }
                return false;
            }
        }

        return true;
    }

    private void deliverProcessingOutputs(CraftingJob job) {
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        var node = getMainNode().getNode();
        UUID gridId = node != null ? node.getGridId() : null;

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
                dropped += dropItems(serverLevel, view, remaining);
            }
        }

        this.processingOutputsDelivered = true;
        job.recordOutputDelivery(inserted, dropped);
        if (job.getTicksCompleted() < this.jobTicksRequired) {
            job.setTicksCompleted(this.jobTicksRequired);
        }
    }

    private void refundProcessingInputs(CraftingJob job) {
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        var node = getMainNode().getNode();
        UUID gridId = node != null ? node.getGridId() : null;

        for (ItemStackView view : job.getInputs()) {
            int remaining = view.count();
            if (gridId != null) {
                int accepted = StorageService.insertIntoNetwork(gridId, view.item(), remaining, false);
                remaining -= accepted;
            }

            if (remaining > 0) {
                dropItems(serverLevel, view, remaining);
            }
        }
    }

    private int dropItems(ServerLevel level, ItemStackView view, int amount) {
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
}
