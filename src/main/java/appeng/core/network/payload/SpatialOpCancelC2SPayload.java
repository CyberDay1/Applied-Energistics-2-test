package appeng.core.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SpatialOpCancelC2SPayload(int containerId, BlockPos pos) implements CustomPacketPayload {
    public static final Type<SpatialOpCancelC2SPayload> TYPE = new Type<>("ae2:spatial_op_cancel_c2s");

    public static final StreamCodec<FriendlyByteBuf, SpatialOpCancelC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.containerId());
                buffer.writeBlockPos(payload.pos());
            },
            buffer -> new SpatialOpCancelC2SPayload(buffer.readVarInt(), buffer.readBlockPos()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
