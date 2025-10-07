package appeng.core.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import appeng.core.AppEng;

public record SpatialRestoreC2SPayload(int containerId, BlockPos pos, BlockPos regionSize)
        implements CustomPacketPayload {
    public static final Type<SpatialRestoreC2SPayload> TYPE =
            new Type<>(AppEng.makeId("spatial_restore"));

    public static final StreamCodec<FriendlyByteBuf, SpatialRestoreC2SPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.containerId());
                buf.writeBlockPos(payload.pos());
                buf.writeBlockPos(payload.regionSize());
            },
            buf -> new SpatialRestoreC2SPayload(buf.readVarInt(), buf.readBlockPos(), buf.readBlockPos()));

    public SpatialRestoreC2SPayload(int containerId, BlockPos pos) {
        this(containerId, pos, BlockPos.ZERO);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
