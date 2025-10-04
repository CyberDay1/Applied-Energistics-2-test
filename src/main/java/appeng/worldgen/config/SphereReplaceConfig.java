package appeng.worldgen.config;

import java.util.List;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;

/**
 * Placeholder configuration translating a spherical replace request into the
 * vanilla disk feature configuration. This will be replaced with a dedicated
 * meteorite feature once available.
 */
public class SphereReplaceConfig extends DiskConfiguration {
    public SphereReplaceConfig(BlockState state, int minRadius, int maxRadius) {
        super(SimpleStateProvider.simple(state), UniformInt.of(minRadius, maxRadius), maxRadius,
            List.of(Blocks.STONE.defaultBlockState(), Blocks.DEEPSLATE.defaultBlockState()));
    }
}
