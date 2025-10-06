package appeng.integration.processing;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import appeng.api.integration.machines.IProcessingMachine;
import appeng.crafting.CraftingJob;

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
        }
    }

    public void unregister(IProcessingMachine machine) {
        if (machine != null) {
            machines.remove(machine);
        }
    }

    public Optional<IProcessingMachine> findAvailableMachine(CraftingJob job) {
        if (job == null) {
            return Optional.empty();
        }

        for (var machine : machines) {
            if (machine != null && machine.canProcess(job)) {
                return Optional.of(machine);
            }
        }

        return Optional.empty();
    }
}
