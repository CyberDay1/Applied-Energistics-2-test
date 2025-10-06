package appeng.api.integration.machines;

import appeng.crafting.CraftingJob;

/**
 * Base implementation that tracks a single running job. Concrete machines can extend this to reuse common lifecycle
 * handling.
 */
public abstract class AbstractProcessingMachine implements IProcessingMachine {
    private boolean busy;

    @Override
    public final boolean isBusy() {
        return busy;
    }

    @Override
    public boolean canProcess(CraftingJob job) {
        return !busy && IProcessingMachine.super.canProcess(job);
    }

    @Override
    public void beginProcessing(CraftingJob job) {
        busy = true;
    }

    @Override
    public void finishProcessing(CraftingJob job) {
        busy = false;
    }
}
