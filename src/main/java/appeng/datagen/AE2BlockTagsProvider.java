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
    private static final TagKey<Block> IO_PLANES = TagKey.create(Registries.BLOCK,
            new ResourceLocation(AE2Registries.MODID, "io_planes"));

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
                .add(AE2Blocks.CABLE.get())
                .add(AE2Blocks.SKY_STONE_CHEST.get())
                .add(AE2Blocks.DRIVE.get())
                .add(AE2Blocks.TERMINAL.get())
                .add(AE2Blocks.CRAFTING_TERMINAL.get())
                .add(AE2Blocks.PATTERN_TERMINAL.get())
                .add(AE2Blocks.PATTERN_ENCODING_TERMINAL.get())
                .add(AE2Blocks.CRAFTING_MONITOR.get())
                .add(AE2Blocks.ANNIHILATION_PLANE.get())
                .add(AE2Blocks.FORMATION_PLANE.get());

        tag(METEORITE_REPLACEABLE)
                .addTag(BlockTags.STONE_ORE_REPLACEABLES)
                .addTag(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        tag(IO_PLANES)
                .add(AE2Blocks.ANNIHILATION_PLANE.get())
                .add(AE2Blocks.FORMATION_PLANE.get());
    }
}
