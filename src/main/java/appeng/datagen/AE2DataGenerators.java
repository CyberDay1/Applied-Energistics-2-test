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
        // TODO: wire providers
    }
}
