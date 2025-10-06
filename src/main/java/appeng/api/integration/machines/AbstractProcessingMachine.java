package appeng.api.integration.machines;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.crafting.CraftingJob;
import appeng.integration.processing.ProcessingTransferHandler;
import appeng.storage.impl.StorageService;

/**
 * Base implementation that provides the shared execution lifecycle for external processing machines.
 */
public abstract class AbstractProcessingMachine implements IProcessingMachine {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProcessingMachine.class);

    private boolean busy;

    @Override
    public final boolean isBusy() {
        return busy;
    }

    @Override
    public boolean canProcess(CraftingJob job) {
        return !busy && IProcessingMachine.super.canProcess(job);
    }

    /**
     * Executes the provided job using the machine specific hooks implemented by the subclass.
     */
    public final boolean execute(CraftingJob job) {
        var transfer = new ProcessingTransferHandler(job, this);

        LOG.debug(Component.translatable(getStartTranslationKey(), job.describeOutputs()).getString());

        boolean success = false;
        try {
            beginProcessing(job);
            acceptInputs(job, transfer);

            int ticks = Math.max(runMachine(job), CraftingJob.DEFAULT_TICKS_REQUIRED);
            job.setTicksRequired(ticks);
            job.setTicksCompleted(ticks);

            provideOutputs(job, transfer);
            job.recordOutputDelivery(transfer.getInsertedOutputs(), transfer.getDroppedOutputs());
            success = true;

            LOG.debug(Component.translatable(getCompleteTranslationKey(), job.describeOutputs()).getString());
            return true;
        } catch (Exception e) {
            LOG.debug(Component.translatable(getFailedTranslationKey(), job.describeOutputs()).getString(), e);
            transfer.rollback();
            return false;
        } finally {
            if (success) {
                transfer.clearTrackedInputs();
            }
            try {
                finishProcessing(job);
            } catch (Exception e) {
                LOG.debug("Error while finishing external machine execution for job {} using {}", job.getId(), this, e);
            }
        }
    }

    @Override
    public void beginProcessing(CraftingJob job) {
        busy = true;
    }

    @Override
    public void finishProcessing(CraftingJob job) {
        busy = false;
    }

    /**
     * {@return the translation key used when logging that execution has started.}
     */
    protected abstract String getStartTranslationKey();

    /**
     * {@return the translation key used when logging that execution completed successfully.}
     */
    protected abstract String getCompleteTranslationKey();

    /**
     * {@return the translation key used when logging that execution failed.}
     */
    protected abstract String getFailedTranslationKey();

    /**
     * {@return the grid id the machine should interact with.}
     */
    public abstract UUID getGridId();

    /**
     * Allows the subclass to perform its processing logic and return the number of ticks consumed.
     */
    protected abstract int runMachine(CraftingJob job);

    /**
     * Inserts the provided stack into the backing machine, returning any remainder.
     */
    protected abstract ItemStack insertInput(ItemStack stack);

    /**
     * Releases any staged inputs back to the network, typically as part of rollback handling.
     */
    protected abstract ItemStack releaseInput();

    /**
     * Extracts the prepared outputs from the backing machine.
     */
    protected abstract ItemStack extractOutput(ItemStack requested);

    /**
     * Called when the machine needs to drop an item into the world (typically overflow).
     */
    protected abstract void dropItem(ItemStack stack);

    /**
     * Extracts the requested stack from the ME network.
     */
    public final ItemStack withdrawFromNetwork(ItemStack stack, CraftingJob job) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        UUID gridId = getGridId();
        if (gridId == null) {
            throw new IllegalStateException("Machine has no grid binding for job " + job.getId());
        }

        int requested = Math.max(stack.getCount(), 1);
        int extracted = StorageService.extractFromNetwork(gridId, stack.getItem(), requested, false);
        if (extracted <= 0) {
            throw new IllegalStateException("Unable to extract " + stack + " from network for job " + job.getId());
        }
        if (extracted < requested) {
            StorageService.insertIntoNetwork(gridId, stack.getItem(), extracted, false);
            throw new IllegalStateException("Insufficient items available for job " + job.getId());
        }

        ItemStack delivered = stack.copy();
        delivered.setCount(extracted);
        return delivered;
    }

    /**
     * Reinserts the provided stack into the ME network.
     */
    public final void returnToNetwork(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        UUID gridId = getGridId();
        if (gridId == null) {
            return;
        }

        StorageService.insertIntoNetwork(gridId, stack.getItem(), stack.getCount(), false);
    }

    /**
     * Inserts the produced outputs into the ME network, returning the number of items accepted.
     */
    public final int insertOutputsIntoNetwork(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        UUID gridId = getGridId();
        if (gridId == null) {
            return 0;
        }

        return StorageService.insertIntoNetwork(gridId, stack.getItem(), stack.getCount(), false);
    }

    /**
     * Delivers an extracted stack into the backing machine.
     */
    public final ItemStack deliverInputToMachine(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return insertInput(stack);
    }

    /**
     * Releases staged inputs from the machine.
     */
    public final ItemStack releaseInputsFromMachine() {
        return releaseInput();
    }

    /**
     * Retrieves the prepared outputs from the machine.
     */
    public final ItemStack retrieveOutputFromMachine(ItemStack requested) {
        return extractOutput(requested);
    }

    /**
     * Handles overflow by delegating to the machine drop behaviour.
     */
    public final void handleOverflow(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        dropItem(stack);
    }
}
