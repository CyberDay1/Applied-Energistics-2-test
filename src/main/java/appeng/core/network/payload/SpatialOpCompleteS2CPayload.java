package appeng.core.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import appeng.core.AppEng;

public record SpatialOpCompleteS2CPayload(int containerId, BlockPos pos) implements CustomPacketPayload {
    public static final Type<SpatialOpCompleteS2CPayload> TYPE =
            new Type<>(AppEng.makeId("spatial_op_complete"));

    public static final StreamCodec<FriendlyByteBuf, SpatialOpCompleteS2CPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.containerId());
                buf.writeBlockPos(payload.pos());
            },
            buf -> new SpatialOpCompleteS2CPayload(buf.readVarInt(), buf.readBlockPos()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
