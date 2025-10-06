package appeng.integration.compat;

import net.minecraft.network.chat.Component;

import appeng.core.AELog;
import appeng.interop.AE2Interop;
import appeng.interop.AE2Interop.InteropRegistrationResult;

public final class InteropBootstrap {
    private InteropBootstrap() {
    }

    public static void registerInterop() {
        logResult("Curios", AE2Interop.registerCuriosCompat());
        logResult("JEI", AE2Interop.registerJeiCompat());
        logResult("REI", AE2Interop.registerReiCompat());
        logResult("EMI", AE2Interop.registerEmiCompat());
    }

    private static void logResult(String integrationName, InteropRegistrationResult result) {
        if (result.success()) {
            AELog.info(Component
                    .translatable("log.appliedenergistics2.interop.success", integrationName)
                    .getString());
        } else if (result.skipped()) {
            AELog.info(Component
                    .translatable("log.appliedenergistics2.interop.skipped", integrationName)
                    .getString());
        } else {
            var message = Component
                    .translatable("log.appliedenergistics2.interop.failed", integrationName,
                            result.error() != null ? result.error().getMessage() : "unknown")
                    .getString();
            if (result.error() != null) {
                AELog.warn(result.error(), message);
            } else {
                AELog.warn(message);
            }
        }
    }
}
