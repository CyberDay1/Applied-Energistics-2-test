package appeng.api.integration.machines;

import java.util.OptionalInt;

import net.minecraft.world.item.ItemStack;

import appeng.crafting.CraftingJob;

/**
 * Represents an external machine that can execute a processing pattern job on behalf of the ME network.
 *
 * <p>
 * Implementations are expected to coordinate the physical input/output transfer with the provided
 * {@link ProcessingMachineTransfer} helpers. The network will ensure only processing jobs are routed through
 * this API.
 */
public interface IProcessingMachine {
    /**
     * {@return {@code true} if the machine is currently handling a job and cannot accept another one.}
     */
    boolean isBusy();

    /**
     * Checks whether this machine can execute the given processing job right now.
     */
    default boolean canProcess(CraftingJob job) {
        return job != null && job.isProcessing() && hasCapacity();
    }

    /**
     * Called once a job has been assigned to this machine so it can reserve any required resources.
     */
    default void beginProcessing(CraftingJob job) {
    }

    /**
     * Requests that the machine accept the inputs for the given job using the provided transfer helper.
     */
    void acceptInputs(CraftingJob job, ProcessingMachineTransfer transfer);

    /**
     * Requests that the machine expose its outputs for the given job using the provided transfer helper.
     */
    void provideOutputs(CraftingJob job, ProcessingMachineTransfer transfer);

    /**
     * Called after job completion or cancellation to release any resources held by the machine.
     */
    default void finishProcessing(CraftingJob job) {
    }

    /**
     * {@return the optional capacity limit for concurrent jobs handled by this machine.}
     *
     * <p>
     * When empty, the executor is assumed to support unlimited concurrent jobs. Implementations may override this to
     * expose a specific maximum.
     */
    default OptionalInt getCapacity() {
        return OptionalInt.of(1);
    }

    /**
     * {@return the current number of crafting jobs actively executing on this machine.}
     */
    default int getActiveJobCount() {
        return isBusy() ? 1 : 0;
    }

    /**
     * {@return {@code true} if the machine can accept additional jobs without exceeding its declared capacity.}
     */
    default boolean hasCapacity() {
        var capacity = getCapacity();
        if (capacity.isPresent()) {
            return getActiveJobCount() < capacity.getAsInt();
        }
        return true;
    }

    /**
     * {@return an identifier describing the executor type for scheduling purposes.}
     */
    default String getExecutorTypeId() {
        return getClass().getName();
    }

    /**
     * {@return the reported health for this executor.}
     */
    default ProcessingMachineHealth getHealth() {
        return ProcessingMachineHealth.healthy();
    }

    /**
     * {@return {@code true} if the executor reports itself as healthy.}
     */
    default boolean isHealthy() {
        return getHealth().isHealthy();
    }

    /**
     * Helper used during input/output transfer when interacting with the machine.
     */
    interface ProcessingMachineTransfer {
        /**
         * Pushes an input stack from the ME network into the machine.
         */
        void pushToMachine(ItemStack stack);

        /**
         * Pulls an output stack from the machine back into the ME network.
         */
        ItemStack pullFromMachine(ItemStack requested);
    }
}
