package appeng.core.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SpatialOpRollbackS2CPayload(int containerId, BlockPos pos, BlockPos regionSize)
        implements CustomPacketPayload {
    public static final Type<SpatialOpRollbackS2CPayload> TYPE = new Type<>("ae2:spatial_op_rollback");

    public static final StreamCodec<FriendlyByteBuf, SpatialOpRollbackS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeVarInt(payload.containerId());
                buffer.writeBlockPos(payload.pos());
                buffer.writeBlockPos(payload.regionSize());
            },
            buffer -> new SpatialOpRollbackS2CPayload(buffer.readVarInt(), buffer.readBlockPos(),
                    buffer.readBlockPos()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
