package appeng.core.network.payload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import appeng.api.storage.ItemStackView;
import appeng.core.AppEng;
import appeng.crafting.CraftingJob;

public record S2CJobUpdatePayload(UUID jobId, CraftingJob.State state, boolean processing, int ticksCompleted,
        int ticksRequired, int insertedOutputs, int droppedOutputs, @Nullable UUID parentJobId,
        Map<UUID, CraftingJob.SubJobStatus> subJobs, List<ItemStackView> outputs)
        implements CustomPacketPayload {
    public S2CJobUpdatePayload {
        subJobs = Map.copyOf(subJobs);
        outputs = List.copyOf(outputs);
    }

    public static final Type<S2CJobUpdatePayload> TYPE = new Type<>(AppEng.makeId("s2c_job_update"));

    public static final StreamCodec<FriendlyByteBuf, S2CJobUpdatePayload> STREAM_CODEC = StreamCodec.of(
            S2CJobUpdatePayload::write, S2CJobUpdatePayload::read);

    private static S2CJobUpdatePayload read(FriendlyByteBuf buf) {
        UUID jobId = buf.readUUID();
        CraftingJob.State state = buf.readEnum(CraftingJob.State.class);
        boolean processing = buf.readBoolean();
        int ticksCompleted = buf.readVarInt();
        int ticksRequired = buf.readVarInt();
        int insertedOutputs = buf.readVarInt();
        int droppedOutputs = buf.readVarInt();
        UUID parentJobId = buf.readBoolean() ? buf.readUUID() : null;
        int subJobCount = buf.readVarInt();
        Map<UUID, CraftingJob.SubJobStatus> subJobs = new HashMap<>(subJobCount);
        for (int i = 0; i < subJobCount; i++) {
            UUID childId = buf.readUUID();
            CraftingJob.SubJobStatus status = buf.readEnum(CraftingJob.SubJobStatus.class);
            subJobs.put(childId, status);
        }

        int outputCount = buf.readVarInt();
        List<ItemStackView> outputs = new ArrayList<>(outputCount);
        for (int i = 0; i < outputCount; i++) {
            ResourceLocation itemId = buf.readResourceLocation();
            int count = buf.readVarInt();
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item != null) {
                outputs.add(new ItemStackView(item, count));
            }
        }

        return new S2CJobUpdatePayload(jobId, state, processing, ticksCompleted, ticksRequired, insertedOutputs,
                droppedOutputs, parentJobId, subJobs, outputs);
    }

    private static void write(FriendlyByteBuf buf, S2CJobUpdatePayload payload) {
        buf.writeUUID(payload.jobId());
        buf.writeEnum(payload.state());
        buf.writeBoolean(payload.processing());
        buf.writeVarInt(payload.ticksCompleted());
        buf.writeVarInt(payload.ticksRequired());
        buf.writeVarInt(payload.insertedOutputs());
        buf.writeVarInt(payload.droppedOutputs());
        buf.writeBoolean(payload.parentJobId() != null);
        if (payload.parentJobId() != null) {
            buf.writeUUID(payload.parentJobId());
        }

        buf.writeVarInt(payload.subJobs().size());
        for (var entry : payload.subJobs().entrySet()) {
            buf.writeUUID(entry.getKey());
            buf.writeEnum(entry.getValue());
        }

        buf.writeVarInt(payload.outputs().size());
        for (var view : payload.outputs()) {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(view.item()));
            buf.writeVarInt(view.count());
        }
    }

    @Override
    public Type<S2CJobUpdatePayload> type() {
        return TYPE;
    }
}
