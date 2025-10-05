package appeng.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appeng.AE2Registries;
import appeng.registry.AE2Blocks;

public class AE2BlockTagsProvider extends BlockTagsProvider {
    private static final TagKey<Block> METEORITE_REPLACEABLE = TagKey.create(Registries.BLOCK,
            new ResourceLocation(AE2Registries.MODID, "meteorite_replaceable"));

    public AE2BlockTagsProvider(PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookup,
            ExistingFileHelper helper) {
        super(output, lookup, AE2Registries.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(AE2Blocks.CERTUS_QUARTZ_ORE.get())
                .add(AE2Blocks.CHARGER.get())
                .add(AE2Blocks.INSCRIBER.get())
                .add(AE2Blocks.SKY_STONE.get())
                .add(AE2Blocks.CONTROLLER.get())
                .add(AE2Blocks.ENERGY_ACCEPTOR.get())
                .add(AE2Blocks.CABLE.get());

        tag(METEORITE_REPLACEABLE)
                .addTag(BlockTags.STONE_ORE_REPLACEABLES)
                .addTag(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    }
}
