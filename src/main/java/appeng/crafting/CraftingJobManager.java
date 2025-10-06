package appeng.crafting;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import appeng.blockentity.crafting.CraftingCPUBlockEntity;
import appeng.api.integration.machines.IProcessingMachine;
import appeng.api.storage.ItemStackView;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.integration.processing.ProcessingMachineExecutor;
import appeng.integration.processing.ProcessingMachineRegistry;

/**
 * Tracks planned crafting jobs server-side.
 */
public final class CraftingJobManager {
    private static final CraftingJobManager INSTANCE = new CraftingJobManager();

    private static final Logger LOG = LoggerFactory.getLogger(CraftingJobManager.class);

    private final Map<UUID, CraftingJob> jobs = new ConcurrentHashMap<>();
    private final Map<UUID, CraftingJob> completedJobs = new ConcurrentHashMap<>();
    private final Map<UUID, CraftingJobReservation> reservations = new ConcurrentHashMap<>();
    private final Set<MolecularAssemblerBlockEntity> assemblers = ConcurrentHashMap.newKeySet();
    private final ConcurrentMap<UUID, MolecularAssemblerBlockEntity> jobAssemblers = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, IProcessingMachine> jobMachines = new ConcurrentHashMap<>();

    private CraftingJobManager() {
    }

    public static CraftingJobManager getInstance() {
        return INSTANCE;
    }

    public void registerAssembler(MolecularAssemblerBlockEntity assembler) {
        if (assembler != null) {
            assemblers.add(assembler);
        }
    }

    public void unregisterAssembler(MolecularAssemblerBlockEntity assembler) {
        if (assembler == null) {
            return;
        }

        assemblers.remove(assembler);

        for (var entry : jobAssemblers.entrySet()) {
            if (entry.getValue() == assembler) {
                UUID jobId = entry.getKey();
                jobAssemblers.remove(jobId, assembler);
                assembler.cancelJob(jobId);
            }
        }
    }

    @Nullable
    public MolecularAssemblerBlockEntity allocateAssembler(CraftingJob job) {
        if (job == null) {
            return null;
        }

        if (job.isProcessing() && tryExecuteOnExternalMachine(job)) {
            return null;
        }

        synchronized (this) {
            var existing = jobAssemblers.get(job.getId());
            if (existing != null) {
                return existing;
            }

            for (var assembler : assemblers) {
                if (assembler == null || assembler.isRemoved()) {
                    continue;
                }
                if (assembler.beginJob(job)) {
                    jobAssemblers.put(job.getId(), assembler);
                    return assembler;
                }
            }
        }

        return null;
    }

    private boolean tryExecuteOnExternalMachine(CraftingJob job) {
        var registry = ProcessingMachineRegistry.getInstance();
        var machine = registry.findAvailableMachine(job);
        if (machine.isEmpty()) {
            LOG.debug("No external machine registered for processing job {}; falling back to assembler.",
                    job.describeOutputs());
            return false;
        }

        LOG.debug("Attempting to route processing job {} to external machine {}", job.describeOutputs(),
                machine.get());

        var handled = ProcessingMachineExecutor.tryExecute(job, machine.get());
        if (handled) {
            jobMachines.put(job.getId(), machine.get());
        }
        if (!handled) {
            LOG.debug("Stub executor declined processing job {}; continuing with assembler pipeline.",
                    job.describeOutputs());
        }
        return handled;
    }

    public void releaseAssembler(UUID jobId) {
        if (jobId == null) {
            return;
        }
        var assembler = jobAssemblers.remove(jobId);
        if (assembler != null) {
            assembler.cancelJob(jobId);
        }
    }

    public void releaseMachine(UUID jobId) {
        if (jobId == null) {
            return;
        }

        var machine = jobMachines.remove(jobId);
        if (machine == null) {
            return;
        }

        var job = getJob(jobId);
        if (job != null) {
            try {
                machine.finishProcessing(job);
            } catch (Exception e) {
                LOG.debug("Error while releasing processing machine for job {}", jobId, e);
            }
        }
    }

    public boolean isAssemblerAssignedToJob(UUID jobId, MolecularAssemblerBlockEntity assembler) {
        if (jobId == null || assembler == null) {
            return false;
        }
        return jobAssemblers.get(jobId) == assembler;
    }

    public CraftingJob planJob(ItemStack patternStack) {
        CraftingJob job = CraftingJob.fromPattern(patternStack.copy());
        jobs.put(job.getId(), job);
        LOG.debug("Planned {} job {} (state={})", describeJobType(job), job.describeOutputs(), job.getState());
        return job;
    }

    public List<CraftingJob> activeJobs() {
        return List.copyOf(jobs.values());
    }

    public List<CraftingJob> completedJobs() {
        return List.copyOf(completedJobs.values());
    }

    public boolean reserveJob(CraftingJob job, CraftingCPUBlockEntity cpu) {
        jobs.putIfAbsent(job.getId(), job);

        CraftingCPUBlockEntity controller = cpu.getController();
        int requiredCapacity = estimateRequiredCapacity(job);
        int availableSlots = controller.getAvailableJobSlots();

        if (availableSlots <= 0) {
            LOG.info("Failed to reserve {} job {} on CPU at {} (parallel jobs in use: {}/{})",
                    describeJobType(job), job.describeOutputs(), controller.getBlockPos(),
                    controller.getActiveReservations().size(),
                    controller.getMaxParallelJobCount());
            return false;
        }

        boolean reserved = controller.reserveJob(job, requiredCapacity);
        if (reserved) {
            job.setState(CraftingJob.State.RESERVED);
            job.setTicksCompleted(0);
            reservations.put(job.getId(), new CraftingJobReservation(controller.getBlockPos(), requiredCapacity));
            LOG.info("Reserved {} job {} ({} units) on CPU at {}", describeJobType(job), job.describeOutputs(),
                    requiredCapacity,
                    controller.getBlockPos());
            LOG.debug("Job {} transitioned to {}", job.getId(), job.getState());
        } else {
            LOG.info("Failed to reserve {} job {} ({} units) on CPU at {} (available: {})",
                    describeJobType(job), job.describeOutputs(), requiredCapacity, controller.getBlockPos(),
                    controller.getAvailableCapacity());
        }

        return reserved;
    }

    public void jobExecutionStarted(CraftingJob job, CraftingCPUBlockEntity cpu) {
        jobs.putIfAbsent(job.getId(), job);
        job.setState(CraftingJob.State.RUNNING);
        LOG.debug("{} job {} started on CPU at {}", capitalize(describeJobType(job)), job.getId(),
                cpu.getBlockPos());
    }

    public void jobExecutionCompleted(CraftingJob job, CraftingCPUBlockEntity cpu) {
        job.setState(CraftingJob.State.COMPLETE);
        jobs.remove(job.getId());
        completedJobs.put(job.getId(), job);
        reservations.remove(job.getId());
        releaseAssembler(job.getId());
        releaseMachine(job.getId());
        int inserted = job.getInsertedOutputs();
        int dropped = job.getDroppedOutputs();
        if (dropped > 0) {
            LOG.warn("{} job {} completed on CPU at {} with {} items dropped ({} inserted).",
                    capitalize(describeJobType(job)), job.getId(), cpu.getBlockPos(), dropped, inserted);
        } else {
            LOG.info("{} job {} completed on CPU at {} ({} items inserted).",
                    capitalize(describeJobType(job)), job.getId(), cpu.getBlockPos(), inserted);
        }
        LOG.debug("Job {} transitioned to {}", job.getId(), job.getState());
    }

    public CraftingJob getJob(UUID jobId) {
        CraftingJob job = jobs.get(jobId);
        if (job != null) {
            return job;
        }
        return completedJobs.get(jobId);
    }

    public CraftingJobReservation getReservation(UUID jobId) {
        return reservations.get(jobId);
    }

    @Nullable
    public CraftingJob claimReservedJob(CraftingCPUBlockEntity cpu) {
        BlockPos pos = cpu.getBlockPos();
        for (var entry : reservations.entrySet()) {
            CraftingJobReservation reservation = entry.getValue();
            if (reservation.cpuPos().equals(pos)) {
                CraftingJob job = jobs.get(entry.getKey());
                if (job != null && job.getState() == CraftingJob.State.RESERVED) {
                    jobExecutionStarted(job, cpu);
                    return job;
                }
            }
        }
        return null;
    }

    private static int estimateRequiredCapacity(CraftingJob job) {
        int inputs = job.getInputs().stream().mapToInt(ItemStackView::count).sum();
        int outputs = job.getOutputs().stream().mapToInt(ItemStackView::count).sum();
        return Math.max(1, inputs + outputs);
    }

    private static String describeJobType(CraftingJob job) {
        return job.isProcessing() ? "processing" : "crafting";
    }

    private static String capitalize(String text) {
        if (text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    public record CraftingJobReservation(BlockPos cpuPos, int capacity) {
    }
}
