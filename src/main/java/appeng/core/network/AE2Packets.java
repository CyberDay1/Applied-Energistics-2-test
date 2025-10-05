package appeng.core.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.core.network.payload.AE2ActionC2SPayload;
import appeng.core.network.payload.AE2HelloS2CPayload;
import appeng.core.network.payload.PlanCraftingJobC2SPayload;
import appeng.core.network.payload.PlannedCraftingJobS2CPayload;

import appeng.crafting.CraftingJob;

public final class AE2Packets {
    private AE2Packets() {
    }

    public static void sendHello(ServerPlayer player, String msg) {
        PacketDistributor.sendToPlayer(player, new AE2HelloS2CPayload(msg));
    }

    public static void sendActionToServer(int id, long value) {
        PacketDistributor.sendToServer(new AE2ActionC2SPayload(id, value));
    }

    public static void planCraftingJob(int containerId, int slotIndex) {
        PacketDistributor.sendToServer(new PlanCraftingJobC2SPayload(containerId, slotIndex));
    }

    public static void sendPlannedCraftingJob(ServerPlayer player, int containerId, CraftingJob job) {
        PacketDistributor.sendToPlayer(player,
                new PlannedCraftingJobS2CPayload(containerId, job.getId(), job.getOutputs()));
    }
}
