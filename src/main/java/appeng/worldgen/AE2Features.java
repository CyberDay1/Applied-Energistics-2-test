package appeng.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import appeng.core.AppEng;

public final class AE2Features {
    //? if eval(current.version, ">=1.21.4") {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CERTUS_QUARTZ_ORE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, new ResourceLocation(AppEng.MOD_ID, "certus_quartz_ore"));

    public static final ResourceKey<PlacedFeature> CERTUS_QUARTZ_ORE_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE, new ResourceLocation(AppEng.MOD_ID, "certus_quartz_ore"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> METEORITE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, new ResourceLocation(AppEng.MOD_ID, "meteorite"));

    public static final ResourceKey<PlacedFeature> METEORITE_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE, new ResourceLocation(AppEng.MOD_ID, "meteorite"));
    //? } else {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CERTUS_QUARTZ_ORE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, new ResourceLocation(AppEng.MOD_ID, "certus_quartz_ore"));

    public static final ResourceKey<PlacedFeature> CERTUS_QUARTZ_ORE_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE, new ResourceLocation(AppEng.MOD_ID, "certus_quartz_ore"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> METEORITE = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, new ResourceLocation(AppEng.MOD_ID, "meteorite"));

    public static final ResourceKey<PlacedFeature> METEORITE_PLACED = ResourceKey.create(
            Registries.PLACED_FEATURE, new ResourceLocation(AppEng.MOD_ID, "meteorite"));
    //? }

    private AE2Features() {}
}
