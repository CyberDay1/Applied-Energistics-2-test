package appeng.core.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.LoginPayloadRegistrar;
import net.neoforged.neoforge.network.registration.PlayPayloadRegistrar;

import appeng.core.AppEng;
import appeng.core.network.payload.AE2ActionC2SPayload;
import appeng.core.network.payload.AE2HelloS2CPayload;
import appeng.core.network.payload.AE2LoginAckC2SPayload;
import appeng.core.network.payload.AE2LoginSyncS2CPayload;

@EventBusSubscriber(modid = AppEng.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class AE2Network {
    private AE2Network() {
    }

    // Protocol version for play payloads. Update cautiously.
    public static final String PLAY_PROTOCOL = "1";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Play (in-game) payloads
        final PlayPayloadRegistrar play = event.registrar(PLAY_PROTOCOL);

        // S2C example
        play.playToClient(AE2HelloS2CPayload.TYPE, AE2HelloS2CPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleHelloClient);

        // C2S example
        play.playToServer(AE2ActionC2SPayload.TYPE, AE2ActionC2SPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleActionServer);

        // Login (handshake) payloads
        final LoginPayloadRegistrar login = event.login();
        login.register(AE2LoginSyncS2CPayload.TYPE, AE2LoginSyncS2CPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleLoginSyncClient);
        login.register(AE2LoginAckC2SPayload.TYPE, AE2LoginAckC2SPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleLoginAckServer);
    }
}
