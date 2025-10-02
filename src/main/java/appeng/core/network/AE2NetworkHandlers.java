package appeng.core.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import appeng.core.network.payload.AE2ActionC2SPayload;
import appeng.core.network.payload.AE2HelloS2CPayload;
import appeng.core.network.payload.AE2LoginAckC2SPayload;
import appeng.core.network.payload.AE2LoginSyncS2CPayload;

public final class AE2NetworkHandlers {
    private AE2NetworkHandlers() {
    }

    // S2C handler runs on client
    public static void handleHelloClient(final AE2HelloS2CPayload payload, final IPayloadContext ctx) {
        // Ensure on main client thread
        ctx.enqueueWork(() -> {
            // Example: update a client-side cache or open a screen
            // AE2ClientCache.setGreeting(payload.message());
        });
        ctx.setPacketHandled(true);
    }

    // C2S handler runs on server
    public static void handleActionServer(final AE2ActionC2SPayload payload, final IPayloadContext ctx) {
        // Validate and perform server-side action
        // AE2ServerActions.perform(payload, ctx.player());
        ctx.setPacketHandled(true);
    }

    // Login S2C -> client
    public static void handleLoginSyncClient(final AE2LoginSyncS2CPayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Apply synced server settings before play starts
            // AE2ClientConfig.apply(payload.configBlob());
        });
        ctx.setPacketHandled(true);
    }

    // Login C2S -> server
    public static void handleLoginAckServer(final AE2LoginAckC2SPayload payload, final IPayloadContext ctx) {
        // Optionally record that the client accepted sync
        ctx.setPacketHandled(true);
    }
}
