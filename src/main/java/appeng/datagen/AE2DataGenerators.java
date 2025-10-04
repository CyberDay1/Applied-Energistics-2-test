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

        if (event.includeServer()) {
            generator.addProvider(true, new InscriberRecipeProvider(output));
            generator.addProvider(true, new ChargerRecipeProvider(output));
        }
    }
}
