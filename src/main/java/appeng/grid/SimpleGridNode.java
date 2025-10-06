package appeng.grid;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import appeng.api.grid.IGridNode;

/**
 * Simple in-memory grid node implementation.
 */
public class SimpleGridNode implements IGridNode {
    public enum OfflineReason {
        NONE,
        REDSTONE,
        CHANNELS
    }

    private final Set<IGridNode> neighbors = ConcurrentHashMap.newKeySet();
    private UUID gridId = GridId.random().id();
    private volatile NodeType nodeType;
    private volatile boolean requiresChannel;
    private volatile int channelCapacity;
    private volatile int usedChannels;
    private volatile boolean hasChannel = true;
    private volatile boolean redstonePowered = true;
    private volatile OfflineReason offlineReason = OfflineReason.NONE;

    public SimpleGridNode() {
        this(NodeType.UNKNOWN);
    }

    public SimpleGridNode(NodeType nodeType) {
        this.nodeType = nodeType;
        this.requiresChannel = EnumSet.of(NodeType.MACHINE, NodeType.TERMINAL).contains(nodeType);
        if (nodeType == NodeType.CABLE) {
            this.channelCapacity = 8;
        }
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
        this.requiresChannel = EnumSet.of(NodeType.MACHINE, NodeType.TERMINAL).contains(nodeType);
    }

    public boolean requiresChannel() {
        return requiresChannel;
    }

    public void setRequiresChannel(boolean requiresChannel) {
        this.requiresChannel = requiresChannel;
    }

    public int getChannelCapacity() {
        return channelCapacity;
    }

    public void setChannelCapacity(int channelCapacity) {
        this.channelCapacity = channelCapacity;
    }

    public int getUsedChannels() {
        return usedChannels;
    }

    public int getRemainingChannelCapacity() {
        return channelCapacity - usedChannels;
    }

    public void resetChannelUsage() {
        this.usedChannels = 0;
        this.hasChannel = true;
        this.offlineReason = OfflineReason.NONE;
    }

    public boolean tryAllocateChannel() {
        if (usedChannels >= channelCapacity) {
            return false;
        }
        usedChannels++;
        return true;
    }

    public void releaseChannel() {
        if (usedChannels > 0) {
            usedChannels--;
        }
    }

    public boolean hasChannel() {
        return !requiresChannel || hasChannel;
    }

    public void setHasChannel(boolean hasChannel) {
        this.hasChannel = hasChannel;
    }

    public boolean isRedstonePowered() {
        return redstonePowered;
    }

    public void setRedstonePowered(boolean redstonePowered) {
        this.redstonePowered = redstonePowered;
    }

    public OfflineReason getOfflineReason() {
        return offlineReason;
    }

    public void setOfflineReason(OfflineReason offlineReason) {
        this.offlineReason = offlineReason;
    }
}
