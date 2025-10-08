package appeng.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.concurrent.CompletableFuture;

import appeng.core.AppEng;
import appeng.datagen.AE2BlockStateProvider;
import appeng.datagen.AE2BlockTagsProvider;
import appeng.datagen.AE2ItemModelProvider;
import appeng.datagen.AE2ItemTagsProvider;
import appeng.datagen.AE2LootTableProvider;
import appeng.datagen.AE2RecipeProvider;
//? if eval(current.version, ">=1.21.4") {
import appeng.datagen.AE2WorldgenProvider;
import appeng.datagen.AE2BiomeModifierProvider;
//? }
import appeng.datagen.AELangProvider;
import appeng.datagen.ChargerRecipeProvider;
import appeng.datagen.InscriberRecipeProvider;
import appeng.datagen.ProcessingMachineRegistryProvider;

@EventBusSubscriber(modid = AppEng.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class AE2DataGen {
    private AE2DataGen() {
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final PackOutput packOutput = generator.getPackOutput();
        final ExistingFileHelper existing = event.getExistingFileHelper();
        final CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        final boolean includeClient = event.includeClient();
        final boolean includeServer = event.includeServer();

        if (includeServer) {
            var blockTags = new AE2BlockTagsProvider(packOutput, lookup, existing);
            generator.addProvider(true, blockTags);
            generator.addProvider(true, new AE2ItemTagsProvider(packOutput, lookup, existing));

            generator.addProvider(true, new AE2LootTableProvider(packOutput));
            //? if eval(current.version, ">=1.21.4") {
            generator.addProvider(true, new AE2WorldgenProvider(packOutput, lookup));
            generator.addProvider(true, new AE2BiomeModifierProvider(packOutput, lookup));
            //? }

            generator.addProvider(true, new AE2RecipeProvider(packOutput));
            generator.addProvider(true, new ChargerRecipeProvider(packOutput));
            generator.addProvider(true, new InscriberRecipeProvider(packOutput));
            generator.addProvider(true, new ProcessingMachineRegistryProvider(packOutput));
        }

        if (includeClient) {
            generator.addProvider(true, new AE2BlockStateProvider(packOutput, existing));
            generator.addProvider(true, new AE2ItemModelProvider(packOutput, existing));

            generator.addProvider(true, new AELangProvider(packOutput));
        }
    }
}
