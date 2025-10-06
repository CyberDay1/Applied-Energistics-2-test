package appeng.integration.processing;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.item.ItemStack;

import appeng.api.integration.machines.IProcessingMachine;
import appeng.crafting.CraftingJob;
import appeng.integration.processing.FurnaceProcessingMachine;
import appeng.storage.impl.StorageService;

/**
 * Coordinates transfers between the ME network and an external processing machine.
 */
public final class ProcessingTransferHandler implements IProcessingMachine.ProcessingMachineTransfer {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingTransferHandler.class);

    private final CraftingJob job;
    private final IProcessingMachine machine;

    private ItemStack pendingInput = ItemStack.EMPTY;
    private int insertedOutputs;
    private int droppedOutputs;

    public ProcessingTransferHandler(CraftingJob job, IProcessingMachine machine) {
        this.job = job;
        this.machine = machine;
    }

    @Override
    public void pushToMachine(ItemStack stack) {
        if (machine instanceof FurnaceProcessingMachine furnace) {
            UUID gridId = furnace.getGridId();
            if (gridId == null) {
                throw new IllegalStateException("Furnace machine has no grid binding");
            }

            int requested = stack.getCount();
            int extracted = StorageService.extractFromNetwork(gridId, stack.getItem(), requested, false);
            if (extracted <= 0) {
                throw new IllegalStateException("Unable to extract " + stack + " from network for job " + job.getId());
            }
            if (extracted < requested) {
                StorageService.insertIntoNetwork(gridId, stack.getItem(), extracted, false);
                throw new IllegalStateException("Insufficient items available for furnace job " + job.getId());
            }

            ItemStack delivered = stack.copy();
            delivered.setCount(extracted);

            ItemStack remainder = furnace.insertInput(delivered);
            if (!remainder.isEmpty()) {
                ItemStack released = furnace.releaseInput();
                if (!released.isEmpty()) {
                    StorageService.insertIntoNetwork(gridId, released.getItem(), released.getCount(), false);
                }
                StorageService.insertIntoNetwork(gridId, remainder.getItem(), remainder.getCount(), false);
                throw new IllegalStateException("Furnace could not accept inputs for job " + job.getId());
            }

            pendingInput = delivered;
            LOG.debug("Delivered {} to {} for job {}", delivered, furnace, job.getId());
            return;
        }

        LOG.debug("Stub transfer: pushing {} to external machine {} for job {}", stack, machine, job.getId());
    }

    @Override
    public ItemStack pullFromMachine(ItemStack requested) {
        if (machine instanceof FurnaceProcessingMachine furnace) {
            UUID gridId = furnace.getGridId();
            if (gridId == null) {
                throw new IllegalStateException("Furnace machine has no grid binding");
            }

            ItemStack produced = furnace.extractOutput(requested);
            if (produced.isEmpty()) {
                return ItemStack.EMPTY;
            }

            int accepted = StorageService.insertIntoNetwork(gridId, produced.getItem(), produced.getCount(), false);
            insertedOutputs += accepted;

            int remaining = produced.getCount() - accepted;
            if (remaining > 0) {
                ItemStack remainder = produced.copy();
                remainder.setCount(remaining);
                furnace.dropItem(remainder);
                droppedOutputs += remaining;
            }

            pendingInput = ItemStack.EMPTY;
            LOG.debug("Retrieved {} from {} for job {}", produced, furnace, job.getId());
            return produced;
        }

        LOG.debug("Stub transfer: requesting {} from external machine {} for job {}", requested, machine, job.getId());
        return ItemStack.EMPTY;
    }

    public void rollback() {
        if (machine instanceof FurnaceProcessingMachine furnace) {
            UUID gridId = furnace.getGridId();
            ItemStack returned = furnace.releaseInput();
            if (returned.isEmpty() && !pendingInput.isEmpty()) {
                returned = pendingInput.copy();
            }
            if (!returned.isEmpty()) {
                StorageService.insertIntoNetwork(gridId, returned.getItem(), returned.getCount(), false);
            }
            ItemStack stranded = furnace.extractOutput(ItemStack.EMPTY);
            if (!stranded.isEmpty()) {
                furnace.dropItem(stranded);
            }
            pendingInput = ItemStack.EMPTY;
        }
    }

    public void clearTrackedInputs() {
        pendingInput = ItemStack.EMPTY;
    }

    public int getInsertedOutputs() {
        return insertedOutputs;
    }

    public int getDroppedOutputs() {
        return droppedOutputs;
    }
}
