package appeng.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import appeng.AE2Registries;

@EventBusSubscriber(modid = AE2Registries.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AE2ClientSetup {
    private AE2ClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // TODO: register MenuScreens and render layers
        });
    }
}
