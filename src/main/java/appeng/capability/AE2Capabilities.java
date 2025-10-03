package appeng.capability;

import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.CapabilityToken;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.storage.MEStorage;

/**
 * Central definition point for AE2 specific capabilities.
 * <p>
 * Capabilities are obtained through NeoForge's {@link CapabilityToken} pattern to ensure that instances are resolved
 * lazily and stay compatible across reloads.
 * </p>
 */
public final class AE2Capabilities {
    private AE2Capabilities() {
    }

    public static final Capability<MEStorage> ME_STORAGE = new CapabilityToken<>() {
    };

    public static final Capability<ICraftingMachine> CRAFTING_MACHINE = new CapabilityToken<>() {
    };

    public static final Capability<GenericInternalInventory> GENERIC_INTERNAL_INV = new CapabilityToken<>() {
    };

    public static final Capability<IInWorldGridNodeHost> IN_WORLD_GRID_NODE_HOST = new CapabilityToken<>() {
    };

    public static final Capability<ICrankable> CRANKABLE = new CapabilityToken<>() {
    };
}
