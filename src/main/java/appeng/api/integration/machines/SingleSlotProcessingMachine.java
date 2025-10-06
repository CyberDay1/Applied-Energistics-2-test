package appeng.api.integration.machines;

import appeng.crafting.CraftingJob;

/**
 * Convenience base class for machines that expose a single logical slot for inputs and outputs.
 */
public abstract class SingleSlotProcessingMachine extends AbstractProcessingMachine {
    @Override
    public void acceptInputs(CraftingJob job, ProcessingMachineTransfer transfer) {
        handleSingleSlotInput(job, transfer);
    }

    @Override
    public void provideOutputs(CraftingJob job, ProcessingMachineTransfer transfer) {
        handleSingleSlotOutput(job, transfer);
    }

    /**
     * Hook for delivering all job inputs into the backing machine slot.
     */
    protected abstract void handleSingleSlotInput(CraftingJob job, ProcessingMachineTransfer transfer);

    /**
     * Hook for pulling job outputs from the backing machine slot.
     */
    protected abstract void handleSingleSlotOutput(CraftingJob job, ProcessingMachineTransfer transfer);
}
