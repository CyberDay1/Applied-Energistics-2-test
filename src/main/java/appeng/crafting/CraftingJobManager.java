package appeng.crafting;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.item.ItemStack;

/**
 * Tracks planned crafting jobs server-side.
 */
public final class CraftingJobManager {
    private static final CraftingJobManager INSTANCE = new CraftingJobManager();

    private final Map<UUID, CraftingJob> jobs = new ConcurrentHashMap<>();

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
}
