package appeng.core.network.serverbound;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.PartitionedCellMenu;

public record UpdatePartitionedCellWhitelistPacket(List<ResourceLocation> whitelist) implements ServerboundPacket {
    public static final Type<UpdatePartitionedCellWhitelistPacket> TYPE = CustomAppEngPayload
            .createType("update_partitioned_cell_whitelist");

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePartitionedCellWhitelistPacket> STREAM_CODEC = StreamCodec
            .ofMember(UpdatePartitionedCellWhitelistPacket::write, UpdatePartitionedCellWhitelistPacket::decode);

    @Override
    public Type<UpdatePartitionedCellWhitelistPacket> type() {
        return TYPE;
    }

    public static UpdatePartitionedCellWhitelistPacket decode(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<ResourceLocation> whitelist = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            whitelist.add(buffer.readResourceLocation());
        }
        return new UpdatePartitionedCellWhitelistPacket(List.copyOf(whitelist));
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(whitelist.size());
        for (var id : whitelist) {
            buffer.writeResourceLocation(id);
        }
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player.containerMenu instanceof PartitionedCellMenu menu) {
            menu.updateWhitelistFromClient(whitelist);
        }
    }
}
