package appeng.grid;

import appeng.api.grid.IGridNode;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple in-memory grid node implementation.
 */
public class SimpleGridNode implements IGridNode {
    private final Set<IGridNode> neighbors = new HashSet<>();

    @Override
    public void connect(IGridNode other) {
        if (other != null && other != this) {
            neighbors.add(other);
        }
    }

    @Override
    public Set<IGridNode> neighbors() {
        return Set.copyOf(neighbors);
    }
}
