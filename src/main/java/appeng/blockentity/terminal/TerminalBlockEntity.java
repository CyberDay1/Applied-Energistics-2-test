package appeng.blockentity.terminal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.grid.IGridHost;
import appeng.api.grid.IGridNode;
import appeng.api.storage.ItemStackView;
import appeng.grid.GridIndex;
import appeng.grid.NodeType;
import appeng.grid.SimpleGridNode;
import appeng.registry.AE2BlockEntities;
import appeng.storage.impl.StorageService;
import appeng.util.GridHelper;

public class TerminalBlockEntity extends BlockEntity implements IGridHost {
    private final SimpleGridNode gridNode = new SimpleGridNode(NodeType.TERMINAL);

    public TerminalBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.TERMINAL.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        GridHelper.discover(this);
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
        return set != null && set.isOnline();
    }
}
