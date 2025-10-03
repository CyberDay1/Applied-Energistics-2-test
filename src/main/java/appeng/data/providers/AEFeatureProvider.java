package appeng.data.providers;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.worldgen.AE2Features;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class AEFeatureProvider extends net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider {
    public AEFeatureProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, createBuilder(), Set.of(AppEng.MOD_ID));
    }

    private static RegistrySetBuilder createBuilder() {
        return new RegistrySetBuilder().add(Registries.CONFIGURED_FEATURE, AEFeatureProvider::registerConfiguredFeatures)
                .add(Registries.PLACED_FEATURE, AEFeatureProvider::registerPlacedFeatures);
    }

    private static void registerConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        var certusConfig = new OreConfiguration(
                List.of(OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES,
                        AEBlocks.QUARTZ_BLOCK.block().defaultBlockState())),
                8);
        registerConfiguredFeature(context, AE2Features.CERTUS_ORE_CONFIG, Feature.ORE, certusConfig);

        registerConfiguredFeature(context, AE2Features.METEORITE_CONFIG, Feature.NO_OP,
                NoneFeatureConfiguration.INSTANCE);
    }

    private static void registerPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        var configured = context.lookup(Registries.CONFIGURED_FEATURE);

        registerPlacedFeature(context, configured, AE2Features.CERTUS_ORE_PLACED, AE2Features.CERTUS_ORE_CONFIG,
                AE2Features.orePlacement(10, -32, 64));

        registerPlacedFeature(context, configured, AE2Features.METEORITE_PLACED, AE2Features.METEORITE_CONFIG,
                List.<PlacementModifier>of());
    }

    private static <FC extends net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration> void registerConfiguredFeature(
            BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key,
            Feature<FC> feature, FC config) {
        context.register(key, new ConfiguredFeature<>(feature, config));
    }

    private static void registerPlacedFeature(BootstrapContext<PlacedFeature> context,
            HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures, ResourceKey<PlacedFeature> placedKey,
            ResourceKey<ConfiguredFeature<?, ?>> configuredKey, List<PlacementModifier> modifiers) {
        var configuredFeature = configuredFeatures.getOrThrow(configuredKey);
        context.register(placedKey, new PlacedFeature(configuredFeature, List.copyOf(modifiers)));
    }
}
