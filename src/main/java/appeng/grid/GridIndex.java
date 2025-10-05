package appeng.grid;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

public final class GridIndex {
    private static final GridIndex INSTANCE = new GridIndex();

    public static GridIndex get() {
        return INSTANCE;
    }

    private final Map<UUID, GridSet> sets = new ConcurrentHashMap<>();

    public GridSet getOrCreate(UUID id) {
        return sets.computeIfAbsent(id, k -> new GridSet(new GridId(k)));
    }

    @Nullable
    public GridSet get(UUID id) {
        return sets.get(id);
    }

    public Collection<GridSet> all() {
        return sets.values();
    }

    public void drop(UUID id) {
        sets.remove(id);
    }

    public void clear() {
        sets.clear();
    }
}
