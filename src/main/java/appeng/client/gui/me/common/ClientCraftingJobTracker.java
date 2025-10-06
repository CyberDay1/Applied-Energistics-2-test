package appeng.client.gui.me.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.ItemStackView;
import appeng.core.network.payload.S2CJobUpdatePayload;
import appeng.crafting.CraftingJob;

/**
 * Client-side tracker used to display and announce crafting sub-job dependencies.
 */
public final class ClientCraftingJobTracker {
    private static final Map<UUID, ClientJob> JOBS = new HashMap<>();
    private static final int MAX_LINES = 12;

    private ClientCraftingJobTracker() {
    }

    @Nullable
    public static Component update(S2CJobUpdatePayload payload) {
        var job = JOBS.computeIfAbsent(payload.jobId(), ClientJob::new);

        CraftingJob.State previousState = job.state;

        job.state = payload.state();
        job.processing = payload.processing();
        job.parentId = payload.parentJobId();
        job.outputs = payload.outputs();
        job.subJobs = new HashMap<>(payload.subJobs());

        if (payload.parentJobId() != null) {
            var parent = JOBS.computeIfAbsent(payload.parentJobId(), ClientJob::new);
            parent.subJobs.put(payload.jobId(), mapStateToSubJobStatus(payload.state()));
        }

        for (var entry : payload.subJobs().entrySet()) {
            var child = JOBS.computeIfAbsent(entry.getKey(), ClientJob::new);
            child.parentId = payload.jobId();
        }

        if (payload.parentJobId() != null) {
            if (previousState == null) {
                return Component.translatable("message.appliedenergistics2.crafting_job.sub_job_spawning",
                        describeOutputs(job.outputs));
            }

            if (payload.state() == CraftingJob.State.RUNNING && previousState != CraftingJob.State.RUNNING) {
                return Component.translatable("message.appliedenergistics2.crafting_job.sub_job_running",
                        describeOutputs(job.outputs));
            }

            if (payload.state() == CraftingJob.State.COMPLETE && previousState != CraftingJob.State.COMPLETE) {
                return Component.translatable("message.appliedenergistics2.crafting_job.sub_job_completed",
                        describeOutputs(job.outputs));
            }

            if (payload.state() == CraftingJob.State.FAILED && previousState != CraftingJob.State.FAILED) {
                return Component.translatable("message.appliedenergistics2.crafting_job.sub_job_failed",
                        describeOutputs(job.outputs));
            }
        }

        return null;
    }

    public static List<DependencyLine> getDependencyLines() {
        var lines = new ArrayList<DependencyLine>();
        var visited = new HashSet<UUID>();

        var roots = JOBS.values().stream()
                .filter(job -> job.parentId == null)
                .sorted(Comparator.comparing(ClientJob::describe))
                .toList();

        for (var job : roots) {
            appendLines(job, 0, lines, visited);
            if (lines.size() >= MAX_LINES) {
                break;
            }
        }

        return lines;
    }

    private static void appendLines(ClientJob job, int depth, List<DependencyLine> lines, Set<UUID> visited) {
        if (job.state == null || !visited.add(job.id)) {
            return;
        }

        lines.add(new DependencyLine(formatLine(job, depth), colorForState(job.state)));
        if (lines.size() >= MAX_LINES) {
            return;
        }

        var children = new ArrayList<UUID>(job.subJobs.keySet());
        children.sort(Comparator.comparing(UUID::toString));
        for (var childId : children) {
            var child = JOBS.get(childId);
            if (child != null) {
                appendLines(child, depth + 1, lines, visited);
            } else {
                var status = job.subJobs.get(childId);
                String indent = "  ".repeat(depth + 1);
                String text = indent + "- " + childId + " ["
                        + (status != null ? status.name().toLowerCase(Locale.ROOT) : "unknown") + "]";
                lines.add(new DependencyLine(text, 0x888888));
            }

            if (lines.size() >= MAX_LINES) {
                break;
            }
        }
    }

    private static CraftingJob.SubJobStatus mapStateToSubJobStatus(CraftingJob.State state) {
        return switch (state) {
            case PLANNED -> CraftingJob.SubJobStatus.PLANNED;
            case RESERVED -> CraftingJob.SubJobStatus.RESERVED;
            case RUNNING -> CraftingJob.SubJobStatus.RUNNING;
            case COMPLETE -> CraftingJob.SubJobStatus.COMPLETE;
            case FAILED -> CraftingJob.SubJobStatus.FAILED;
        };
    }

    private static Component describeOutputs(List<ItemStackView> outputs) {
        if (outputs.isEmpty()) {
            return Component.translatable("message.appliedenergistics2.crafting_job.unknown_output");
        }

        var first = outputs.get(0);
        ItemStack stack = first.asStack();
        int remaining = Math.max(0, outputs.size() - 1);
        Component base = Component.literal(first.count() + "x ").append(stack.getHoverName());
        if (remaining > 0) {
            base = base.copy().append(Component.literal(" +" + remaining));
        }
        return base;
    }

    private static String formatLine(ClientJob job, int depth) {
        String indent = "  ".repeat(depth);
        String state = job.state != null ? job.state.name().toLowerCase(Locale.ROOT) : "unknown";
        return indent + "- " + describeOutputs(job.outputs).getString() + " [" + state + "]";
    }

    private static int colorForState(CraftingJob.State state) {
        Integer color = switch (state) {
            case COMPLETE -> ChatFormatting.GREEN.getColor();
            case FAILED -> ChatFormatting.RED.getColor();
            case RUNNING -> ChatFormatting.GOLD.getColor();
            case RESERVED -> ChatFormatting.YELLOW.getColor();
            default -> ChatFormatting.GRAY.getColor();
        };
        return color != null ? color : 0xFFFFFF;
    }

    private record ClientJob(UUID id, @Nullable CraftingJob.State state, boolean processing, @Nullable UUID parentId,
            List<ItemStackView> outputs, Map<UUID, CraftingJob.SubJobStatus> subJobs) {
        ClientJob(UUID id) {
            this(id, null, false, null, List.of(), Map.of());
        }

        String describe() {
            return describeOutputs(outputs).getString();
        }
    }

    public record DependencyLine(String text, int color) {
    }
}
