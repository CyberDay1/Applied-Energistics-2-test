package appeng.api.grid;

import org.jetbrains.annotations.Nullable;

/**
 * Basic host interface for exposing a single AE2 grid node via capabilities.
 */
public interface IGridHost {
    /**
     * @return The exposed grid node, or {@code null} if the host is not currently connected.
     */
    @Nullable
    IGridNode getGridNode();
}
