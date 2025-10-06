package appeng.core.network.serverbound;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.PartitionedCellMenu;

public record UpdatePartitionedCellWhitelistModePacket(boolean whitelistMode) implements ServerboundPacket {
    public static final Type<UpdatePartitionedCellWhitelistModePacket> TYPE = CustomAppEngPayload
            .createType("update_partitioned_cell_whitelist_mode");

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePartitionedCellWhitelistModePacket> STREAM_CODEC = StreamCodec
            .ofMember(UpdatePartitionedCellWhitelistModePacket::write, UpdatePartitionedCellWhitelistModePacket::decode);

    @Override
    public Type<UpdatePartitionedCellWhitelistModePacket> type() {
        return TYPE;
    }

    public static UpdatePartitionedCellWhitelistModePacket decode(RegistryFriendlyByteBuf buffer) {
        return new UpdatePartitionedCellWhitelistModePacket(buffer.readBoolean());
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(whitelistMode);
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (player.containerMenu instanceof PartitionedCellMenu menu) {
            menu.updateWhitelistModeFromClient(whitelistMode);
        }
    }
}
