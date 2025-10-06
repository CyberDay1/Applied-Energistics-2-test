package appeng.interop;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.AE2Capabilities;
import appeng.api.compat.CuriosCompat;
import appeng.api.compat.JeiCompat;
import appeng.api.compat.ReiCompat;
import appeng.api.grid.IGridNode;
import appeng.api.storage.IStorageService;
import appeng.integration.modules.emi.AppEngEmiPlugin;

/**
 * Simple helpers for third-party integrations to query AE2 grid node capabilities.
 */
public final class AE2Interop {
    private AE2Interop() {
    }

    public static boolean hasGridNode(BlockEntity be) {
        return getGridNode(be) != null;
    }

    @Nullable
    public static IGridNode getGridNode(BlockEntity be) {
        return be.getCapability(AE2Capabilities.GRID_NODE, null);
    }

    public static boolean hasStorage(BlockEntity be) {
        return getStorage(be) != null;
    }

    @Nullable
    public static IStorageService getStorage(BlockEntity be) {
        return be.getCapability(AE2Capabilities.STORAGE_SERVICE, null);
    }

    public static InteropRegistrationResult registerCuriosCompat() {
        return attemptRegistration(CuriosCompat::register);
    }

    public static InteropRegistrationResult registerJeiCompat() {
        return attemptRegistration(JeiCompat::register);
    }

    public static InteropRegistrationResult registerReiCompat() {
        return attemptRegistration(ReiCompat::register);
    }

    public static InteropRegistrationResult registerEmiCompat() {
        return attemptRegistration(AppEngEmiPlugin::register);
    }

    private static InteropRegistrationResult attemptRegistration(Supplier<Boolean> registration) {
        try {
            boolean loaded = registration.get();
            if (loaded) {
                return InteropRegistrationResult.success();
            }
            return InteropRegistrationResult.skipped();
        } catch (Throwable t) {
            return InteropRegistrationResult.failure(t);
        }
    }

    public record InteropRegistrationResult(boolean success, boolean skipped, @Nullable Throwable error) {
        public static InteropRegistrationResult success() {
            return new InteropRegistrationResult(true, false, null);
        }

        public static InteropRegistrationResult skipped() {
            return new InteropRegistrationResult(false, true, null);
        }

        public static InteropRegistrationResult failure(Throwable error) {
            return new InteropRegistrationResult(false, false, error);
        }
    }
}
