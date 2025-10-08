package appeng.capability;

//? if eval(current.version, ">=1.21.4") {
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.CapabilityManager;
import net.neoforged.neoforge.capabilities.CapabilityToken;
//? } else {
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
//? }

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.ICrankable;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.storage.MEStorage;

/**
 * Central definition point for AE2 specific capabilities.
 */
public final class AE2Capabilities {
    private AE2Capabilities() {
    }

    //? if eval(current.version, ">=1.21.4") {
    public static final CapabilityToken<MEStorage> ME_STORAGE_TOKEN = new CapabilityToken<>() {};
    public static final CapabilityToken<ICraftingMachine> CRAFTING_MACHINE_TOKEN = new CapabilityToken<>() {};
    public static final CapabilityToken<GenericInternalInventory> GENERIC_INTERNAL_INV_TOKEN =
            new CapabilityToken<>() {};
    public static final CapabilityToken<IInWorldGridNodeHost> IN_WORLD_GRID_NODE_HOST_TOKEN =
            new CapabilityToken<>() {};
    public static final CapabilityToken<ICrankable> CRANKABLE_TOKEN = new CapabilityToken<>() {};

    public static final Capability<MEStorage> ME_STORAGE = CapabilityManager.get(ME_STORAGE_TOKEN);
    public static final Capability<ICraftingMachine> CRAFTING_MACHINE =
            CapabilityManager.get(CRAFTING_MACHINE_TOKEN);
    public static final Capability<GenericInternalInventory> GENERIC_INTERNAL_INV =
            CapabilityManager.get(GENERIC_INTERNAL_INV_TOKEN);
    public static final Capability<IInWorldGridNodeHost> IN_WORLD_GRID_NODE_HOST =
            CapabilityManager.get(IN_WORLD_GRID_NODE_HOST_TOKEN);
    public static final Capability<ICrankable> CRANKABLE = CapabilityManager.get(CRANKABLE_TOKEN);
    //? } else {
    public static final Capability<MEStorage> ME_STORAGE =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<ICraftingMachine> CRAFTING_MACHINE =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<GenericInternalInventory> GENERIC_INTERNAL_INV =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<IInWorldGridNodeHost> IN_WORLD_GRID_NODE_HOST =
            CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<ICrankable> CRANKABLE =
            CapabilityManager.get(new CapabilityToken<>() {});
    //? }
}
