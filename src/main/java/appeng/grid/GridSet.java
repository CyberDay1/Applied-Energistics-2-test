package appeng.grid;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import appeng.api.grid.IGridNode;

public final class GridSet {
    private final GridId id;
    private final Set<IGridNode> nodes = ConcurrentHashMap.newKeySet();
    private volatile boolean hasController;
    private volatile boolean online;
    private volatile long energyBudget;
    private volatile long tickCost;

    public GridSet(GridId id) {
        this.id = id;
    }

    public GridId id() {
        return id;
    }

    public void add(IGridNode n) {
        nodes.add(n);
    }

    public void remove(IGridNode n) {
        nodes.remove(n);
    }

    public Set<IGridNode> nodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public boolean hasController() {
        return hasController;
    }

    public void setHasController(boolean v) {
        hasController = v;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean v) {
        this.online = v;
    }

    public long energyBudget() {
        return energyBudget;
    }

    public void setEnergyBudget(long b) {
        energyBudget = b;
    }

    public long tickCost() {
        return tickCost;
    }

    public void setTickCost(long tickCost) {
        this.tickCost = tickCost;
    }

    public boolean recomputeOnline() {
        boolean newOnline = hasController && energyBudget >= tickCost;
        boolean changed = newOnline != this.online;
        this.online = newOnline;
        return changed;
    }
}
