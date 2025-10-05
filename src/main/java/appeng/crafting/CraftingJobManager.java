package appeng.crafting;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import appeng.blockentity.crafting.CraftingCPUBlockEntity;
import appeng.api.storage.ItemStackView;

/**
 * Tracks planned crafting jobs server-side.
 */
public final class CraftingJobManager {
    private static final CraftingJobManager INSTANCE = new CraftingJobManager();

    private static final Logger LOG = LoggerFactory.getLogger(CraftingJobManager.class);

    private final Map<UUID, CraftingJob> jobs = new ConcurrentHashMap<>();
    private final Map<UUID, CraftingJob> completedJobs = new ConcurrentHashMap<>();
    private final Map<UUID, CraftingJobReservation> reservations = new ConcurrentHashMap<>();

    private CraftingJobManager() {
    }

    public static CraftingJobManager getInstance() {
        return INSTANCE;
    }

    public CraftingJob planJob(ItemStack patternStack) {
        CraftingJob job = CraftingJob.fromPattern(patternStack.copy());
        jobs.put(job.getId(), job);
        LOG.debug("Planned crafting job {} (state={})", job.describeOutputs(), job.getState());
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
            LOG.info("Failed to reserve crafting job {} on CPU at {} (parallel jobs in use: {}/{})", job.describeOutputs(),
                    controller.getBlockPos(), controller.getActiveReservations().size(),
                    controller.getMaxParallelJobCount());
            return false;
        }

        boolean reserved = controller.reserveJob(job, requiredCapacity);
        if (reserved) {
            job.setState(CraftingJob.State.RESERVED);
            job.setTicksCompleted(0);
            reservations.put(job.getId(), new CraftingJobReservation(controller.getBlockPos(), requiredCapacity));
            LOG.info("Reserved crafting job {} ({} units) on CPU at {}", job.describeOutputs(), requiredCapacity,
                    controller.getBlockPos());
            LOG.debug("Job {} transitioned to {}", job.getId(), job.getState());
        } else {
            LOG.info("Failed to reserve crafting job {} ({} units) on CPU at {} (available: {})",
                    job.describeOutputs(), requiredCapacity, controller.getBlockPos(),
                    controller.getAvailableCapacity());
        }

        return reserved;
    }

    public void jobExecutionStarted(CraftingJob job, CraftingCPUBlockEntity cpu) {
        jobs.putIfAbsent(job.getId(), job);
        job.setState(CraftingJob.State.RUNNING);
        LOG.debug("Job {} started on CPU at {}", job.getId(), cpu.getBlockPos());
    }

    public void jobExecutionCompleted(CraftingJob job, CraftingCPUBlockEntity cpu) {
        job.setState(CraftingJob.State.COMPLETE);
        jobs.remove(job.getId());
        completedJobs.put(job.getId(), job);
        reservations.remove(job.getId());
        int inserted = job.getInsertedOutputs();
        int dropped = job.getDroppedOutputs();
        if (dropped > 0) {
            LOG.warn("Job {} completed on CPU at {} with {} items dropped ({} inserted).", job.getId(),
                    cpu.getBlockPos(), dropped, inserted);
        } else {
            LOG.info("Job {} completed on CPU at {} ({} items inserted).", job.getId(), cpu.getBlockPos(), inserted);
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

    public record CraftingJobReservation(BlockPos cpuPos, int capacity) {
    }
}
