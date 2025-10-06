package appeng.crafting;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks the dependency relationships between crafting jobs. Dependencies form a directed acyclic graph where edges
 * point from a parent job to sub-jobs that must finish before the parent can continue.
 */
public final class CraftingJobDependencyGraph {
    private final Map<UUID, Node> nodes = new HashMap<>();

    public synchronized void addJob(CraftingJob job) {
        Objects.requireNonNull(job, "job");
        nodes.computeIfAbsent(job.getId(), Node::new);
    }

    public synchronized void removeJob(UUID jobId) {
        if (jobId == null) {
            return;
        }

        Node node = nodes.remove(jobId);
        if (node == null) {
            return;
        }

        for (UUID parentId : node.parents) {
            Node parent = nodes.get(parentId);
            if (parent != null) {
                parent.children.remove(jobId);
            }
        }

        for (UUID childId : node.children) {
            Node child = nodes.get(childId);
            if (child != null) {
                child.parents.remove(jobId);
            }
        }
    }

    public synchronized void addDependency(UUID parentId, UUID childId) {
        Objects.requireNonNull(parentId, "parentId");
        Objects.requireNonNull(childId, "childId");

        if (parentId.equals(childId)) {
            throw new CraftingJobDependencyGraphException("Cannot link job to itself: " + parentId);
        }

        Node parent = nodes.computeIfAbsent(parentId, Node::new);
        Node child = nodes.computeIfAbsent(childId, Node::new);

        if (parent.children.contains(childId)) {
            return;
        }

        if (createsCycle(childId, parentId)) {
            throw new CraftingJobDependencyGraphException("Adding dependency from " + parentId + " to " + childId
                    + " would create a cycle");
        }

        parent.children.add(childId);
        child.parents.add(parentId);
    }

    public synchronized Set<UUID> getChildren(UUID jobId) {
        Node node = nodes.get(jobId);
        if (node == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(node.children));
    }

    public synchronized Set<UUID> getParents(UUID jobId) {
        Node node = nodes.get(jobId);
        if (node == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new HashSet<>(node.parents));
    }

    public synchronized boolean hasParents(UUID jobId) {
        Node node = nodes.get(jobId);
        return node != null && !node.parents.isEmpty();
    }

    private boolean createsCycle(UUID startId, UUID targetId) {
        if (startId.equals(targetId)) {
            return true;
        }

        Set<UUID> visited = new HashSet<>();
        Deque<UUID> toVisit = new ArrayDeque<>();
        toVisit.push(startId);

        while (!toVisit.isEmpty()) {
            UUID current = toVisit.pop();
            if (!visited.add(current)) {
                continue;
            }

            if (current.equals(targetId)) {
                return true;
            }

            Node node = nodes.get(current);
            if (node == null) {
                continue;
            }

            for (UUID parent : node.parents) {
                toVisit.push(parent);
            }
        }

        return false;
    }

    private static final class Node {
        final UUID id;
        final Set<UUID> parents = new HashSet<>();
        final Set<UUID> children = new HashSet<>();

        Node(UUID id) {
            this.id = id;
        }
    }
}
