package appeng.blockentity.terminal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.RedstoneMode;
import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.api.storage.ItemStackView;
import appeng.grid.GridIndex;
import appeng.grid.NodeType;
import appeng.grid.SimpleGridNode;
import appeng.grid.SimpleGridNode.OfflineReason;
import appeng.registry.AE2BlockEntities;
import appeng.storage.impl.StorageService;
import appeng.util.GridHelper;

public class TerminalBlockEntity extends BlockEntity implements IGridHost {
    private final SimpleGridNode gridNode = new SimpleGridNode(NodeType.TERMINAL);
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;

    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.TERMINAL.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        GridHelper.discover(this);
        updateRedstoneState();
    }

    @Override
    public void setRemoved() {
        var gridId = gridNode.getGridId();
        super.setRemoved();
        if (gridId != null) {
            GridHelper.updateSetMetadata(gridId);
        }
    }

    @Override
    public IGridNode getGridNode() {
        return gridNode;
    }

    public void onNeighborChanged() {
        updateRedstoneState();
    }

    public void updateRedstoneState() {
        var level = getLevel();
        if (level == null) {
            return;
        }

        boolean powered = level.hasNeighborSignal(getBlockPos());
        boolean enabled = switch (redstoneMode) {
            case HIGH_SIGNAL -> powered;
            case LOW_SIGNAL -> !powered;
            default -> true;
        };

        if (gridNode.isRedstonePowered() != enabled) {
            gridNode.setRedstonePowered(enabled);
            if (!level.isClientSide()) {
                GridHelper.updateSetMetadata(gridNode.getGridId());
            }
        }
    }

    public OfflineReason getOfflineReason() {
        return gridNode.getOfflineReason();
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        if (this.redstoneMode != redstoneMode) {
            this.redstoneMode = redstoneMode;
            setChanged();
            updateRedstoneState();
        }
    }

    public List<ItemStackView> getStoredItems() {
        var gridId = gridNode.getGridId();
        var views = StorageService.getNetworkContents(gridId);
        Map<Item, Integer> combined = new HashMap<>();
        for (var view : views) {
            combined.merge(view.item(), view.count(), Integer::sum);
        }
        var result = new ArrayList<ItemStackView>();
        for (var entry : combined.entrySet()) {
            result.add(new ItemStackView(entry.getKey(), entry.getValue()));
        }
        result.sort(Comparator.comparing(view -> BuiltInRegistries.ITEM.getKey(view.item())));
        return result;
    }

    public boolean isGridOnline() {
        var gridId = gridNode.getGridId();
        if (gridId == null) {
            return false;
        }
        var set = GridIndex.get().get(gridId);
        return set != null && set.isOnline() && gridNode.hasChannel() && gridNode.isRedstonePowered();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("RedstoneMode")) {
            try {
                redstoneMode = RedstoneMode.valueOf(tag.getString("RedstoneMode"));
            } catch (IllegalArgumentException ignored) {
                redstoneMode = RedstoneMode.IGNORE;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("RedstoneMode", redstoneMode.name());
    }
}
