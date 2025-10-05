package appeng.crafting;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<UUID, CraftingJobReservation> reservations = new ConcurrentHashMap<>();

    private CraftingJobManager() {
    }

    public static CraftingJobManager getInstance() {
        return INSTANCE;
    }

    public CraftingJob planJob(ItemStack patternStack) {
        CraftingJob job = CraftingJob.fromPattern(patternStack.copy());
        jobs.put(job.getId(), job);
        return job;
    }

    public List<CraftingJob> activeJobs() {
        return List.copyOf(jobs.values());
    }

    public boolean reserveJob(CraftingJob job, CraftingCPUBlockEntity cpu) {
        jobs.putIfAbsent(job.getId(), job);

        CraftingCPUBlockEntity controller = cpu.getController();
        int requiredCapacity = estimateRequiredCapacity(job);

        boolean reserved = controller.reserveJob(job, requiredCapacity);
        if (reserved) {
            reservations.put(job.getId(), new CraftingJobReservation(controller.getBlockPos(), requiredCapacity));
            LOG.info("Reserved crafting job {} ({} units) on CPU at {}", job.describeOutputs(), requiredCapacity,
                    controller.getBlockPos());
        } else {
            LOG.info("Failed to reserve crafting job {} ({} units) on CPU at {} (available: {})",
                    job.describeOutputs(), requiredCapacity, controller.getBlockPos(),
                    controller.getAvailableCapacity());
        }

        return reserved;
    }

    private static int estimateRequiredCapacity(CraftingJob job) {
        int inputs = job.getInputs().stream().mapToInt(ItemStackView::count).sum();
        int outputs = job.getOutputs().stream().mapToInt(ItemStackView::count).sum();
        return Math.max(1, inputs + outputs);
    }

    private record CraftingJobReservation(BlockPos cpuPos, int capacity) {
    }
}
