package appeng.core.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;

public record EncodePatternC2SPayload(int containerId) implements CustomPacketPayload {
    public static final Type<EncodePatternC2SPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AppEng.MOD_ID, "encode_pattern"));

    public static final StreamCodec<FriendlyByteBuf, EncodePatternC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeVarInt(payload.containerId()),
            buf -> new EncodePatternC2SPayload(buf.readVarInt()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
