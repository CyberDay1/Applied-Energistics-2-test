package appeng.integration.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.chat.Component;

import appeng.api.integration.machines.IProcessingMachine;
import appeng.crafting.CraftingJob;
import appeng.integration.processing.FurnaceProcessingMachine;

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

        if (machine instanceof FurnaceProcessingMachine furnace) {
            LOG.debug(Component
                    .translatable("message.appliedenergistics2.processing_job.external_furnace_started",
                            job.describeOutputs())
                    .getString());
            var transfer = new ProcessingTransferHandler(job, machine);
            boolean success = false;
            try {
                machine.beginProcessing(job);
                machine.acceptInputs(job, transfer);

                int ticks = furnace.process(job);
                job.setTicksRequired(Math.max(ticks, 1));
                job.setTicksCompleted(job.getTicksRequired());

                machine.provideOutputs(job, transfer);
                job.recordOutputDelivery(transfer.getInsertedOutputs(), transfer.getDroppedOutputs());
                success = true;
                LOG.debug(Component
                        .translatable("message.appliedenergistics2.processing_job.external_furnace_complete",
                                job.describeOutputs())
                        .getString());
                return true;
            } catch (Exception e) {
                LOG.debug(Component
                        .translatable("message.appliedenergistics2.processing_job.external_furnace_failed",
                                job.describeOutputs())
                        .getString(), e);
                transfer.rollback();
                return false;
            } finally {
                if (success) {
                    transfer.clearTrackedInputs();
                }
                try {
                    machine.finishProcessing(job);
                } catch (Exception e) {
                    LOG.debug("Error while finishing external furnace execution for job {}", job.getId(), e);
                }
            }
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
