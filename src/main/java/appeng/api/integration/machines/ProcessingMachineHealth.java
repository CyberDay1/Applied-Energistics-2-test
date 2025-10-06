package appeng.api.integration.machines;

import net.minecraft.network.chat.Component;

/**
 * Represents the health state of an external processing executor.
 */
public record ProcessingMachineHealth(boolean healthy, Component diagnostic) {
    public static ProcessingMachineHealth healthy() {
        return new ProcessingMachineHealth(true, Component.empty());
    }

    public static ProcessingMachineHealth offline(Component diagnostic) {
        return new ProcessingMachineHealth(false, diagnostic == null ? Component.empty() : diagnostic);
    }

    public boolean isHealthy() {
        return healthy;
    }
}
