//? >=1.21.4 {
package appeng.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import appeng.core.AppEng;
import appeng.registry.AE2Blocks;
import appeng.worldgen.AE2Features;
import appeng.worldgen.config.MeteoriteConfig;
import appeng.worldgen.feature.AE2FeatureTypes;

public class AE2WorldgenProvider extends DatapackBuiltinEntriesProvider {
    public AE2WorldgenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, createBuilder(), Set.of(AppEng.MOD_ID));
    }

    private static RegistrySetBuilder createBuilder() {
        return new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, AE2WorldgenProvider::bootstrapConfiguredFeatures)
            .add(Registries.PLACED_FEATURE, AE2WorldgenProvider::bootstrapPlacedFeatures);
    }

    private static void bootstrapConfiguredFeatures(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceable = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);

        OreConfiguration certusConfig = new OreConfiguration(stoneReplaceable,
            AE2Blocks.CERTUS_QUARTZ_ORE.get().defaultBlockState(), 8);
        context.register(AE2Features.CERTUS_QUARTZ_ORE, new ConfiguredFeature<>(Feature.ORE, certusConfig));

        MeteoriteConfig meteoriteConfig = new MeteoriteConfig(
            SimpleStateProvider.simple(AE2Blocks.SKY_STONE.get().defaultBlockState()),
            SimpleStateProvider.simple(AE2Blocks.SKY_STONE_CHEST.get().defaultBlockState()),
            UniformInt.of(6, 14),
            2.5f);
        context.register(AE2Features.METEORITE, new ConfiguredFeature<>(AE2FeatureTypes.METEORITE.get(), meteoriteConfig));
    }

    private static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        List<PlacementModifier> certusPlacement = List.of(
            CountPlacement.of(8),
            InSquarePlacement.spread(),
            HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(64))
        );

        var configured = context.lookup(Registries.CONFIGURED_FEATURE);
        var certus = configured.getOrThrow(AE2Features.CERTUS_QUARTZ_ORE);
        context.register(AE2Features.CERTUS_QUARTZ_ORE_PLACED, new PlacedFeature(certus, certusPlacement));

        List<PlacementModifier> meteoritePlacement = List.of(
            RarityFilter.onAverageOnceEvery(1200),
            InSquarePlacement.spread(),
            HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES),
            BiomeFilter.biome());
        var meteorite = configured.getOrThrow(AE2Features.METEORITE);
        context.register(AE2Features.METEORITE_PLACED, new PlacedFeature(meteorite, meteoritePlacement));
    }
}
//?}
