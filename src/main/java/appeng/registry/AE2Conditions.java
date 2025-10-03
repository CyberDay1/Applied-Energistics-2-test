package appeng.registry;

import com.mojang.serialization.MapCodec;

import net.neoforged.neoforge.registries.RegistryObject;

import appeng.recipes.conditions.HasItemCondition;

public final class AE2Conditions {
    private AE2Conditions() {
    }

    public static final RegistryObject<MapCodec<HasItemCondition>> HAS_ITEM =
            AE2Registries.CONDITIONS.register("has_item", () -> HasItemCondition.CODEC);

    public static void init() {
        // Ensure class loading
    }
}
