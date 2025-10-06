package appeng.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.HashMap;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.api.storage.IStorageHost;
import appeng.blockentity.ControllerBlockEntity;
import appeng.blockentity.EnergyAcceptorBlockEntity;
import appeng.blockentity.simple.DriveBlockEntity;
import appeng.core.AELog;
import appeng.grid.GridIndex;
import appeng.grid.GridSet;
import appeng.grid.NodeType;
import appeng.grid.SimpleGridNode;
import appeng.grid.SimpleGridNode.OfflineReason;
import appeng.storage.impl.StorageService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class GridHelper {
    private static final Map<IGridNode, BlockEntity> HOSTS = Collections.synchronizedMap(new WeakHashMap<>());

    private GridHelper() {
    }

    public static void discover(BlockEntity be) {
        if (!(be instanceof IGridHost host)) {
            return;
        }

        IGridNode self = host.getGridNode();
        if (self == null) {
            return;
        }

        HOSTS.put(self, be);
        GridIndex.get().getOrCreate(self.getGridId()).add(self);

        Level level = be.getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        Set<UUID> touched = new HashSet<>();
        touched.add(self.getGridId());

        BlockPos pos = be.getBlockPos();
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor instanceof IGridHost nHost) {
                IGridNode neighborNode = nHost.getGridNode();
                if (neighborNode != null) {
                    HOSTS.putIfAbsent(neighborNode, neighbor);
                    merge(self, neighborNode);
                    touched.add(self.getGridId());
                    touched.add(neighborNode.getGridId());
                }
            }
        }

        for (UUID id : touched) {
            updateSetMetadata(id);
        }
    }

    private static void merge(IGridNode a, IGridNode b) {
        a.connect(b);
        b.connect(a);

        GridIndex index = GridIndex.get();
        index.getOrCreate(a.getGridId()).add(a);
        index.getOrCreate(b.getGridId()).add(b);
    }

    public static void updateSetMetadata(UUID gridId) {
        if (gridId == null) {
            return;
        }
        GridSet set = GridIndex.get().get(gridId);
        if (set == null) {
            return;
        }

        boolean hasController = false;
        long tickCost = 0;
        long energyBudget = 0;
        List<ControllerBlockEntity> controllers = new ArrayList<>();
        List<DriveBlockEntity> drives = new ArrayList<>();
        List<SimpleGridNode> controllerNodes = new ArrayList<>();
        List<SimpleGridNode> channelConsumers = new ArrayList<>();

        for (IGridNode node : set.nodes()) {
            BlockEntity host = HOSTS.get(node);
            if (host == null) {
                continue;
            }

            if (host instanceof IStorageHost storageHost) {
                var service = storageHost.getStorageService();
                if (service instanceof StorageService impl) {
                    impl.attachToGrid(node.getGridId());
                }
            }

            if (host instanceof ControllerBlockEntity controller) {
                hasController = true;
                controllers.add(controller);
                tickCost += 1;
                energyBudget += controller.available();
            }

            if (host instanceof DriveBlockEntity drive) {
                drives.add(drive);
            }

            if (host instanceof EnergyAcceptorBlockEntity acceptor) {
                energyBudget += acceptor.available();
            }

            if (node instanceof SimpleGridNode simpleNode) {
                simpleNode.resetChannelUsage();
                NodeType nodeType = simpleNode.getNodeType();
                if (nodeType == NodeType.MACHINE || nodeType == NodeType.TERMINAL) {
                    tickCost += 1;
                    channelConsumers.add(simpleNode);
                } else if (nodeType == NodeType.CONTROLLER) {
                    controllerNodes.add(simpleNode);
                }
            }
        }

        set.setHasController(hasController);
        set.setTickCost(tickCost);
        set.setEnergyBudget(energyBudget);
        boolean changed = set.recomputeOnline();

        for (ControllerBlockEntity controller : controllers) {
            controller.setGridOnline(set.isOnline());
        }

        if (changed) {
            AELog.debug("GridSet %s online=%s energyBudget=%s tickCost=%s",
                    gridId,
                    set.isOnline(),
                    energyBudget,
                    tickCost);
        }

        enforceChannelLimits(controllerNodes, channelConsumers);

        for (DriveBlockEntity drive : drives) {
            drive.notifyGridChanged();
        }

        var totals = StorageService.getTotals(gridId);
        set.setItemCellCapacity(totals.totalCapacity());
        set.setItemCellUsage(totals.used());

        AELog.debug("GridSet %s storage capacity=%s used=%s", gridId, totals.totalCapacity(), totals.used());
    }

    private static void enforceChannelLimits(List<SimpleGridNode> controllers,
            List<SimpleGridNode> consumers) {
        if (consumers.isEmpty()) {
            return;
        }

        if (controllers.isEmpty()) {
            for (SimpleGridNode consumer : consumers) {
                consumer.setHasChannel(false);
                consumer.setOfflineReason(
                        consumer.isRedstonePowered() ? OfflineReason.CHANNELS : OfflineReason.REDSTONE);
            }
            return;
        }

        for (SimpleGridNode consumer : consumers) {
            if (!consumer.isRedstonePowered()) {
                consumer.setHasChannel(false);
                consumer.setOfflineReason(OfflineReason.REDSTONE);
                continue;
            }

            boolean allocated = false;
            for (SimpleGridNode controller : controllers) {
                var path = findPathWithCapacity(consumer, controller);
                if (path != null && allocateAlongPath(path)) {
                    consumer.setHasChannel(true);
                    consumer.setOfflineReason(OfflineReason.NONE);
                    allocated = true;
                    break;
                }
            }

            if (!allocated) {
                consumer.setHasChannel(false);
                consumer.setOfflineReason(OfflineReason.CHANNELS);
            }
        }
    }

    private static boolean allocateAlongPath(List<SimpleGridNode> path) {
        List<SimpleGridNode> allocated = new ArrayList<>();
        for (SimpleGridNode node : path) {
            if (node.getNodeType() != NodeType.CABLE) {
                continue;
            }
            if (!node.tryAllocateChannel()) {
                for (SimpleGridNode allocatedNode : allocated) {
                    allocatedNode.releaseChannel();
                }
                return false;
            }
            allocated.add(node);
        }
        return true;
    }

    private static List<SimpleGridNode> findPathWithCapacity(SimpleGridNode start, SimpleGridNode target) {
        Map<SimpleGridNode, SimpleGridNode> previous = new HashMap<>();
        var queue = new ArrayDeque<SimpleGridNode>();
        queue.add(start);
        previous.put(start, null);

        while (!queue.isEmpty()) {
            var current = queue.poll();
            if (current == target) {
                break;
            }

            for (IGridNode neighborNode : current.neighbors()) {
                if (!(neighborNode instanceof SimpleGridNode neighbor)) {
                    continue;
                }
                if (previous.containsKey(neighbor)) {
                    continue;
                }
                if (neighbor.requiresChannel() && neighbor != target) {
                    continue;
                }
                if (neighbor.getNodeType() == NodeType.CABLE && neighbor.getRemainingChannelCapacity() <= 0) {
                    continue;
                }
                previous.put(neighbor, current);
                queue.add(neighbor);
            }
        }

        if (!previous.containsKey(target)) {
            return null;
        }

        List<SimpleGridNode> path = new ArrayList<>();
        SimpleGridNode current = target;
        while (current != null && current != start) {
            path.add(0, current);
            current = previous.get(current);
        }
        return path;
    }
}
