package appeng.capability.provider;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capability;

import appeng.capability.AE2Capabilities;
import appeng.api.networking.IInWorldGridNodeHost;

/**
 * Provides the {@link IInWorldGridNodeHost} capability for block entities that expose AE2 grid nodes.
 */
public final class InWorldGridNodeHostProvider extends AE2BlockEntityCapabilityProvider {
    private final IInWorldGridNodeHost host;

    public InWorldGridNodeHostProvider(BlockEntity be, IInWorldGridNodeHost host) {
        super(be);
        this.host = host;
    }

    @Override
    public <T> T getCapability(BlockEntity object, Capability<T> cap, @Nullable Direction side) {
        if (cap == AE2Capabilities.IN_WORLD_GRID_NODE_HOST) {
            // The block entity manages its own lifecycle; once it is removed the grid node is detached.
            return AE2Capabilities.IN_WORLD_GRID_NODE_HOST.cast(host);
        }
        return null;
    }
}
