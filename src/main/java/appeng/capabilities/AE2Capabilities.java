package appeng.capabilities;

//? if (>=1.21.4) {
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.CapabilityManager;
import net.neoforged.neoforge.capabilities.CapabilityToken;
//? } else {
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
//? }

/**
 * Capability references that need to work across the Forge ⇆ NeoForge API drift.
 */
public final class AE2Capabilities {
    //? if (>=1.21.4) {
    public static final CapabilityToken<Object> GRID_HOST_TOKEN = new CapabilityToken<>() {};
    public static final Capability<Object> GRID_HOST = CapabilityManager.get(GRID_HOST_TOKEN);
    //? } else {
    public static final Capability<Object> GRID_HOST =
            CapabilityManager.get(new CapabilityToken<>() {});
    //? }

    private AE2Capabilities() {}
}
