package appeng.integration.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.item.ItemStack;

import appeng.api.integration.machines.IProcessingMachine;
import appeng.crafting.CraftingJob;

/**
 * Stub transfer handler that only logs interactions. Real machine implementations will replace this in later phases.
 */
public final class ProcessingTransferHandler implements IProcessingMachine.ProcessingMachineTransfer {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingTransferHandler.class);

    private final CraftingJob job;
    private final IProcessingMachine machine;

    public ProcessingTransferHandler(CraftingJob job, IProcessingMachine machine) {
        this.job = job;
        this.machine = machine;
    }

    @Override
    public void pushToMachine(ItemStack stack) {
        LOG.debug("Stub transfer: pushing {} to external machine {} for job {}", stack, machine, job.getId());
    }

    @Override
    public ItemStack pullFromMachine(ItemStack requested) {
        LOG.debug("Stub transfer: requesting {} from external machine {} for job {}", requested, machine, job.getId());
        return ItemStack.EMPTY;
    }
}
