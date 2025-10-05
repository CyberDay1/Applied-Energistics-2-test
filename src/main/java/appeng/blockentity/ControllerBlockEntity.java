package appeng.blockentity;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.api.storage.IStorageHost;
import appeng.api.storage.IStorageService;
import appeng.registry.AE2BlockEntities;
import appeng.storage.impl.StorageService;
import appeng.grid.SimpleGridNode;
import appeng.util.GridHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ControllerBlockEntity extends BlockEntity implements IGridHost, IStorageHost {
    private final IGridNode gridNode = new SimpleGridNode();
    private final IStorageService storageService = new StorageService();

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
}
