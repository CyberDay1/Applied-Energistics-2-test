package appeng.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import appeng.AE2Registries;

@EventBusSubscriber(modid = AE2Registries.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AE2Network {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlerEvent event) {
        // TODO: add payload registration
    }
}
