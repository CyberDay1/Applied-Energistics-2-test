package appeng.core.network;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;

import appeng.api.storage.ItemStackView;
import appeng.core.AELog;
import appeng.core.network.AE2Packets;
import appeng.core.network.payload.AE2ActionC2SPayload;
import appeng.core.network.payload.AE2HelloS2CPayload;
import appeng.core.network.payload.AE2LoginAckC2SPayload;
import appeng.core.network.payload.AE2LoginSyncS2CPayload;
import appeng.core.network.payload.PlanCraftingJobC2SPayload;
import appeng.core.network.payload.PlannedCraftingJobS2CPayload;
import appeng.core.network.payload.S2CJobUpdatePayload;
import appeng.crafting.CraftingJob;
import appeng.crafting.CraftingJobManager;
import appeng.items.patterns.EncodedPatternItem;
import appeng.menu.SlotSemantics;
import appeng.menu.terminal.PatternTerminalMenu;

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
        // Optionally record that the client accepted sync
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

            String translationKey = null;
            Object[] args = new Object[0];
            if (payload.state() == CraftingJob.State.RUNNING) {
                if (payload.ticksCompleted() == 0) {
                    translationKey = "message.ae2.crafting_job_started";
                    args = new Object[] { payload.jobId() };
                } else {
                    translationKey = "message.ae2.crafting_job_progress";
                    args = new Object[] { payload.jobId(), payload.ticksCompleted(), payload.ticksRequired() };
                }
            } else if (payload.state() == CraftingJob.State.COMPLETE) {
                translationKey = "message.ae2.crafting_job_complete";
                args = new Object[] { payload.jobId(), payload.insertedOutputs(), payload.droppedOutputs() };
            }

            if (translationKey != null) {
                player.displayClientMessage(Component.translatable(translationKey, args), false);
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
