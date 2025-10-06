package appeng.integration.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import appeng.api.integration.machines.IProcessingMachine;
import appeng.crafting.CraftingJob;
import appeng.crafting.CraftingJobManager;

/**
 * Tracks processing machines discovered by IO buses.
 */
public final class ProcessingMachineRegistry {
    private static final ProcessingMachineRegistry INSTANCE = new ProcessingMachineRegistry();

    private final Set<IProcessingMachine> machines = ConcurrentHashMap.newKeySet();

    private ProcessingMachineRegistry() {
    }

    public static ProcessingMachineRegistry getInstance() {
        return INSTANCE;
    }

    public void register(IProcessingMachine machine) {
        if (machine != null) {
            machines.add(machine);
            CraftingJobManager.getInstance().notifyProcessingCapacityChanged();
        }
    }

    public void unregister(IProcessingMachine machine) {
        if (machine != null) {
            machines.remove(machine);
            CraftingJobManager.getInstance().notifyProcessingCapacityChanged();
        }
    }

    public List<IProcessingMachine> findMachinesForJob(CraftingJob job) {
        if (job == null) {
            return List.of();
        }

        List<IProcessingMachine> candidates = new ArrayList<>();
        for (var machine : machines) {
            if (machine != null && machine.canProcess(job)) {
                candidates.add(machine);
            }
        }

        return candidates;
    }

    public Optional<IProcessingMachine> findAvailableMachine(CraftingJob job) {
        return findMachinesForJob(job).stream().findFirst();
    }
}
