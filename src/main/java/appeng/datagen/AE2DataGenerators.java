package appeng.datagen;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import appeng.AE2Registries;

@EventBusSubscriber(modid = AE2Registries.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class AE2DataGenerators {
    private AE2DataGenerators() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var helper = event.getExistingFileHelper();
        var lookup = event.getLookupProvider();

        if (event.includeServer()) {
            generator.addProvider(true, new InscriberRecipeProvider(output));
            generator.addProvider(true, new ChargerRecipeProvider(output));
            generator.addProvider(true, new AE2ItemTagsProvider(output, lookup, helper));
            generator.addProvider(true, new AE2BlockTagsProvider(output, lookup, helper));
            generator.addProvider(true, new AE2LootTableProvider(output));
            generator.addProvider(true, new AE2WorldgenProvider(output, lookup));
            generator.addProvider(true, new AE2BiomeModifierProvider(output, lookup));
        }

        if (event.includeClient()) {
            generator.addProvider(true, new AE2BlockStateProvider(output, helper));
            generator.addProvider(true, new AE2ItemModelProvider(output, helper));
            generator.addProvider(true, new AE2LanguageProvider(output));
        }
    }
}
