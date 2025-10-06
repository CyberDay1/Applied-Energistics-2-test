package appeng.integration.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bootstraps the external processing machine integration stub. For now this just logs activation and ensures the
 * registry is constructed so other systems can register machines.
 */
public final class ProcessingIntegration {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingIntegration.class);

    private ProcessingIntegration() {
    }

    public static void init() {
        LOG.debug("External processing machine integration stubs initialized.");
        ProcessingMachineRegistry.getInstance();
    }
}
