package appeng.capability.provider;

import java.util.Objects;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

/**
 * Base implementation for capability providers that serve AE2 block entities.
 */
public abstract class AE2BlockEntityCapabilityProvider
        implements ICapabilityProvider<BlockEntity, Direction> {
    protected final BlockEntity be;

    protected AE2BlockEntityCapabilityProvider(BlockEntity be) {
        this.be = Objects.requireNonNull(be);
    }

    @Override
    public <T> T getCapability(BlockEntity object, Capability<T> cap, Direction side) {
        return null;
    }
}
