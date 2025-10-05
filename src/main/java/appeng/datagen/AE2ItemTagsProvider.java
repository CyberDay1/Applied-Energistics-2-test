package appeng.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appeng.AE2Registries;
import appeng.registry.AE2Blocks;
import appeng.registry.AE2Items;

public class AE2ItemTagsProvider extends ItemTagsProvider {
    private static final TagKey<Item> PRESSES = tag("presses");
    private static final TagKey<Item> CELLS = tag("cells");
    private static final TagKey<Item> CONTROLLERS = tag("controllers");
    private static final TagKey<Item> TERMINALS = tag("terminals");
    private static final TagKey<Item> CABLES = tag("cables");

    public AE2ItemTagsProvider(PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookup,
            ExistingFileHelper helper) {
        super(output, lookup, AE2Registries.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.GEMS_QUARTZ).add(AE2Items.CERTUS_QUARTZ_CRYSTAL.get());
        tag(ItemTags.create(new ResourceLocation("forge", "silicon")))
                .add(AE2Items.SILICON.get());
        tag(ItemTags.create(new ResourceLocation("forge", "processors")))
                .add(AE2Items.LOGIC_PROCESSOR.get())
                .add(AE2Items.ENGINEERING_PROCESSOR.get())
                .add(AE2Items.CALCULATION_PROCESSOR.get());

        tag(PRESSES)
                .add(AE2Items.INSCRIBER_SILICON_PRESS.get())
                .add(AE2Items.INSCRIBER_LOGIC_PRESS.get())
                .add(AE2Items.INSCRIBER_ENGINEERING_PRESS.get())
                .add(AE2Items.INSCRIBER_CALCULATION_PRESS.get());

        tag(CELLS)
                .addOptional(new ResourceLocation(AE2Registries.MODID, "1k_storage_cell"))
                .addOptional(new ResourceLocation(AE2Registries.MODID, "4k_storage_cell"))
                .addOptional(new ResourceLocation(AE2Registries.MODID, "16k_storage_cell"))
                .addOptional(new ResourceLocation(AE2Registries.MODID, "64k_storage_cell"));

        tag(CONTROLLERS).add(AE2Items.CONTROLLER.get());

        tag(TERMINALS)
                .addOptional(new ResourceLocation(AE2Registries.MODID, "terminal"))
                .addOptional(new ResourceLocation(AE2Registries.MODID, "crafting_terminal"))
                .addOptional(new ResourceLocation(AE2Registries.MODID, "pattern_terminal"));

        tag(CABLES)
                .add(AE2Items.CABLE.get())
                .add(AE2Blocks.CABLE.get().asItem());
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(AE2Registries.MODID, path));
    }
}
