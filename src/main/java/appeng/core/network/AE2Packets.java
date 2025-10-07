package appeng.core.network;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import appeng.api.config.AccessRestriction;
import appeng.api.config.StorageFilter;
import appeng.api.config.YesNo;
import appeng.core.network.payload.AE2ActionC2SPayload;
import appeng.core.network.payload.AE2HelloS2CPayload;
import appeng.core.network.payload.CraftingJobSyncS2CPayload;
import appeng.core.network.payload.EncodePatternC2SPayload;
import appeng.core.network.payload.PartitionedCellSyncS2CPayload;
import appeng.core.network.payload.PlanCraftingJobC2SPayload;
import appeng.core.network.payload.PlannedCraftingJobS2CPayload;
import appeng.core.network.payload.S2CJobUpdatePayload;
import appeng.core.network.payload.SetPatternEncodingModeC2SPayload;
import appeng.core.network.payload.SpatialCaptureC2SPayload;
import appeng.core.network.payload.SpatialRestoreC2SPayload;
import appeng.core.network.payload.StorageBusStateS2CPayload;

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

    public static void encodePattern(int containerId) {
        PacketDistributor.sendToServer(new EncodePatternC2SPayload(containerId));
    }

    public static void setPatternEncodingMode(int containerId, boolean processing) {
        PacketDistributor.sendToServer(new SetPatternEncodingModeC2SPayload(containerId, processing));
    }

    public static void sendPlannedCraftingJob(ServerPlayer player, int containerId, CraftingJob job) {
        PacketDistributor.sendToPlayer(player,
                new PlannedCraftingJobS2CPayload(containerId, job.getId(), job.getOutputs()));
    }

    public static void sendCraftingJobUpdate(ServerLevel level, BlockPos pos, CraftingJob job) {
        PacketDistributor.sendToPlayersNear(level, null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 32,
                createCraftingJobUpdate(job));
    }

    public static S2CJobUpdatePayload createCraftingJobUpdate(CraftingJob job) {
        return new S2CJobUpdatePayload(job.getId(), job.getState(), job.isProcessing(), job.getTicksCompleted(),
                job.getTicksRequired(), job.getInsertedOutputs(), job.getDroppedOutputs(), job.getParentJobId(),
                job.getSubJobStatuses(), job.getOutputs());
    }

    public static void sendCraftingJobSync(ServerPlayer player, List<S2CJobUpdatePayload> jobs) {
        if (jobs.isEmpty()) {
            return;
        }

        PacketDistributor.sendToPlayer(player, new CraftingJobSyncS2CPayload(jobs));
    }

    public static void sendPartitionedCellSync(ServerPlayer player, int containerId, int priority,
            boolean whitelistMode, List<ResourceLocation> whitelist) {
        PacketDistributor.sendToPlayer(player,
                new PartitionedCellSyncS2CPayload(containerId, priority, whitelistMode, whitelist));
    }

    public static void sendStorageBusState(ServerPlayer player, int containerId, AccessRestriction accessMode,
            StorageFilter storageFilter, YesNo filterOnExtract, @Nullable Component connectedTo) {
        PacketDistributor.sendToPlayer(player,
                new StorageBusStateS2CPayload(containerId, accessMode, storageFilter, filterOnExtract, connectedTo));
    }

    public static void sendSpatialCapture(int containerId, BlockPos pos) {
        PacketDistributor.sendToServer(new SpatialCaptureC2SPayload(containerId, pos));
    }

    public static void sendSpatialRestore(int containerId, BlockPos pos) {
        PacketDistributor.sendToServer(new SpatialRestoreC2SPayload(containerId, pos));
    }
}
