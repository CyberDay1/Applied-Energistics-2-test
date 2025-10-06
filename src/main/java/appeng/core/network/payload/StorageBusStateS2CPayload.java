package appeng.core.network.payload;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

import appeng.api.config.AccessRestriction;
import appeng.api.config.StorageFilter;
import appeng.api.config.YesNo;
import appeng.core.AppEng;

public record StorageBusStateS2CPayload(int containerId, AccessRestriction accessMode, StorageFilter storageFilter,
        YesNo filterOnExtract, @Nullable Component connectedTo) implements CustomPacketPayload {

    public static final Type<StorageBusStateS2CPayload> TYPE = new Type<>(AppEng.makeId("storage_bus_state"));

    public static final StreamCodec<FriendlyByteBuf, StorageBusStateS2CPayload> STREAM_CODEC = StreamCodec.of(
            StorageBusStateS2CPayload::write, StorageBusStateS2CPayload::read);

    private static StorageBusStateS2CPayload read(FriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        AccessRestriction accessMode = buf.readEnum(AccessRestriction.class);
        StorageFilter storageFilter = buf.readEnum(StorageFilter.class);
        YesNo filterOnExtract = buf.readEnum(YesNo.class);
        Component connectedTo = buf.readBoolean() ? buf.readComponent() : null;
        return new StorageBusStateS2CPayload(containerId, accessMode, storageFilter, filterOnExtract, connectedTo);
    }

    private static void write(FriendlyByteBuf buf, StorageBusStateS2CPayload payload) {
        buf.writeVarInt(payload.containerId());
        buf.writeEnum(payload.accessMode());
        buf.writeEnum(payload.storageFilter());
        buf.writeEnum(payload.filterOnExtract());
        buf.writeBoolean(payload.connectedTo() != null);
        if (payload.connectedTo() != null) {
            buf.writeComponent(payload.connectedTo());
        }
    }

    @Override
    public Type<StorageBusStateS2CPayload> type() {
        return TYPE;
    }
}
