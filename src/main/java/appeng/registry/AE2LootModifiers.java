//? if eval(current.version, ">=1.21.4") {
package appeng.registry;

import com.mojang.serialization.Codec;

import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.RegistryObject;

import appeng.loot.AE2CertusLootModifier;
import appeng.loot.AE2PressLootModifier;

public final class AE2LootModifiers {
    private AE2LootModifiers() {
    }

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> PRESS_LOOT =
            AE2Registries.LOOT_MODIFIERS.register("add_presses", () -> AE2PressLootModifier.CODEC.codec());

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> CERTUS_EXTRA_DROPS =
            AE2Registries.LOOT_MODIFIERS.register("certus_extra_drops", () -> AE2CertusLootModifier.CODEC.codec());

    public static void init() {
        // Intentionally empty to force the class to load and register modifiers.
    }
}
//? }
