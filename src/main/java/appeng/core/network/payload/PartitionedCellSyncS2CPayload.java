package appeng.core.network.payload;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;

import appeng.core.AppEng;

public record PartitionedCellSyncS2CPayload(int containerId, int priority, List<ResourceLocation> whitelist)
        implements CustomPacketPayload {
    public PartitionedCellSyncS2CPayload {
        whitelist = List.copyOf(whitelist);
    }

    public static final Type<PartitionedCellSyncS2CPayload> TYPE = new Type<>(AppEng.makeId("partitioned_cell_sync"));

    public static final StreamCodec<FriendlyByteBuf, PartitionedCellSyncS2CPayload> STREAM_CODEC = StreamCodec.of(
            PartitionedCellSyncS2CPayload::write, PartitionedCellSyncS2CPayload::read);

    private static PartitionedCellSyncS2CPayload read(FriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        int priority = buf.readVarInt();
        int size = buf.readVarInt();
        List<ResourceLocation> whitelist = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            whitelist.add(buf.readResourceLocation());
        }
        return new PartitionedCellSyncS2CPayload(containerId, priority, whitelist);
    }

    private static void write(FriendlyByteBuf buf, PartitionedCellSyncS2CPayload payload) {
        buf.writeVarInt(payload.containerId());
        buf.writeVarInt(payload.priority());
        buf.writeVarInt(payload.whitelist().size());
        for (var id : payload.whitelist()) {
            buf.writeResourceLocation(id);
        }
    }

    @Override
    public Type<PartitionedCellSyncS2CPayload> type() {
        return TYPE;
    }
}
