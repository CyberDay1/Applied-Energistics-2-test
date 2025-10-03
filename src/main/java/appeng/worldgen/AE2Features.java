package appeng.worldgen;

import appeng.core.AppEng;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public final class AE2Features {
    private AE2Features() {
    }

    public static final ResourceKey<ConfiguredFeature<?, ?>> CERTUS_ORE_CONFIG = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, new ResourceLocation(AppEng.MOD_ID, "certus_ore"));
    public static final ResourceKey<PlacedFeature> CERTUS_ORE_PLACED = ResourceKey.create(Registries.PLACED_FEATURE,
            new ResourceLocation(AppEng.MOD_ID, "certus_ore"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> METEORITE_CONFIG = ResourceKey.create(
            Registries.CONFIGURED_FEATURE, new ResourceLocation(AppEng.MOD_ID, "meteorite"));
    public static final ResourceKey<PlacedFeature> METEORITE_PLACED = ResourceKey.create(Registries.PLACED_FEATURE,
            new ResourceLocation(AppEng.MOD_ID, "meteorite"));

    public static List<PlacementModifier> orePlacement(int veinsPerChunk, int minY, int maxY) {
        return List.of(CountPlacement.of(veinsPerChunk), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(minY), VerticalAnchor.absolute(maxY)),
                BiomeFilter.biome());
    }
}
