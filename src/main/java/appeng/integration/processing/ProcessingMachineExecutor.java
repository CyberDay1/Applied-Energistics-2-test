package appeng.integration.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appeng.api.integration.machines.IProcessingMachine;
import appeng.crafting.CraftingJob;

/**
 * Coordinates the stub execution flow when routing processing jobs to external machines.
 */
public final class ProcessingMachineExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingMachineExecutor.class);

    private ProcessingMachineExecutor() {
    }

    public static boolean tryExecute(CraftingJob job, IProcessingMachine machine) {
        if (job == null || machine == null) {
            return false;
        }

        LOG.debug("Stub external machine executor invoked for job {} using {}", job.getId(), machine);

        var transfer = new ProcessingTransferHandler(job, machine);
        try {
            machine.beginProcessing(job);
            machine.acceptInputs(job, transfer);
            machine.provideOutputs(job, transfer);
        } catch (Exception e) {
            LOG.debug("Stub execution encountered an error for job {}", job.getId(), e);
            return false;
        } finally {
            machine.finishProcessing(job);
        }

        LOG.debug("Stub execution for job {} completed with no action; falling back to assembler.", job.getId());
        return false;
    }
}
