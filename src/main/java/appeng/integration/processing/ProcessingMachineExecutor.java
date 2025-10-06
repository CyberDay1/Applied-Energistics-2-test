package appeng.integration.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appeng.api.integration.machines.AbstractProcessingMachine;
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

        if (machine instanceof AbstractProcessingMachine abstractMachine) {
            return abstractMachine.execute(job);
        }

        LOG.debug("Stub external machine executor invoked for job {} using {}", job.getId(), machine);

        LOG.debug("Stub execution for job {} completed with no action; falling back to assembler.", job.getId());
        return false;
    }
}
