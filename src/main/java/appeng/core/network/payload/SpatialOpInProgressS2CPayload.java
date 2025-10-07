package appeng.core.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import appeng.core.AppEng;

public record SpatialOpInProgressS2CPayload(int containerId, BlockPos pos, boolean inProgress)
        implements CustomPacketPayload {
    public static final Type<SpatialOpInProgressS2CPayload> TYPE =
            new Type<>(AppEng.makeId("spatial_op_in_progress"));

    public static final StreamCodec<FriendlyByteBuf, SpatialOpInProgressS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.containerId());
                buf.writeBlockPos(payload.pos());
                buf.writeBoolean(payload.inProgress());
            },
            buf -> new SpatialOpInProgressS2CPayload(buf.readVarInt(), buf.readBlockPos(), buf.readBoolean()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
