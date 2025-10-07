package appeng.blockentity.io;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.behaviors.StackImportStrategy;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEKeyType;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.settings.TickRates;
import appeng.me.helpers.MachineSource;
import appeng.menu.implementations.FormationPlaneMenuHost;
import appeng.parts.automation.StackTransferContextImpl;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.util.ConfigInventory;
import appeng.util.Platform;
import appeng.util.prioritylist.DefaultPriorityList;
import appeng.util.prioritylist.IPartitionList;

public class AnnihilationPlaneBlockEntity extends AENetworkedBlockEntity
        implements FormationPlaneMenuHost, IGridTickable {

    private final ConfigInventory config = ConfigInventory.configTypes(63)
            .supportedTypes(StackWorldBehaviors.withImportStrategy())
            .changeListener(this::onConfigChanged)
            .build();

    private final IConfigManager configManager = IConfigManager.builder(this::onSettingChanged)
            .registerSetting(Settings.PLACE_BLOCK, YesNo.YES)
            .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
            .registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE)
            .build();

    private final IUpgradeInventory upgrades = UpgradeInventories
            .forMachine(AEBlocks.ANNIHILATION_PLANE, 5, this::onUpgradesChanged);

    private final MachineSource actionSource = new MachineSource(this);

    @Nullable
    private StackImportStrategy importStrategy;

    private IPartitionList cachedFilter = DefaultPriorityList.INSTANCE;
    private boolean filterDirty = true;

    private boolean serverActive;
    private boolean clientActive;

    public AnnihilationPlaneBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
        getMainNode().addService(IGridTickable.class, this);
    }

    private void onConfigChanged() {
        setChanged();
        filterDirty = true;
        wakeDevice();
    }

    private void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        setChanged();
        filterDirty = true;
        wakeDevice();
    }

    private void onUpgradesChanged() {
        setChanged();
        filterDirty = true;
        resetImportStrategy();
        wakeDevice();
    }

    @Override
    public ConfigInventory getConfig() {
        return config;
    }

    @Override
    public IConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @Override
    public boolean isPlaneActive() {
        if (level != null && level.isClientSide()) {
            return clientActive;
        }
        return serverActive;
    }

    @Override
    public void onReady() {
        super.onReady();
        wakeDevice();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        wakeDevice();
    }

    public void onNeighborChanged() {
        resetImportStrategy();
        wakeDevice();
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        resetImportStrategy();
        wakeDevice();
    }

    private void wakeDevice() {
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
    }

    private void resetImportStrategy() {
        importStrategy = null;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(TickRates.AnnihilationPlane, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        var level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            updateActiveState(false);
            return TickRateModulation.SLEEP;
        }

        if (!getMainNode().isActive()) {
            updateActiveState(false);
            return TickRateModulation.SLEEP;
        }

        if (!isRedstoneEnabled()) {
            updateActiveState(false);
            return TickRateModulation.SLEEP;
        }

        Direction front = getFront();
        var targetPos = getBlockPos().relative(front);
        if (!Platform.areBlockEntitiesTicking(level, targetPos)) {
            updateActiveState(false);
            return TickRateModulation.SLEEP;
        }

        IGrid grid = node.getGrid();
        if (grid == null) {
            updateActiveState(false);
            return TickRateModulation.SLEEP;
        }

        var strategy = getImportStrategy(serverLevel, targetPos, front.getOpposite());
        if (strategy == null) {
            updateActiveState(false);
            return TickRateModulation.SLOWER;
        }

        var context = new StackTransferContextImpl(
                grid.getStorageService(),
                grid.getEnergyService(),
                actionSource,
                getOperationsPerTick(),
                getFilterList());

        boolean didWork = strategy.transfer(context);
        updateActiveState(true);

        return context.hasDoneWork() || didWork ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    private boolean isRedstoneEnabled() {
        if (!hasRedstoneUpgrade()) {
            return true;
        }

        var mode = configManager.getSetting(Settings.REDSTONE_CONTROLLED);
        if (mode == RedstoneMode.IGNORE) {
            return true;
        }

        var powered = level != null && level.hasNeighborSignal(getBlockPos());
        return switch (mode) {
            case HIGH_SIGNAL, SIGNAL_PULSE -> powered;
            case LOW_SIGNAL -> !powered;
            case IGNORE -> true;
        };
    }

    private void updateActiveState(boolean newState) {
        if (serverActive != newState) {
            serverActive = newState;
            setChanged();
            markForClientUpdate();
        }
        clientActive = newState;
    }

    @Nullable
    private StackImportStrategy getImportStrategy(ServerLevel level, BlockPos fromPos, Direction fromSide) {
        if (importStrategy == null) {
            importStrategy = StackWorldBehaviors.createImportFacade(level, fromPos, fromSide,
                    type -> type == AEKeyType.items() || type == AEKeyType.fluids());
        }

        return importStrategy;
    }

    private IPartitionList getFilterList() {
        if (filterDirty) {
            cachedFilter = buildFilter();
            filterDirty = false;
        }
        return cachedFilter;
    }

    private IPartitionList buildFilter() {
        var builder = IPartitionList.builder();
        if (hasFuzzyUpgrade()) {
            builder.fuzzyMode(configManager.getSetting(Settings.FUZZY_MODE));
        }

        for (int i = 0; i < config.size(); i++) {
            var key = config.getKey(i);
            if (key != null) {
                builder.add(key);
            }
        }

        return builder.build();
    }

    private int getOperationsPerTick() {
        var baseAmount = 8;
        var perCardIncrease = 8;
        return baseAmount + upgrades.getInstalledUpgrades(AEItems.CAPACITY_CARD) * perCardIncrease;
    }

    private boolean hasRedstoneUpgrade() {
        return upgrades.isInstalled(AEItems.REDSTONE_CARD);
    }

    private boolean hasFuzzyUpgrade() {
        return upgrades.isInstalled(AEItems.FUZZY_CARD);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putBoolean("active", serverActive);
        config.writeToChildTag(data, "config", registries);
        configManager.writeToNBT(data, registries);
        upgrades.writeToNBT(data, "upgrades", registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        serverActive = data.getBoolean("active");
        clientActive = serverActive;
        config.readFromChildTag(data, "config", registries);
        configManager.readFromNBT(data, registries);
        upgrades.readFromNBT(data, "upgrades", registries);
    }
}
