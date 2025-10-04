package appeng.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import appeng.AE2Registries;

public final class AE2Features {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CERTUS_QUARTZ_ORE =
        ResourceKey.create(Registries.CONFIGURED_FEATURE,
            new ResourceLocation(AE2Registries.MODID, "certus_quartz_ore"));

    public static final ResourceKey<PlacedFeature> CERTUS_QUARTZ_ORE_PLACED =
        ResourceKey.create(Registries.PLACED_FEATURE,
            new ResourceLocation(AE2Registries.MODID, "certus_quartz_ore"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> METEORITE =
        ResourceKey.create(Registries.CONFIGURED_FEATURE,
            new ResourceLocation(AE2Registries.MODID, "meteorite"));

    public static final ResourceKey<PlacedFeature> METEORITE_PLACED =
        ResourceKey.create(Registries.PLACED_FEATURE,
            new ResourceLocation(AE2Registries.MODID, "meteorite"));

    private AE2Features() {}
}
