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
import appeng.data.providers.AEBiomeModifierProvider;
import appeng.data.providers.AEBlockStateProvider;
import appeng.data.providers.AEItemModelProvider;
import appeng.data.providers.AELangProvider;
import appeng.data.providers.AELootTableProvider;
import appeng.data.providers.AERecipeProvider;
import appeng.data.providers.AETagProviders;

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
            final AETagProviders.BlockTags blocks = new AETagProviders.BlockTags(packOutput, lookup);
            generator.addProvider(true, blocks);
            generator.addProvider(true, new AETagProviders.ItemTags(packOutput, lookup, blocks));

            generator.addProvider(true, new AELootTableProvider(packOutput, lookup));

            generator.addProvider(true, new AERecipeProvider(packOutput));
        }

        if (includeClient) {
            generator.addProvider(true, new AEBlockStateProvider(packOutput, existing));
            generator.addProvider(true, new AEItemModelProvider(packOutput, existing));

            generator.addProvider(true, new AELangProvider(packOutput, "en_us"));
        }

        if (includeServer) {
            generator.addProvider(true, new AEBiomeModifierProvider(packOutput, lookup));
        }
    }
}
