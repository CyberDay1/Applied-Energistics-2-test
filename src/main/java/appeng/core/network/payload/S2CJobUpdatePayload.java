package appeng.core.network.payload;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.network.codec.StreamCodec;

import appeng.core.AppEng;
import appeng.crafting.CraftingJob;

public record S2CJobUpdatePayload(UUID jobId, CraftingJob.State state, int ticksCompleted, int ticksRequired)
        implements CustomPacketPayload {
    public static final Type<S2CJobUpdatePayload> TYPE = new Type<>(AppEng.makeId("s2c_job_update"));

    public static final StreamCodec<FriendlyByteBuf, S2CJobUpdatePayload> STREAM_CODEC = StreamCodec.of(
            S2CJobUpdatePayload::write, S2CJobUpdatePayload::read);

    private static S2CJobUpdatePayload read(FriendlyByteBuf buf) {
        UUID jobId = buf.readUUID();
        CraftingJob.State state = buf.readEnum(CraftingJob.State.class);
        int ticksCompleted = buf.readVarInt();
        int ticksRequired = buf.readVarInt();
        return new S2CJobUpdatePayload(jobId, state, ticksCompleted, ticksRequired);
    }

    private static void write(FriendlyByteBuf buf, S2CJobUpdatePayload payload) {
        buf.writeUUID(payload.jobId());
        buf.writeEnum(payload.state());
        buf.writeVarInt(payload.ticksCompleted());
        buf.writeVarInt(payload.ticksRequired());
    }

    @Override
    public Type<S2CJobUpdatePayload> type() {
        return TYPE;
    }
}
