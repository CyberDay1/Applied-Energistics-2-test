package appeng.blockentity;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.api.storage.IStorageHost;
import appeng.api.storage.IStorageService;
import appeng.registry.AE2BlockEntities;
import appeng.storage.impl.StorageService;
import appeng.grid.NodeType;
import appeng.grid.SimpleGridNode;
import appeng.util.GridHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ControllerBlockEntity extends BlockEntity implements IGridHost, IStorageHost {
    private final SimpleGridNode gridNode = new SimpleGridNode(NodeType.CONTROLLER);
    private final IStorageService storageService = new StorageService();
    private final long maxEnergy = 100000;
    private long storedEnergy;
    private boolean online;

    public ControllerBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.CONTROLLER.get(), pos, state);
    }

    @Override
    public IGridNode getGridNode() {
        return gridNode;
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
    public IStorageService getStorageService() {
        return storageService;
    }

    public long getStoredEnergy() {
        return storedEnergy;
    }

    public long receiveEnergy(long amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }

        long space = maxEnergy - storedEnergy;
        if (space <= 0) {
            return 0;
        }

        long toReceive = Math.min(space, amount);
        if (!simulate) {
            storedEnergy += toReceive;
        }
        return toReceive;
    }

    public long extractEnergy(long amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }

        long toExtract = Math.min(storedEnergy, amount);
        if (!simulate) {
            storedEnergy -= toExtract;
        }
        return toExtract;
    }

    public long available() {
        return storedEnergy;
    }

    public boolean isOnline() {
        return online && storedEnergy > 0;
    }

    public void setGridOnline(boolean online) {
        this.online = online;
    }
}
