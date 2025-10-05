package appeng.crafting.monitor;

import org.jetbrains.annotations.Nullable;

import appeng.api.stacks.GenericStack;

/**
 * Represents a single crafting job tracked by a {@link appeng.blockentity.crafting.CraftingMonitorBlockEntity}.
 *
 * <p>
 * This is a very small immutable data-transfer object that can be safely shared between the block entity, menu, and
 * networking code without introducing additional dependencies.
 * </p>
 */
public record CraftingMonitorEntry(@Nullable GenericStack stack, long totalItems, long completedItems,
        long elapsedTimeNanos, boolean done) {

    public float progress() {
        if (totalItems <= 0) {
            return 0f;
        }
        return Math.min(1f, (float) completedItems / (float) totalItems);
    }
}
