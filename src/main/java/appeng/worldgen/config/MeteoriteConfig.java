package appeng.worldgen.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

/**
 * Configuration for the {@link appeng.worldgen.feature.MeteoriteFeature}.
 *
 * @param meteoriteBlock The block used for the meteorite shell.
 * @param chestBlock     The block used for the meteorite loot chest.
 * @param radius         Radius provider for the generated meteorite sphere.
 * @param surfaceJitter  Amount of random jitter applied to the meteorite surface.
 */
public record MeteoriteConfig(BlockStateProvider meteoriteBlock, BlockStateProvider chestBlock,
        IntProvider radius, float surfaceJitter) implements FeatureConfiguration {

    public static final Codec<MeteoriteConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("meteorite_block").forGetter(MeteoriteConfig::meteoriteBlock),
            BlockStateProvider.CODEC.fieldOf("chest_block").forGetter(MeteoriteConfig::chestBlock),
            IntProvider.POSITIVE_CODEC.fieldOf("radius").forGetter(MeteoriteConfig::radius),
            Codec.FLOAT.optionalFieldOf("surface_jitter", 2.5f).forGetter(MeteoriteConfig::surfaceJitter))
            .apply(instance, MeteoriteConfig::new));
}
