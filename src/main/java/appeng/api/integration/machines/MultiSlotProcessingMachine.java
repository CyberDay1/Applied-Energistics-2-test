package appeng.api.integration.machines;

import java.util.List;

import net.minecraft.world.item.ItemStack;

import appeng.crafting.CraftingJob;

/**
 * Convenience base class for machines that manage multiple logical slots. Sub-classes describe the slot layout so the
 * network can distribute inputs and request outputs appropriately.
 */
public abstract class MultiSlotProcessingMachine extends AbstractProcessingMachine {
    @Override
    public void acceptInputs(CraftingJob job, ProcessingMachineTransfer transfer) {
        var stacks = mapInputs(job);
        for (var stack : stacks) {
            transfer.pushToMachine(stack);
        }
    }

    @Override
    public void provideOutputs(CraftingJob job, ProcessingMachineTransfer transfer) {
        var requests = mapOutputs(job);
        for (var request : requests) {
            transfer.pullFromMachine(request);
        }
    }

    /**
     * {@return the ordered list of stacks that should be delivered to the backing machine.}
     */
    protected abstract List<ItemStack> mapInputs(CraftingJob job);

    /**
     * {@return the ordered list of stacks that should be requested from the backing machine.}
     */
    protected abstract List<ItemStack> mapOutputs(CraftingJob job);
}
