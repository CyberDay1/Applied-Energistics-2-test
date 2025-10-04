package appeng.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appeng.AE2Registries;
import appeng.registry.AE2Items;

public class AE2ItemTagsProvider extends ItemTagsProvider {
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
    }
}
