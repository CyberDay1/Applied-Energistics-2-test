package appeng.api.networking.interop;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.AECapabilities;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.core.AE2InteropValidator;

/**
 * Entry point for add-ons that want to interact with AE2 grid hosts without touching internal helpers.
 */
public final class GridApi {
    public static final GridApi INSTANCE = new GridApi();

    private GridApi() {
        AE2InteropValidator.markBridgeInitialized("Grid capability helpers");
    }

    public Optional<IInWorldGridNodeHost> findNodeHost(Level level, BlockPos pos) {
        return Optional.ofNullable(GridHelper.getNodeHost(level, pos));
    }

    public Optional<IGridNode> findExposedNode(Level level, BlockPos pos, Direction side) {
        return Optional.ofNullable(GridHelper.getExposedNode(level, pos, side));
    }

    public Optional<IInWorldGridNodeHost> findNodeHost(Level level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        if (blockEntity != null && blockEntity.getLevel() == level) {
            return Optional.ofNullable(blockEntity.getCapability(AECapabilities.IN_WORLD_GRID_NODE_HOST_ENTITY, null));
        }
        return findNodeHost(level, pos);
    }
}
