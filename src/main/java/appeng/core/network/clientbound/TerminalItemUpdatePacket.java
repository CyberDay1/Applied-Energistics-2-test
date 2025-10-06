package appeng.core.network.clientbound;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import appeng.api.config.RedstoneMode;
import appeng.api.storage.ItemStackView;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.menu.terminal.TerminalMenu;
import appeng.grid.SimpleGridNode.OfflineReason;

public record TerminalItemUpdatePacket(int containerId, boolean online, OfflineReason offlineReason,
        RedstoneMode redstoneMode, List<ItemStackView> stacks)
        implements ClientboundPacket {

    public static final Type<TerminalItemUpdatePacket> TYPE =
            CustomAppEngPayload.createType("terminal_item_update");

    public static final StreamCodec<RegistryFriendlyByteBuf, TerminalItemUpdatePacket> STREAM_CODEC =
            StreamCodec.ofMember(TerminalItemUpdatePacket::write, TerminalItemUpdatePacket::decode);

    public static TerminalItemUpdatePacket decode(RegistryFriendlyByteBuf buf) {
        int containerId = buf.readVarInt();
        boolean online = buf.readBoolean();
        int offlineReasonOrdinal = buf.readVarInt();
        int redstoneModeOrdinal = buf.readVarInt();
        int size = buf.readVarInt();
        List<ItemStackView> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ResourceLocation id = buf.readResourceLocation();
            int count = buf.readVarInt();
            Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
            if (item != null) {
                stacks.add(new ItemStackView(item, count));
            }
        }
        var offlineReason = OfflineReason.values()[Math.max(0, Math.min(OfflineReason.values().length - 1, offlineReasonOrdinal))];
        var redstoneMode = RedstoneMode.values()[Math.max(0, Math.min(RedstoneMode.values().length - 1, redstoneModeOrdinal))];
        return new TerminalItemUpdatePacket(containerId, online, offlineReason, redstoneMode, stacks);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeBoolean(online);
        buf.writeVarInt(offlineReason.ordinal());
        buf.writeVarInt(redstoneMode.ordinal());
        buf.writeVarInt(stacks.size());
        for (ItemStackView view : stacks) {
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(view.item()));
            buf.writeVarInt(view.count());
        }
    }

    @Override
    public Type<TerminalItemUpdatePacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        if (player.containerMenu instanceof TerminalMenu menu && menu.containerId == containerId) {
            menu.handleClientUpdate(stacks, online, offlineReason, redstoneMode);
        }
    }
}
