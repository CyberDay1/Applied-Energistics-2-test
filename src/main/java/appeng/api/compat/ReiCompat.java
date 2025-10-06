package appeng.api.compat;

import net.neoforged.fml.ModList;

import appeng.core.AE2InteropValidator;

public final class ReiCompat {
    public static final String MOD_ID = "roughlyenoughitems";

    private ReiCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID) || ModList.get().isLoaded("rei");
    }

    public static boolean register() {
        if (!isLoaded()) {
            return false;
        }

        reportBridgeInitialized();
        return true;
    }

    public static void reportBridgeInitialized() {
        if (isLoaded()) {
            AE2InteropValidator.markBridgeInitialized("REI client bridge");
        }
    }
}
