package appeng.datagen;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;

import appeng.AE2Registries;
import appeng.worldgen.AE2Features;

public class AE2BiomeModifierProvider extends DatapackBuiltinEntriesProvider {
    private static final ResourceKey<BiomeModifier> ADD_CERTUS_QUARTZ = ResourceKey.create(
        Registries.BIOME_MODIFIER, new ResourceLocation(AE2Registries.MODID, "add_certus_quartz_ore"));
    private static final ResourceKey<BiomeModifier> ADD_METEORITES = ResourceKey.create(
        Registries.BIOME_MODIFIER, new ResourceLocation(AE2Registries.MODID, "add_meteorites"));

    public AE2BiomeModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, createBuilder(), Set.of(AE2Registries.MODID));
    }

    private static RegistrySetBuilder createBuilder() {
        return new RegistrySetBuilder().add(Registries.BIOME_MODIFIER, AE2BiomeModifierProvider::bootstrap);
    }

    private static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var biomes = context.lookup(Registries.BIOME);
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);

        var overworld = biomes.getOrThrow(ResourceKey.create(Registries.BIOME,
            new ResourceLocation("minecraft", "overworld")));
        var certusOre = placedFeatures.getOrThrow(AE2Features.CERTUS_QUARTZ_ORE_PLACED);

        context.register(ADD_CERTUS_QUARTZ, new BiomeModifiers.AddFeaturesBiomeModifier(
            HolderSet.direct(overworld),
            HolderSet.direct(certusOre),
            GenerationStep.Decoration.UNDERGROUND_ORES));

        var meteoriteFeature = placedFeatures.getOrThrow(AE2Features.METEORITE_PLACED);
        context.register(ADD_METEORITES, new BiomeModifiers.AddFeaturesBiomeModifier(
            HolderSet.direct(overworld),
            HolderSet.direct(meteoriteFeature),
            GenerationStep.Decoration.SURFACE_STRUCTURES));
    }
}
