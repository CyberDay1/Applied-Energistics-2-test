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
    private static final TagKey<Item> MONITORS = tag("monitors");
    private static final TagKey<Item> AUTOMATION = tag("automation");
    private static final TagKey<Item> IO_PLANES = tag("io_planes");
    private static final TagKey<Item> FORGE_SILICON = forgeTag("silicon");
    private static final TagKey<Item> COMMON_SILICON = commonTag("silicon");
    private static final TagKey<Item> FORGE_PROCESSORS = forgeTag("processors");
    private static final TagKey<Item> COMMON_PROCESSORS = commonTag("processors");

    public AE2ItemTagsProvider(PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookup,
            ExistingFileHelper helper) {
        super(output, lookup, AE2Registries.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.GEMS_QUARTZ).add(AE2Items.CERTUS_QUARTZ_CRYSTAL.get());
        tag(FORGE_SILICON).add(AE2Items.SILICON.get());
        tag(COMMON_SILICON).add(AE2Items.SILICON.get());
        tag(FORGE_PROCESSORS)
                .add(AE2Items.LOGIC_PROCESSOR.get())
                .add(AE2Items.ENGINEERING_PROCESSOR.get())
                .add(AE2Items.CALCULATION_PROCESSOR.get());
        tag(COMMON_PROCESSORS)
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
                .addOptional(new ResourceLocation(AE2Registries.MODID, "64k_storage_cell"))
                .add(AE2Items.PARTITIONED_CELL.get());

        tag(CONTROLLERS).add(AE2Items.CONTROLLER.get());

        tag(TERMINALS)
                .add(AE2Items.TERMINAL.get())
                .add(AE2Items.CRAFTING_TERMINAL.get())
                .add(AE2Items.PATTERN_TERMINAL.get())
                .add(AE2Items.PATTERN_ENCODING_TERMINAL_BLOCK.get());

        tag(CABLES)
                .add(AE2Items.CABLE.get())
                .add(AE2Blocks.CABLE.get().asItem());

        // Mirrors the mainline "monitors" tag so downstream integrations can share recipes/config.
        tag(MONITORS).add(AE2Items.CRAFTING_MONITOR.get());

        tag(AUTOMATION)
                .add(AE2Items.ANNIHILATION_PLANE.get())
                .add(AE2Items.FORMATION_PLANE.get());
        tag(IO_PLANES)
                .add(AE2Items.ANNIHILATION_PLANE.get())
                .add(AE2Items.FORMATION_PLANE.get());
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(AE2Registries.MODID, path));
    }

    private static TagKey<Item> forgeTag(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("forge", path));
    }

    private static TagKey<Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("c", path));
    }
}
