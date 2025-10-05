package appeng.blockentity.io;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.behaviors.StackExportStrategy;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.IConfigManager;
import appeng.api.stacks.AEKey;
import appeng.core.settings.TickRates;
import appeng.core.definitions.AEItems;
import appeng.parts.automation.StackTransferContextImpl;
import appeng.parts.automation.StackWorldBehaviors;

public class ExportBusBlockEntity extends IOBusBlockEntity implements IGridTickable {

    private StackExportStrategy exportStrategy;
    private int nextSlot = 0;

    public ExportBusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    protected IConfigManager createConfigManager() {
        return IConfigManager.builder(this::onSettingChanged)
                .registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE)
                .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
                .registerSetting(Settings.CRAFT_ONLY, YesNo.NO)
                .registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT)
                .build();
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.ExportBus, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (!canDoBusWork()) {
            return TickRateModulation.SLEEP;
        }

        if (getConfigManager().getSetting(Settings.CRAFT_ONLY) == YesNo.YES) {
            return TickRateModulation.SLOWER;
        }

        IGrid grid = node.getGrid();
        if (grid == null) {
            return TickRateModulation.SLEEP;
        }

        var strategy = getExportStrategy();
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

        var schedulingMode = getConfigManager().getSetting(Settings.SCHEDULING_MODE);
        var totalSlots = getAvailableConfigSlots();
        var iterations = 0;
        var didWork = false;

        for (; iterations < totalSlots && context.hasOperationsLeft(); iterations++) {
            int slotIndex = selectSlot(schedulingMode, iterations, totalSlots);
            AEKey what = getConfig().getKey(slotIndex);
            if (what == null) {
                continue;
            }

            long transferFactor = what.getAmountPerOperation();
            long requestedAmount = (long) context.getOperationsRemaining() * transferFactor;
            long inserted = strategy.transfer(context, what, requestedAmount);

            if (inserted > 0) {
                didWork = true;
                long operationsUsed = Math.max(1, (inserted + transferFactor - 1) / transferFactor);
                context.reduceOperationsRemaining(operationsUsed);
            }
        }

        if (didWork) {
            advanceScheduling(schedulingMode, iterations, totalSlots);
        }

        return didWork ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    @Override
    protected void onTargetChanged() {
        super.onTargetChanged();
        exportStrategy = null;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putInt("nextSlot", nextSlot);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        nextSlot = data.getInt("nextSlot");
    }

    private StackExportStrategy getExportStrategy() {
        if (exportStrategy == null) {
            var level = getLevel();
            if (level instanceof ServerLevel serverLevel) {
                var fromPos = getBlockPos().relative(getFront());
                var fromSide = getFront().getOpposite();
                exportStrategy = StackWorldBehaviors.createExportFacade(serverLevel, fromPos, fromSide);
            }
        }

        return exportStrategy;
    }

    private int selectSlot(SchedulingMode mode, int iteration, int totalSlots) {
        return switch (mode) {
            case RANDOM -> getLevel().getRandom().nextInt(totalSlots);
            case ROUNDROBIN -> (nextSlot + iteration) % totalSlots;
            case DEFAULT -> iteration;
        };
    }

    private void advanceScheduling(SchedulingMode mode, int iterations, int totalSlots) {
        if (mode == SchedulingMode.ROUNDROBIN && totalSlots > 0) {
            nextSlot = (nextSlot + iterations) % totalSlots;
        }
    }
}
