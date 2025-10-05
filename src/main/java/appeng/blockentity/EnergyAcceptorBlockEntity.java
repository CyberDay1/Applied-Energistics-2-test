package appeng.blockentity;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.grid.SimpleGridNode;
import appeng.registry.AE2BlockEntities;
import appeng.util.GridHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class EnergyAcceptorBlockEntity extends BlockEntity implements IGridHost {
    private final IGridNode gridNode = new SimpleGridNode();
    private final EnergyBuffer buffer = new EnergyBuffer();

    public EnergyAcceptorBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.ENERGY_ACCEPTOR.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        GridHelper.discover(this);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        // TODO: Prune connections when nodes are removed
    }

    @Override
    public IGridNode getGridNode() {
        return gridNode;
    }

    public IEnergyStorage getEnergyStorage() {
        return buffer;
    }

    private static class EnergyBuffer implements IEnergyStorage {
        private int energy;

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = Math.min(1000 - energy, maxReceive);
            if (!simulate) {
                energy += received;
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return 1000;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
