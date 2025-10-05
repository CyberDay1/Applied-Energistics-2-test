package appeng.grid;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import appeng.api.grid.IGridNode;

/**
 * Simple in-memory grid node implementation.
 */
public class SimpleGridNode implements IGridNode {
    private final Set<IGridNode> neighbors = ConcurrentHashMap.newKeySet();
    private UUID gridId = GridId.random().id();
    private volatile NodeType nodeType;

    public SimpleGridNode() {
        this(NodeType.UNKNOWN);
    }

    public SimpleGridNode(NodeType nodeType) {
        this.nodeType = nodeType;
        GridIndex.get().getOrCreate(gridId).add(this);
    }

    @Override
    public void connect(IGridNode other) {
        if (other == null || other == this) {
            return;
        }

        neighbors.add(other);

        var otherId = other.getGridId();
        if (otherId != null && !Objects.equals(this.gridId, otherId)) {
            moveToGrid(otherId);
        }
    }

    private void moveToGrid(UUID newId) {
        if (newId == null || Objects.equals(this.gridId, newId)) {
            return;
        }

        var index = GridIndex.get();
        var oldId = this.gridId;
        this.gridId = newId;
        index.getOrCreate(newId).add(this);

        if (oldId != null && !Objects.equals(oldId, newId)) {
            var oldSet = index.get(oldId);
            if (oldSet != null) {
                oldSet.remove(this);
                if (oldSet.nodes().isEmpty()) {
                    index.drop(oldId);
                }
            }
        }
    }

    @Override
    public Set<IGridNode> neighbors() {
        return Collections.unmodifiableSet(neighbors);
    }

    @Override
    public UUID getGridId() {
        return gridId;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
}
