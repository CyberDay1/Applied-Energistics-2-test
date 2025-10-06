package appeng.integration.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.item.ItemStack;

import appeng.api.integration.machines.AbstractProcessingMachine;
import appeng.api.integration.machines.IProcessingMachine;
import appeng.crafting.CraftingJob;

/**
 * Coordinates transfers between the ME network and an external processing machine.
 */
public final class ProcessingTransferHandler implements IProcessingMachine.ProcessingMachineTransfer {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingTransferHandler.class);

    private final CraftingJob job;
    private final AbstractProcessingMachine machine;

    private final List<ItemStack> trackedInputs = new ArrayList<>();
    private int insertedOutputs;
    private int droppedOutputs;

    public ProcessingTransferHandler(CraftingJob job, AbstractProcessingMachine machine) {
        this.job = job;
        this.machine = machine;
    }

    @Override
    public void pushToMachine(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemStack delivered = machine.withdrawFromNetwork(stack, job);
        ItemStack remainder = machine.deliverInputToMachine(delivered);
        if (!remainder.isEmpty()) {
            machine.returnToNetwork(delivered);
            machine.returnToNetwork(remainder);
            throw new IllegalStateException("Machine could not accept inputs for job " + job.getId());
        }

        trackedInputs.add(delivered.copy());
        LOG.debug("Delivered {} to {} for job {}", delivered, machine, job.getId());
    }

    @Override
    public ItemStack pullFromMachine(ItemStack requested) {
        ItemStack produced = machine.retrieveOutputFromMachine(requested);
        if (produced.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int accepted = machine.insertOutputsIntoNetwork(produced);
        insertedOutputs += accepted;

        int remaining = produced.getCount() - accepted;
        if (remaining > 0) {
            ItemStack overflow = produced.copy();
            overflow.setCount(remaining);
            machine.handleOverflow(overflow);
            droppedOutputs += overflow.getCount();
        }

        LOG.debug("Retrieved {} from {} for job {}", produced, machine, job.getId());
        return produced;
    }

    public void rollback() {
        ItemStack returned = machine.releaseInputsFromMachine();
        if (!returned.isEmpty()) {
            machine.returnToNetwork(returned);
            subtractTrackedInput(returned);
        }

        for (ItemStack tracked : trackedInputs) {
            machine.returnToNetwork(tracked);
        }
        trackedInputs.clear();

        ItemStack stranded;
        while (!(stranded = machine.retrieveOutputFromMachine(ItemStack.EMPTY)).isEmpty()) {
            machine.handleOverflow(stranded);
        }
    }

    private void subtractTrackedInput(ItemStack returned) {
        int remaining = returned.getCount();
        Iterator<ItemStack> iterator = trackedInputs.iterator();
        while (iterator.hasNext() && remaining > 0) {
            ItemStack tracked = iterator.next();
            if (!ItemStack.isSameItemSameComponents(tracked, returned)) {
                continue;
            }

            if (tracked.getCount() <= remaining) {
                remaining -= tracked.getCount();
                iterator.remove();
            } else {
                tracked.setCount(tracked.getCount() - remaining);
                remaining = 0;
            }
        }
    }

    public void clearTrackedInputs() {
        trackedInputs.clear();
    }

    public int getInsertedOutputs() {
        return insertedOutputs;
    }

    public int getDroppedOutputs() {
        return droppedOutputs;
    }
}
