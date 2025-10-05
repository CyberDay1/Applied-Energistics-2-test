package appeng.core.network.clientbound;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import appeng.api.stacks.GenericStack;
import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import appeng.crafting.monitor.CraftingMonitorEntry;
import appeng.menu.me.crafting.CraftingMonitorMenu;

public record CraftingMonitorUpdatePacket(int containerId, List<CraftingMonitorEntry> jobs)
        implements ClientboundPacket {

    public static final CustomPacketPayload.Type<CraftingMonitorUpdatePacket> TYPE =
            CustomAppEngPayload.createType("crafting_monitor_update");

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingMonitorUpdatePacket> STREAM_CODEC = StreamCodec
            .ofMember(CraftingMonitorUpdatePacket::write, CraftingMonitorUpdatePacket::decode);

    public static CraftingMonitorUpdatePacket decode(RegistryFriendlyByteBuf buffer) {
        int containerId = buffer.readVarInt();
        int size = buffer.readVarInt();
        var jobs = new ArrayList<CraftingMonitorEntry>(size);
        for (int i = 0; i < size; i++) {
            var stack = GenericStack.readBuffer(buffer);
            long total = buffer.readVarLong();
            long completed = buffer.readVarLong();
            long elapsed = buffer.readVarLong();
            boolean done = buffer.readBoolean();
            jobs.add(new CraftingMonitorEntry(stack, total, completed, elapsed, done));
        }
        return new CraftingMonitorUpdatePacket(containerId, List.copyOf(jobs));
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(containerId);
        buffer.writeVarInt(jobs.size());
        for (var job : jobs) {
            GenericStack.writeBuffer(job.stack(), buffer);
            buffer.writeVarLong(job.totalItems());
            buffer.writeVarLong(job.completedItems());
            buffer.writeVarLong(job.elapsedTimeNanos());
            buffer.writeBoolean(job.done());
        }
    }

    @Override
    public Type<CraftingMonitorUpdatePacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        if (player.containerMenu instanceof CraftingMonitorMenu menu && menu.containerId == containerId) {
            menu.handleClientUpdate(jobs);
        }
    }
}
