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
import appeng.core.network.payload.CraftingJobSyncS2CPayload;
import appeng.core.network.payload.EncodePatternC2SPayload;
import appeng.core.network.payload.PartitionedCellSyncS2CPayload;
import appeng.core.network.payload.PlanCraftingJobC2SPayload;
import appeng.core.network.payload.PlannedCraftingJobS2CPayload;
import appeng.core.network.payload.S2CJobUpdatePayload;
import appeng.core.network.payload.SetPatternEncodingModeC2SPayload;
import appeng.core.network.payload.StorageBusStateS2CPayload;

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

        play.playToClient(AE2HelloS2CPayload.TYPE, AE2HelloS2CPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleHelloClient);
        play.playToClient(PlannedCraftingJobS2CPayload.TYPE, PlannedCraftingJobS2CPayload.STREAM_CODEC,
                AE2NetworkHandlers::handlePlannedCraftingJobClient);
        play.playToClient(S2CJobUpdatePayload.TYPE, S2CJobUpdatePayload.STREAM_CODEC,
                AE2NetworkHandlers::handleJobUpdateClient);
        play.playToClient(CraftingJobSyncS2CPayload.TYPE, CraftingJobSyncS2CPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleCraftingJobSyncClient);
        play.playToClient(PartitionedCellSyncS2CPayload.TYPE, PartitionedCellSyncS2CPayload.STREAM_CODEC,
                AE2NetworkHandlers::handlePartitionedCellSyncClient);
        play.playToClient(StorageBusStateS2CPayload.TYPE, StorageBusStateS2CPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleStorageBusStateClient);

        play.playToServer(AE2ActionC2SPayload.TYPE, AE2ActionC2SPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleActionServer);
        play.playToServer(PlanCraftingJobC2SPayload.TYPE, PlanCraftingJobC2SPayload.STREAM_CODEC,
                AE2NetworkHandlers::handlePlanCraftingJobServer);
        play.playToServer(EncodePatternC2SPayload.TYPE, EncodePatternC2SPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleEncodePatternServer);
        play.playToServer(SetPatternEncodingModeC2SPayload.TYPE, SetPatternEncodingModeC2SPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleSetPatternEncodingModeServer);

        // Login (handshake) payloads
        final LoginPayloadRegistrar login = event.login();
        login.register(AE2LoginSyncS2CPayload.TYPE, AE2LoginSyncS2CPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleLoginSyncClient);
        login.register(AE2LoginAckC2SPayload.TYPE, AE2LoginAckC2SPayload.STREAM_CODEC,
                AE2NetworkHandlers::handleLoginAckServer);
    }
}
