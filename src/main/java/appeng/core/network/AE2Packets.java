package appeng.core.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.core.network.payload.AE2ActionC2SPayload;
import appeng.core.network.payload.AE2HelloS2CPayload;

public final class AE2Packets {
    private AE2Packets() {
    }

    public static void sendHello(ServerPlayer player, String msg) {
        PacketDistributor.sendToPlayer(player, new AE2HelloS2CPayload(msg));
    }

    public static void sendActionToServer(int id, long value) {
        PacketDistributor.sendToServer(new AE2ActionC2SPayload(id, value));
    }
}
