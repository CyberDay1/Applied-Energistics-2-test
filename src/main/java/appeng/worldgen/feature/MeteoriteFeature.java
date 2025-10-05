package appeng.worldgen.feature;

import java.util.EnumSet;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.FluidState;

import appeng.core.AppEng;
import appeng.worldgen.config.MeteoriteConfig;

/**
 * Generates a rough spherical meteorite with a loot chest embedded inside.
 */
public class MeteoriteFeature extends Feature<MeteoriteConfig> {
    private static final EnumSet<Heightmap.Types> SURFACE_HEIGHTMAPS = EnumSet.of(
            Heightmap.Types.WORLD_SURFACE_WG,
            Heightmap.Types.MOTION_BLOCKING,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES);

    private static final ResourceLocation LOOT_TABLE = AppEng.makeId("chests/meteorite");

    public MeteoriteFeature(Codec<MeteoriteConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<MeteoriteConfig> context) {
        var level = context.level();
        var random = context.random();
        var origin = context.origin();
        var config = context.config();

        int surfaceY = sampleSurfaceHeight(level, origin);
        if (surfaceY <= level.getMinBuildHeight()) {
            return false;
        }

        int radius = config.radius().sample(random);
        if (radius < 1) {
            return false;
        }

        int buryDepth = Math.max(2, radius / 2 + random.nextInt(Math.max(1, radius / 3)));
        int centerY = surfaceY - buryDepth;
        int minY = level.getMinBuildHeight() + 4;
        if (centerY < minY) {
            centerY = minY;
        }

        BlockPos center = new BlockPos(origin.getX(), centerY, origin.getZ());
        BlockState meteoriteState = config.meteoriteBlock().getState(random, center);

        var mutablePos = new BlockPos.MutableBlockPos();
        boolean placedAny = false;

        int radiusSq = radius * radius;
        int innerRadiusSq = Math.max(1, (radius - 2) * (radius - 2));

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    int distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > radiusSq) {
                        continue;
                    }

                    mutablePos.setWithOffset(center, dx, dy, dz);
                    if (!level.getWorldBorder().isWithinBounds(mutablePos) || !withinBuildHeight(level, mutablePos)) {
                        continue;
                    }

                    if (!canReplace(level, mutablePos)) {
                        continue;
                    }

                    boolean placeBlock = distSq <= innerRadiusSq;
                    if (!placeBlock) {
                        double distance = Math.sqrt(distSq);
                        placeBlock = shouldPlaceWithJitter(mutablePos, distance, radius, config.surfaceJitter());
                    }

                    if (placeBlock) {
                        level.setBlock(mutablePos, meteoriteState, Block.UPDATE_ALL);
                        placedAny = true;
                    }
                }
            }
        }

        if (!placedAny) {
            return false;
        }

        BlockPos chestPos = carveInterior(level, center, radius);
        placeChest(level, random, chestPos, meteoriteState, config);

        return true;
    }

    private static int sampleSurfaceHeight(WorldGenLevel level, BlockPos origin) {
        int highest = level.getMinBuildHeight();
        for (var type : SURFACE_HEIGHTMAPS) {
            int height = level.getHeight(type, origin.getX(), origin.getZ());
            if (height > highest) {
                highest = height;
            }
        }
        return highest;
    }

    private static boolean canReplace(LevelAccessor level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }

        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty()) {
            return false;
        }

        if (state.is(BlockTags.FEATURES_CANNOT_REPLACE)) {
            return false;
        }

        return state.canBeReplaced()
                || state.is(BlockTags.BASE_STONE_OVERWORLD)
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.SAND)
                || state.is(BlockTags.SNOW);
    }

    private static boolean shouldPlaceWithJitter(BlockPos pos, double distance, int radius, float jitterAmount) {
        if (distance <= radius - 1.5) {
            return true;
        }

        RandomSource noise = RandomSource.create(Mth.getSeed(pos.getX(), pos.getY(), pos.getZ()));
        double jitter = (noise.nextDouble() - 0.5) * jitterAmount;
        return distance <= radius + jitter;
    }

    private static BlockPos carveInterior(LevelAccessor level, BlockPos center, int radius) {
        var mutable = new BlockPos.MutableBlockPos();
        int cavityRadius = Math.max(1, radius / 3);
        for (int dx = -cavityRadius; dx <= cavityRadius; dx++) {
            for (int dy = -cavityRadius; dy <= cavityRadius; dy++) {
                for (int dz = -cavityRadius; dz <= cavityRadius; dz++) {
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq > cavityRadius * cavityRadius) {
                        continue;
                    }
                    mutable.setWithOffset(center, dx, dy, dz);
                    if (level.getWorldBorder().isWithinBounds(mutable) && withinBuildHeight(level, mutable)) {
                        level.setBlock(mutable, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        return center;
    }

    private static void placeChest(LevelAccessor level, RandomSource random, BlockPos center, BlockState meteoriteState,
            MeteoriteConfig config) {
        BlockPos chestPos = center.above();
        if (!level.getWorldBorder().isWithinBounds(chestPos)) {
            chestPos = center;
        }

        if (!withinBuildHeight(level, chestPos)) {
            chestPos = center;
        }

        if (level.isEmptyBlock(chestPos)) {
            var below = chestPos.below();
            if (withinBuildHeight(level, below) && level.isEmptyBlock(below)) {
                level.setBlock(below, meteoriteState, Block.UPDATE_ALL);
            }
        } else {
            level.setBlock(chestPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        BlockState chestState = config.chestBlock().getState(random, chestPos);
        if (chestState.hasProperty(ChestBlock.FACING)) {
            chestState = chestState.setValue(ChestBlock.FACING,
                    Direction.Plane.HORIZONTAL.getRandomDirection(random));
        }
        if (chestState.hasProperty(ChestBlock.TYPE)) {
            chestState = chestState.setValue(ChestBlock.TYPE, ChestType.SINGLE);
        }
        if (chestState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            chestState = chestState.setValue(BlockStateProperties.WATERLOGGED, false);
        }

        level.setBlock(chestPos, chestState, Block.UPDATE_ALL);

        var above = chestPos.above();
        if (withinBuildHeight(level, above)) {
            level.setBlock(above, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            var side = chestPos.relative(direction);
            if (withinBuildHeight(level, side)) {
                level.setBlock(side, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        BlockEntity blockEntity = level.getBlockEntity(chestPos);
        if (blockEntity instanceof RandomizableContainerBlockEntity container) {
            container.setLootTable(LOOT_TABLE, random.nextLong());
        }
    }

    private static boolean withinBuildHeight(LevelAccessor level, BlockPos pos) {
        return pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight();
    }
}
