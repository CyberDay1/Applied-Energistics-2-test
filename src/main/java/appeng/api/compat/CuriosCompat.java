package appeng.api.compat;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;

import appeng.core.AE2InteropValidator;
import appeng.integration.modules.curios.CuriosIntegration;

/**
 * Helper for interacting with Curios without forcing a hard dependency.
 */
public final class CuriosCompat {
    public static final String MOD_ID = "curios";

    private CuriosCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static Optional<IItemHandler> getCuriosHandler(Player player) {
        if (!isLoaded()) {
            return Optional.empty();
        }

        return Optional.ofNullable(player.getCapability(CuriosIntegration.ITEM_HANDLER));
    }

    public static Optional<IItemHandler> getCuriosHandler(@Nullable Player player, int slot) {
        if (player == null) {
            return Optional.empty();
        }

        return getCuriosHandler(player).filter(handler -> slot >= 0 && slot < handler.getSlots());
    }

    public static EntityCapability<IItemHandler, Void> capability() {
        return CuriosIntegration.ITEM_HANDLER;
    }

    public static void reportBridgeInitialized() {
        if (isLoaded()) {
            AE2InteropValidator.markBridgeInitialized("Curios capability bridge");
        }
    }
}
