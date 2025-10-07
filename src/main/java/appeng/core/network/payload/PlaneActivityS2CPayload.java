package appeng.core.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

import appeng.core.AppEng;

public record PlaneActivityS2CPayload(int containerId, boolean active) implements CustomPacketPayload {

    public static final Type<PlaneActivityS2CPayload> TYPE = new Type<>(AppEng.makeId("plane_activity"));

    public static final StreamCodec<FriendlyByteBuf, PlaneActivityS2CPayload> STREAM_CODEC = StreamCodec.of(
            PlaneActivityS2CPayload::write, PlaneActivityS2CPayload::read);

    private static PlaneActivityS2CPayload read(FriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        boolean active = buf.readBoolean();
        return new PlaneActivityS2CPayload(containerId, active);
    }

    private static void write(FriendlyByteBuf buf, PlaneActivityS2CPayload payload) {
        buf.writeVarInt(payload.containerId());
        buf.writeBoolean(payload.active());
    }

    @Override
    public Type<PlaneActivityS2CPayload> type() {
        return TYPE;
    }
}
