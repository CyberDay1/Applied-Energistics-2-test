package appeng.core.network.payload;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

import appeng.core.AppEng;

public record CraftingJobSyncS2CPayload(List<S2CJobUpdatePayload> jobs) implements CustomPacketPayload {
    public CraftingJobSyncS2CPayload {
        jobs = List.copyOf(jobs);
    }

    public static final Type<CraftingJobSyncS2CPayload> TYPE = new Type<>(AppEng.makeId("crafting_job_sync"));

    public static final StreamCodec<FriendlyByteBuf, CraftingJobSyncS2CPayload> STREAM_CODEC = StreamCodec.of(
            CraftingJobSyncS2CPayload::write, CraftingJobSyncS2CPayload::read);

    private static CraftingJobSyncS2CPayload read(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<S2CJobUpdatePayload> jobs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            jobs.add(S2CJobUpdatePayload.STREAM_CODEC.decode(buf));
        }
        return new CraftingJobSyncS2CPayload(jobs);
    }

    private static void write(FriendlyByteBuf buf, CraftingJobSyncS2CPayload payload) {
        buf.writeVarInt(payload.jobs().size());
        for (var job : payload.jobs()) {
            S2CJobUpdatePayload.STREAM_CODEC.encode(buf, job);
        }
    }

    @Override
    public Type<CraftingJobSyncS2CPayload> type() {
        return TYPE;
    }
}
