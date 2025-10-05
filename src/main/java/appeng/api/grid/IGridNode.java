package appeng.api.grid;

import java.util.Set;
import java.util.UUID;

/**
 * Basic grid node abstraction used to connect blocks into a graph.
 */
public interface IGridNode {
    void connect(IGridNode other);

    Set<IGridNode> neighbors();

    UUID getGridId();
}
