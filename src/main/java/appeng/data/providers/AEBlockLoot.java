package appeng.data.providers;

import java.util.Set;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.loot.BlockLootSubProvider;

public final class AEBlockLoot extends BlockLootSubProvider {
    public AEBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        // dropSelf(AEBlocks.QUARTZ_ORE.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // return AEBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
        return Set.<Block>of();
    }
}
