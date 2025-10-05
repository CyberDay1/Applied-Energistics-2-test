package appeng.core.network.payload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import appeng.api.storage.ItemStackView;
import appeng.core.AppEng;

public record PlannedCraftingJobS2CPayload(int containerId, UUID jobId, List<ItemStackView> outputs)
        implements CustomPacketPayload {
    public static final Type<PlannedCraftingJobS2CPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AppEng.MOD_ID, "planned_crafting_job"));

    public static final StreamCodec<FriendlyByteBuf, PlannedCraftingJobS2CPayload> STREAM_CODEC = StreamCodec.of(
            PlannedCraftingJobS2CPayload::write, PlannedCraftingJobS2CPayload::read);

    public PlannedCraftingJobS2CPayload {
        outputs = List.copyOf(outputs);
    }

    private static PlannedCraftingJobS2CPayload read(FriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        UUID jobId = buf.readUUID();
        int size = buf.readVarInt();
        List<ItemStackView> outputs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            int count = buf.readVarInt();
            var item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (item != null && count > 0) {
                outputs.add(new ItemStackView(item, count));
            }
        }
        return new PlannedCraftingJobS2CPayload(containerId, jobId, outputs);
    }

    private static void write(FriendlyByteBuf buf, PlannedCraftingJobS2CPayload payload) {
        buf.writeVarInt(payload.containerId());
        buf.writeUUID(payload.jobId());
        buf.writeVarInt(payload.outputs().size());
        for (ItemStackView view : payload.outputs()) {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(view.item()));
            buf.writeVarInt(view.count());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
