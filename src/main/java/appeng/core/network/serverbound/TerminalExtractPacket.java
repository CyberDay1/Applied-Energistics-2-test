package appeng.core.network.serverbound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

import appeng.core.network.CustomAppEngPayload;
import appeng.core.network.ServerboundPacket;
import appeng.menu.terminal.TerminalMenu;

public record TerminalExtractPacket(int containerId, ResourceLocation itemId, int amount)
        implements ServerboundPacket {

    public static final Type<TerminalExtractPacket> TYPE = CustomAppEngPayload.createType("terminal_extract");

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalExtractPacket> STREAM_CODEC =
            StreamCodec.ofMember(TerminalExtractPacket::write, TerminalExtractPacket::decode);

    public static TerminalExtractPacket decode(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        ResourceLocation itemId = buf.readResourceLocation();
        int amount = buf.readVarInt();
        return new TerminalExtractPacket(containerId, itemId, amount);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeResourceLocation(itemId);
        buf.writeVarInt(amount);
    }

    @Override
    public Type<TerminalExtractPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnServer(ServerPlayer player) {
        if (amount <= 0) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
        if (item == null) {
            return;
        }
        if (player.containerMenu instanceof TerminalMenu menu && menu.containerId == containerId) {
            menu.handleExtract(item, amount);
        }
    }
}
