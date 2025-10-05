package appeng.blockentity.io;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.util.IConfigManager;
import appeng.core.settings.TickRates;

public class ExportBusBlockEntity extends IOBusBlockEntity implements IGridTickable {

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
        return TickRateModulation.SLEEP;
    }
}
