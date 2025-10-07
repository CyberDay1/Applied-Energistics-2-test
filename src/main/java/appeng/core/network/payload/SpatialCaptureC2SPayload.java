package appeng.core.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import appeng.core.AppEng;

public record SpatialCaptureC2SPayload(int containerId, BlockPos pos) implements CustomPacketPayload {
    public static final Type<SpatialCaptureC2SPayload> TYPE =
            new Type<>(AppEng.makeId("spatial_capture"));

    public static final StreamCodec<FriendlyByteBuf, SpatialCaptureC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.containerId());
                buf.writeBlockPos(payload.pos());
            },
            buf -> new SpatialCaptureC2SPayload(buf.readVarInt(), buf.readBlockPos()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
