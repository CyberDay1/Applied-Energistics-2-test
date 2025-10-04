package appeng.capability.provider;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capability;

import appeng.AE2Capabilities;
import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;

/**
 * Capability provider that exposes the placeholder grid node capability for AE2 block entities.
 */
public final class GridNodeCapabilityProvider extends AE2BlockEntityCapabilityProvider {
    private final IGridHost host;

    public GridNodeCapabilityProvider(BlockEntity be, IGridHost host) {
        super(be);
        this.host = host;
    }

    @Override
    public <T> T getCapability(BlockEntity object, Capability<T> cap, @Nullable Direction side) {
        if (cap == AE2Capabilities.GRID_NODE) {
            IGridNode node = host.getGridNode();
            if (node != null) {
                return AE2Capabilities.GRID_NODE.cast(node);
            }
        }
        return null;
    }
}
