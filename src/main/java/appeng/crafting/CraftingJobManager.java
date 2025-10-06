package appeng.crafting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
    private final PriorityQueue<CraftingJob> processingQueue = new PriorityQueue<>(
            Comparator.comparingInt((CraftingJob job) -> job.getPriority().weight()).reversed()
                    .thenComparing(CraftingJob::getId));
    private final ProcessingExecutorSchedulingPolicy executorSchedulingPolicy =
            new RoundRobinProcessingExecutorSchedulingPolicy();

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

        if (job.isProcessing()) {
            var result = dispatchProcessingJob(job);
            if (result != ProcessingDispatchResult.FALLBACK) {
                return null;
            }
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

    private ProcessingDispatchResult dispatchProcessingJob(CraftingJob job) {
        synchronized (processingQueue) {
            processingQueue.remove(job);
            processingQueue.add(job);
            LOG.debug("Queued processing job {} (priority={}) for external executor evaluation", job.getId(),
                    job.getPriority());
            var result = drainProcessingQueue(job);
            if (result == ProcessingDispatchResult.HANDLED) {
                drainProcessingQueue(null);
            }
            return result;
        }
    }

    private ProcessingDispatchResult drainProcessingQueue(@Nullable CraftingJob target) {
        while (!processingQueue.isEmpty()) {
            CraftingJob head = processingQueue.peek();
            var result = attemptScheduleCandidate(head);
            if (head == target) {
                return result;
            }
            if (result == ProcessingDispatchResult.QUEUED) {
                return target == null ? ProcessingDispatchResult.HANDLED : ProcessingDispatchResult.QUEUED;
            }
        }

        return target == null ? ProcessingDispatchResult.HANDLED : ProcessingDispatchResult.FALLBACK;
    }

    private ProcessingDispatchResult attemptScheduleCandidate(CraftingJob job) {
        var registry = ProcessingMachineRegistry.getInstance();
        var machines = new ArrayList<IProcessingMachine>();
        for (var machine : registry.findMachinesForJob(job)) {
            if (machine == null) {
                continue;
            }
            if (!machine.isHealthy()) {
                LOG.info(Component
                        .translatable("message.appliedenergistics2.processing_job.executor_offline_fallback",
                                job.describeOutputs(), machine, machine.getExecutorTypeId())
                        .getString());
                continue;
            }
            machines.add(machine);
        }

        if (machines.isEmpty()) {
            processingQueue.poll();
            LOG.debug(Component
                    .translatable("message.appliedenergistics2.processing_job.external_fallback",
                            job.describeOutputs())
                    .getString());
            return ProcessingDispatchResult.FALLBACK;
        }

        var attempted = new ArrayList<IProcessingMachine>();
        while (!machines.isEmpty()) {
            var snapshot = buildExecutorPools(machines);
            for (var saturated : snapshot.saturatedExecutors()) {
                LOG.debug(Component
                        .translatable("message.appliedenergistics2.processing_job.executor_at_capacity",
                                saturated.executorType(), saturated.machine(), saturated.activeJobs(),
                                formatCapacity(saturated.capacity()))
                        .getString());
            }

            var selection = executorSchedulingPolicy.select(job, snapshot.availableExecutors());
            if (selection.isEmpty()) {
                LOG.debug("Processing job {} (priority={}) waiting for executor capacity", job.getId(),
                        job.getPriority());
                return ProcessingDispatchResult.QUEUED;
            }

            var selectionResult = selection.get();
            var machine = selectionResult.executor();
            attempted.add(machine);

            int plannedActive = machine.getActiveJobCount() + 1;
            var capacity = formatCapacity(machine.getCapacity());

            LOG.debug(Component
                    .translatable("message.appliedenergistics2.processing_job.job_scheduled_on_executor",
                            job.describeOutputs(), machine, selectionResult.executorType(), plannedActive, capacity)
                    .getString());

            LOG.debug("Routing processing job {} (priority={}) to external machine {}", job.getId(),
                    job.getPriority(), machine);

            boolean handled = ProcessingMachineExecutor.tryExecute(job, machine);
            if (handled) {
                jobMachines.put(job.getId(), machine);
                processingQueue.poll();
                if (job.getPriority() == CraftingJob.Priority.HIGH) {
                    LOG.info(Component
                            .translatable("message.appliedenergistics2.processing_job.high_priority_scheduled",
                                    job.describeOutputs(), machine, selectionResult.executorType())
                            .getString());
                }
                return ProcessingDispatchResult.HANDLED;
            }

            var health = machine.getHealth();
            if (health.isHealthy()) {
                LOG.info(Component
                        .translatable("message.appliedenergistics2.processing_job.executor_failed_reroute",
                                job.describeOutputs(), machine, selectionResult.executorType())
                        .getString());
            } else {
                LOG.info(Component
                        .translatable("message.appliedenergistics2.processing_job.executor_offline_fallback",
                                job.describeOutputs(), machine, selectionResult.executorType())
                        .getString());
            }

            machines.removeIf(attempted::contains);
        }

        processingQueue.poll();
        LOG.debug(Component
                .translatable("message.appliedenergistics2.processing_job.external_fallback", job.describeOutputs())
                .getString());
        return ProcessingDispatchResult.FALLBACK;
    }

    private ExecutorPoolSnapshot buildExecutorPools(List<IProcessingMachine> machines) {
        Map<String, List<IProcessingMachine>> pools = new LinkedHashMap<>();
        List<ExecutorAvailability> saturated = new ArrayList<>();
        for (var machine : machines) {
            if (machine == null) {
                continue;
            }

            var type = machine.getExecutorTypeId();
            int activeJobs = machine.getActiveJobCount();
            var capacity = machine.getCapacity();

            if (!machine.hasCapacity()) {
                saturated.add(new ExecutorAvailability(type, machine, activeJobs, capacity));
                continue;
            }

            pools.computeIfAbsent(type, key -> new ArrayList<>()).add(machine);
        }
        return new ExecutorPoolSnapshot(pools, saturated);
    }

    private static String formatCapacity(OptionalInt capacity) {
        return capacity.isPresent() ? Integer.toString(capacity.getAsInt()) : "unbounded";
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

        dispatchQueuedJobs();
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
        synchronized (processingQueue) {
            processingQueue.remove(job);
        }
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

    private record ExecutorAvailability(String executorType, IProcessingMachine machine, int activeJobs,
            OptionalInt capacity) {
    }

    private record ExecutorPoolSnapshot(Map<String, List<IProcessingMachine>> availableExecutors,
            List<ExecutorAvailability> saturatedExecutors) {
    }

    private interface ProcessingExecutorSchedulingPolicy {
        Optional<Selection> select(CraftingJob job, Map<String, List<IProcessingMachine>> executorPools);

        record Selection(String executorType, IProcessingMachine executor) {
        }
    }

    private static final class RoundRobinProcessingExecutorSchedulingPolicy
            implements ProcessingExecutorSchedulingPolicy {
        private final ConcurrentMap<String, AtomicInteger> rotation = new ConcurrentHashMap<>();

        @Override
        public Optional<Selection> select(CraftingJob job, Map<String, List<IProcessingMachine>> executorPools) {
            for (var entry : executorPools.entrySet()) {
                var executors = entry.getValue();
                if (!executors.isEmpty()) {
                    var index = rotation.computeIfAbsent(entry.getKey(), key -> new AtomicInteger());
                    int next = Math.floorMod(index.getAndIncrement(), executors.size());
                    return Optional.of(new Selection(entry.getKey(), executors.get(next)));
                }
            }
            return Optional.empty();
        }
    }

    private enum ProcessingDispatchResult {
        HANDLED,
        QUEUED,
        FALLBACK
    }

    private void dispatchQueuedJobs() {
        synchronized (processingQueue) {
            drainProcessingQueue(null);
        }
    }

    public void notifyProcessingCapacityChanged() {
        dispatchQueuedJobs();
    }

    public record CraftingJobReservation(BlockPos cpuPos, int capacity) {
    }
}
