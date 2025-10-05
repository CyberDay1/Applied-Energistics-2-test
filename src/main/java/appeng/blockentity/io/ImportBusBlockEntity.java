package appeng.blockentity.io;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.networking.IGrid;
import appeng.core.settings.TickRates;
import appeng.api.behaviors.StackImportStrategy;
import appeng.parts.automation.StackTransferContextImpl;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.core.definitions.AEItems;

public class ImportBusBlockEntity extends IOBusBlockEntity implements IGridTickable {

    private StackImportStrategy importStrategy;

    public ImportBusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.ImportBus, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!canDoBusWork()) {
            return TickRateModulation.SLEEP;
        }

        IGrid grid = node.getGrid();
        if (grid == null) {
            return TickRateModulation.SLEEP;
        }

        var strategy = getImportStrategy();
        if (strategy == null) {
            return TickRateModulation.SLOWER;
        }

        var context = new StackTransferContextImpl(
                grid.getStorageService(),
                grid.getEnergyService(),
                source,
                getOperationsPerTick(),
                getFilterList());

        context.setInverted(isUpgradedWith(AEItems.INVERTER_CARD));

        strategy.transfer(context);

        return context.hasDoneWork() ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    @Override
    protected void onTargetChanged() {
        super.onTargetChanged();
        importStrategy = null;
    }

    private StackImportStrategy getImportStrategy() {
        if (importStrategy == null) {
            var level = getLevel();
            if (level instanceof ServerLevel serverLevel) {
                var fromPos = getBlockPos().relative(getFront());
                var fromSide = getFront().getOpposite();
                importStrategy = StackWorldBehaviors.createImportFacade(serverLevel, fromPos, fromSide, type -> true);
            }
        }

        return importStrategy;
    }
}
