package appeng.core.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;

public record AE2LoginAckC2SPayload(boolean ok) implements CustomPacketPayload {
    public static final Type<AE2LoginAckC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AppEng.MOD_ID, "login_ack_c2s"));

    public static final StreamCodec<FriendlyByteBuf, AE2LoginAckC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeBoolean(payload.ok()),
            buf -> new AE2LoginAckC2SPayload(buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
