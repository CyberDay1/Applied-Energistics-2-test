package appeng.core.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;

public record SetPatternEncodingModeC2SPayload(int containerId, boolean processing)
        implements CustomPacketPayload {
    public static final Type<SetPatternEncodingModeC2SPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AppEng.MOD_ID, "set_pattern_encoding_mode"));

    public static final StreamCodec<FriendlyByteBuf, SetPatternEncodingModeC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.containerId());
                buf.writeBoolean(payload.processing());
            },
            buf -> new SetPatternEncodingModeC2SPayload(buf.readVarInt(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
