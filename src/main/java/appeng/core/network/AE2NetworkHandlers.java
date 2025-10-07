package appeng.core.network;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import appeng.api.storage.ItemStackView;
import appeng.client.gui.me.common.ClientCraftingJobTracker;
import appeng.core.AELog;
import appeng.core.network.AE2Packets;
import appeng.core.network.payload.AE2ActionC2SPayload;
import appeng.core.network.payload.AE2HelloS2CPayload;
import appeng.core.network.payload.AE2LoginAckC2SPayload;
import appeng.core.network.payload.AE2LoginSyncS2CPayload;
import appeng.core.network.payload.CraftingJobSyncS2CPayload;
import appeng.core.network.payload.EncodePatternC2SPayload;
import appeng.core.network.payload.PartitionedCellSyncS2CPayload;
import appeng.core.network.payload.PlanCraftingJobC2SPayload;
import appeng.core.network.payload.PlannedCraftingJobS2CPayload;
import appeng.core.network.payload.S2CJobUpdatePayload;
import appeng.core.network.payload.SetPatternEncodingModeC2SPayload;
import appeng.core.network.payload.SpatialCaptureC2SPayload;
import appeng.core.network.payload.SpatialOpCancelC2SPayload;
import appeng.core.network.payload.SpatialOpCancelS2CPayload;
import appeng.core.network.payload.SpatialOpCompleteS2CPayload;
import appeng.core.network.payload.SpatialOpInProgressS2CPayload;
import appeng.core.network.payload.SpatialRestoreC2SPayload;
import appeng.core.network.payload.StorageBusStateS2CPayload;
import appeng.crafting.CraftingJob;
import appeng.crafting.CraftingJobManager;
import appeng.items.patterns.EncodedPatternItem;
import appeng.menu.PartitionedCellMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.PatternEncodingTerminalMenu;
import appeng.menu.implementations.StorageBusMenu;
import appeng.menu.terminal.PatternTerminalMenu;
import appeng.menu.spatial.SpatialIOPortMenu;
import appeng.blockentity.spatial.SpatialIOPortBlockEntity.LastAction;

public final class AE2NetworkHandlers {
    private AE2NetworkHandlers() {
    }

    // S2C handler runs on client
    public static void handleHelloClient(final AE2HelloS2CPayload payload, final IPayloadContext ctx) {
        // Ensure on main client thread
        ctx.enqueueWork(() -> {
            // Example: update a client-side cache or open a screen
            // AE2ClientCache.setGreeting(payload.message());
        });
        ctx.setPacketHandled(true);
    }

    // C2S handler runs on server
    public static void handleActionServer(final AE2ActionC2SPayload payload, final IPayloadContext ctx) {
        // Validate and perform server-side action
        // AE2ServerActions.perform(payload, ctx.player());
        ctx.setPacketHandled(true);
    }

    // Login S2C -> client
    public static void handleLoginSyncClient(final AE2LoginSyncS2CPayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            // Apply synced server settings before play starts
            // AE2ClientConfig.apply(payload.configBlob());
        });
        ctx.setPacketHandled(true);
    }

    // Login C2S -> server
    public static void handleLoginAckServer(final AE2LoginAckC2SPayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) {
                CraftingJobManager.getInstance().syncJobs(player);
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void handlePlanCraftingJobServer(final PlanCraftingJobC2SPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.containerMenu instanceof PatternTerminalMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }

            var slots = menu.getSlots(SlotSemantics.ENCODED_PATTERN);
            if (payload.slotIndex() < 0 || payload.slotIndex() >= slots.size()) {
                return;
            }

            Slot slot = slots.get(payload.slotIndex());
            var stack = slot.getItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof EncodedPatternItem)) {
                return;
            }

            CraftingJob job;
            try {
                job = CraftingJobManager.getInstance().planJob(stack);
            } catch (IllegalArgumentException ex) {
                AELog.warn("Failed to plan crafting job: {}", ex.getMessage());
                return;
            }

            AELog.info("Planned crafting job {} -> {}", job.getId(), job.describeOutputs());
            AE2Packets.sendPlannedCraftingJob(player, menu.containerId, job);
        });
        ctx.setPacketHandled(true);
    }

    public static void handleEncodePatternServer(final EncodePatternC2SPayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.containerMenu instanceof PatternEncodingTerminalMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }

            menu.encodeServer();
        });
        ctx.setPacketHandled(true);
    }

    public static void handleSetPatternEncodingModeServer(final SetPatternEncodingModeC2SPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.containerMenu instanceof PatternEncodingTerminalMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }

            menu.setProcessingModeServer(payload.processing());
        });
        ctx.setPacketHandled(true);
    }

    public static void handleSpatialCaptureServer(final SpatialCaptureC2SPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.containerMenu instanceof SpatialIOPortMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }
            if (!menu.getBlockPos().equals(payload.pos())) {
                return;
            }

            var port = menu.getBlockEntity();
            if (port != null && !port.isRemoved()) {
                port.onCapture();
                var regionSize = port.getCachedSize();
                menu.updateRegionSize(regionSize);
                menu.updateLastAction(port.getLastAction());
                menu.setInProgress(port.isInProgress());
                if (port.getLastAction() != LastAction.NONE) {
                    PacketDistributor.sendToPlayer(player,
                            new SpatialCaptureC2SPayload(menu.containerId, payload.pos(), regionSize));
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void handleSpatialRestoreServer(final SpatialRestoreC2SPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.containerMenu instanceof SpatialIOPortMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }
            if (!menu.getBlockPos().equals(payload.pos())) {
                return;
            }

            var port = menu.getBlockEntity();
            if (port != null && !port.isRemoved()) {
                port.onRestore();
                var regionSize = port.getCachedSize();
                menu.updateRegionSize(regionSize);
                menu.updateLastAction(port.getLastAction());
                menu.setInProgress(port.isInProgress());
                if (port.getLastAction() != LastAction.NONE) {
                    PacketDistributor.sendToPlayer(player,
                            new SpatialRestoreC2SPayload(menu.containerId, payload.pos(), regionSize));
                }
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void handleSpatialOpCancelServer(final SpatialOpCancelC2SPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!(player.containerMenu instanceof SpatialIOPortMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }
            if (!menu.getBlockPos().equals(payload.pos())) {
                return;
            }

            var port = menu.getBlockEntity();
            if (port != null && !port.isRemoved()) {
                port.cancelOperation();
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void handleSpatialCaptureClient(final SpatialCaptureC2SPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null) {
                return;
            }
            if (!(player.containerMenu instanceof SpatialIOPortMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }
            if (!menu.getBlockPos().equals(payload.pos())) {
                return;
            }

            menu.updateRegionSize(payload.regionSize());
            menu.updateLastAction(LastAction.CAPTURE);
        });
        ctx.setPacketHandled(true);
    }

    public static void handleSpatialRestoreClient(final SpatialRestoreC2SPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null) {
                return;
            }
            if (!(player.containerMenu instanceof SpatialIOPortMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }
            if (!menu.getBlockPos().equals(payload.pos())) {
                return;
            }

            menu.updateRegionSize(payload.regionSize());
            menu.updateLastAction(LastAction.RESTORE);
        });
        ctx.setPacketHandled(true);
    }

    public static void handleSpatialOpInProgressClient(final SpatialOpInProgressS2CPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null) {
                return;
            }
            if (!(player.containerMenu instanceof SpatialIOPortMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }
            if (!menu.getBlockPos().equals(payload.pos())) {
                return;
            }

            menu.setInProgress(payload.inProgress());
        });
        ctx.setPacketHandled(true);
    }

    public static void handleSpatialOpCompleteClient(final SpatialOpCompleteS2CPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null) {
                return;
            }
            if (!(player.containerMenu instanceof SpatialIOPortMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }
            if (!menu.getBlockPos().equals(payload.pos())) {
                return;
            }

            menu.handleOperationComplete();
        });
        ctx.setPacketHandled(true);
    }

    public static void handleSpatialOpCancelClient(final SpatialOpCancelS2CPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null) {
                return;
            }
            if (!(player.containerMenu instanceof SpatialIOPortMenu menu)) {
                return;
            }
            if (menu.containerId != payload.containerId()) {
                return;
            }
            if (!menu.getBlockPos().equals(payload.pos())) {
                return;
            }

            menu.handleOperationCancelled();
        });
        ctx.setPacketHandled(true);
    }

    public static void handlePlannedCraftingJobClient(final PlannedCraftingJobS2CPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null) {
                return;
            }

            MutableComponent message = Component.translatable("message.ae2.crafting_job_planned");
            message.append(" ").append(Component.literal(payload.jobId().toString()));
            if (!payload.outputs().isEmpty()) {
                message.append(": ").append(formatOutputs(payload.outputs()));
            }

            player.displayClientMessage(message, false);
        });
        ctx.setPacketHandled(true);
    }

    public static void handleJobUpdateClient(final S2CJobUpdatePayload payload, final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null) {
                return;
            }

            var trackerMessage = ClientCraftingJobTracker.update(payload);
            if (trackerMessage != null) {
                player.displayClientMessage(trackerMessage, false);
            }

            String translationKey = null;
            Object[] args = new Object[0];
            if (payload.state() == CraftingJob.State.RUNNING) {
                if (payload.ticksCompleted() == 0) {
                    translationKey = payload.processing() ? "message.ae2.processing_job_started"
                            : "message.ae2.crafting_job_started";
                    args = new Object[] { payload.jobId() };
                } else {
                    translationKey = payload.processing() ? "message.ae2.processing_job_progress"
                            : "message.ae2.crafting_job_progress";
                    args = new Object[] { payload.jobId(), payload.ticksCompleted(), payload.ticksRequired() };
                }
            } else if (payload.state() == CraftingJob.State.COMPLETE) {
                translationKey = payload.processing() ? "message.ae2.processing_job_complete"
                        : "message.ae2.crafting_job_complete";
                args = new Object[] { payload.jobId(), payload.insertedOutputs(), payload.droppedOutputs() };
            } else if (payload.state() == CraftingJob.State.FAILED) {
                translationKey = "message.ae2.crafting_job_failed";
                args = new Object[] { payload.jobId() };
            }

            if (payload.parentJobId() != null) {
                translationKey = null;
            }

            if (translationKey != null) {
                var message = Component.translatable(translationKey, args);
                if (payload.processing()) {
                    message = Component.translatable("tooltip.appliedenergistics2.processing_pattern_job")
                            .append(" ").append(message);
                }
                player.displayClientMessage(message, false);
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void handleCraftingJobSyncClient(final CraftingJobSyncS2CPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            for (var job : payload.jobs()) {
                ClientCraftingJobTracker.update(job);
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void handlePartitionedCellSyncClient(final PartitionedCellSyncS2CPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null || player.containerMenu == null) {
                return;
            }

            if (player.containerMenu.containerId != payload.containerId()) {
                return;
            }

            if (player.containerMenu instanceof PartitionedCellMenu menu) {
                menu.applySync(payload.priority(), payload.whitelistMode(), payload.whitelist());
            }
        });
        ctx.setPacketHandled(true);
    }

    public static void handleStorageBusStateClient(final StorageBusStateS2CPayload payload,
            final IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player == null || player.containerMenu == null) {
                return;
            }

            if (player.containerMenu.containerId != payload.containerId()) {
                return;
            }

            if (player.containerMenu instanceof StorageBusMenu menu) {
                menu.applyState(payload.accessMode(), payload.storageFilter(), payload.filterOnExtract(),
                        payload.connectedTo());
            }
        });
        ctx.setPacketHandled(true);
    }

    private static MutableComponent formatOutputs(List<ItemStackView> outputs) {
        MutableComponent result = Component.empty();
        boolean first = true;
        for (ItemStackView view : outputs) {
            if (!first) {
                result.append(Component.literal(", "));
            }
            MutableComponent entry = Component.literal(Integer.toString(view.count()))
                    .append("x ")
                    .append(Component.translatable(view.item().getDescriptionId()));
            result.append(entry);
            first = false;
        }
        return result;
    }
}
