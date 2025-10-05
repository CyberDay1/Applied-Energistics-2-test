/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2024, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.crafting.cpu;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.blockentity.crafting.CraftingCPUBlockEntity;

/**
 * Represents a cluster of {@link CraftingCPUBlockEntity} instances arranged in a rectangular prism.
 */
public final class CraftingCPUMultiblock {
    private static final Logger LOG = LoggerFactory.getLogger(CraftingCPUMultiblock.class);

    private final CraftingCPUBlockEntity controller;
    private final Set<CraftingCPUBlockEntity> members;
    private final BlockPos minCorner;
    private final BlockPos maxCorner;
    private final int totalCapacity;

    private CraftingCPUMultiblock(CraftingCPUBlockEntity controller,
            Set<CraftingCPUBlockEntity> members,
            BlockPos minCorner,
            BlockPos maxCorner) {
        this.controller = Objects.requireNonNull(controller, "controller");
        this.members = Set.copyOf(members);
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
        this.totalCapacity = this.members.stream().mapToInt(CraftingCPUBlockEntity::getBaseCapacity).sum();
    }

    public static CraftingCPUMultiblock build(ServerLevel level, CraftingCPUBlockEntity controller) {
        var discovered = discoverMembers(level, controller);
        if (discovered.isRectangular()) {
            return new CraftingCPUMultiblock(controller, discovered.members, discovered.minCorner,
                    discovered.maxCorner);
        }

        LOG.debug("Crafting CPU cluster at {} is not rectangular; falling back to single controller.",
                controller.getBlockPos());
        return new CraftingCPUMultiblock(controller, Set.of(controller), controller.getBlockPos(),
                controller.getBlockPos());
    }

    private static DiscoveryResult discoverMembers(ServerLevel level, CraftingCPUBlockEntity controller) {
        var queue = new ArrayDeque<BlockPos>();
        var visited = new HashSet<BlockPos>();
        var members = new HashSet<CraftingCPUBlockEntity>();

        queue.add(controller.getBlockPos());
        visited.add(controller.getBlockPos());

        while (!queue.isEmpty()) {
            var currentPos = queue.poll();
            var be = level.getBlockEntity(currentPos);
            if (!(be instanceof CraftingCPUBlockEntity cpu)) {
                continue;
            }

            members.add(cpu);

            for (var direction : Direction.values()) {
                var neighborPos = currentPos.relative(direction);
                if (!visited.add(neighborPos)) {
                    continue;
                }

                if (level.getBlockEntity(neighborPos) instanceof CraftingCPUBlockEntity) {
                    queue.add(neighborPos);
                }
            }
        }

        var minCorner = computeMin(members);
        var maxCorner = computeMax(members);

        boolean rectangular = true;
        for (int x = minCorner.getX(); x <= maxCorner.getX() && rectangular; x++) {
            for (int y = minCorner.getY(); y <= maxCorner.getY() && rectangular; y++) {
                for (int z = minCorner.getZ(); z <= maxCorner.getZ(); z++) {
                    var pos = new BlockPos(x, y, z);
                    if (!(level.getBlockEntity(pos) instanceof CraftingCPUBlockEntity)) {
                        rectangular = false;
                        break;
                    }
                }
            }
        }

        return new DiscoveryResult(members, minCorner, maxCorner, rectangular);
    }

    private static BlockPos computeMin(Set<CraftingCPUBlockEntity> members) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        for (var member : members) {
            var pos = member.getBlockPos();
            if (pos.getX() < minX) {
                minX = pos.getX();
            }
            if (pos.getY() < minY) {
                minY = pos.getY();
            }
            if (pos.getZ() < minZ) {
                minZ = pos.getZ();
            }
        }
        return new BlockPos(minX, minY, minZ);
    }

    private static BlockPos computeMax(Set<CraftingCPUBlockEntity> members) {
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (var member : members) {
            var pos = member.getBlockPos();
            if (pos.getX() > maxX) {
                maxX = pos.getX();
            }
            if (pos.getY() > maxY) {
                maxY = pos.getY();
            }
            if (pos.getZ() > maxZ) {
                maxZ = pos.getZ();
            }
        }
        return new BlockPos(maxX, maxY, maxZ);
    }

    public void attach() {
        for (var member : members) {
            member.setCurrentMultiblock(this, member == controller);
        }
    }

    public void detach() {
        for (var member : members) {
            member.clearCurrentMultiblock(this);
        }
    }

    public CraftingCPUBlockEntity getController() {
        return controller;
    }

    public Set<CraftingCPUBlockEntity> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public BlockPos getMinCorner() {
        return minCorner;
    }

    public BlockPos getMaxCorner() {
        return maxCorner;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    private record DiscoveryResult(Set<CraftingCPUBlockEntity> members, BlockPos minCorner, BlockPos maxCorner,
            boolean rectangular) {
        boolean isRectangular() {
            return rectangular;
        }
    }
}
