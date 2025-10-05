package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.PartitionedCellMenu;

public record UpdatePartitionedCellPriorityPacket(int priority) implements ServerboundPacket {
    public static final Type<UpdatePartitionedCellPriorityPacket> TYPE = CustomAppEngPayload
            .createType("update_partitioned_cell_priority");

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePartitionedCellPriorityPacket> STREAM_CODEC = StreamCodec
            .ofMember(UpdatePartitionedCellPriorityPacket::write, UpdatePartitionedCellPriorityPacket::decode);

    @Override
    public Type<UpdatePartitionedCellPriorityPacket> type() {
        return TYPE;
    }

    public static UpdatePartitionedCellPriorityPacket decode(RegistryFriendlyByteBuf buffer) {
        return new UpdatePartitionedCellPriorityPacket(buffer.readVarInt());
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(priority);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player.containerMenu instanceof PartitionedCellMenu menu) {
            menu.updatePriorityFromClient(priority);
        }
    }
}
