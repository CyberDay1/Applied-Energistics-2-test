package appeng.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.blockentity.ControllerBlockEntity;
import appeng.blockentity.EnergyAcceptorBlockEntity;
import appeng.core.AELog;
import appeng.grid.GridIndex;
import appeng.grid.GridSet;
import appeng.grid.NodeType;
import appeng.grid.SimpleGridNode;
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

    private static void updateSetMetadata(UUID gridId) {
        GridSet set = GridIndex.get().get(gridId);
        if (set == null) {
            return;
        }

        boolean hasController = false;
        long tickCost = 0;
        long energyBudget = 0;
        List<ControllerBlockEntity> controllers = new ArrayList<>();

        for (IGridNode node : set.nodes()) {
            BlockEntity host = HOSTS.get(node);
            if (host == null) {
                continue;
            }

            if (host instanceof ControllerBlockEntity controller) {
                hasController = true;
                controllers.add(controller);
                tickCost += 1;
                energyBudget += controller.available();
            }

            if (host instanceof EnergyAcceptorBlockEntity acceptor) {
                energyBudget += acceptor.available();
            }

            if (node instanceof SimpleGridNode simpleNode) {
                NodeType nodeType = simpleNode.getNodeType();
                if (nodeType == NodeType.MACHINE || nodeType == NodeType.TERMINAL) {
                    tickCost += 1;
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
    }
}
