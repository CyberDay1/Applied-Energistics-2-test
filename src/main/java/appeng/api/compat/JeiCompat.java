package appeng.api.compat;

import net.neoforged.fml.ModList;

import appeng.core.AE2InteropValidator;

public final class JeiCompat {
    public static final String MOD_ID = "jei";

    private JeiCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void reportBridgeInitialized() {
        if (isLoaded()) {
            AE2InteropValidator.markBridgeInitialized("JEI plugin bridge");
        }
    }
}
