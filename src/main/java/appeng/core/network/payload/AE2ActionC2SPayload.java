package appeng.core.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;

public record AE2ActionC2SPayload(int actionId, long value) implements CustomPacketPayload {
    public static final Type<AE2ActionC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AppEng.MOD_ID, "action_c2s"));

    public static final StreamCodec<FriendlyByteBuf, AE2ActionC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.actionId());
                buf.writeVarLong(payload.value());
            },
            buf -> new AE2ActionC2SPayload(buf.readVarInt(), buf.readVarLong()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
