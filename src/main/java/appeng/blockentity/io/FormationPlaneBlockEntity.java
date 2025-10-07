package appeng.blockentity.io;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.behaviors.PlacementStrategy;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.GridFlags;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.menu.implementations.FormationPlaneMenuHost;
import appeng.parts.automation.StackWorldBehaviors;
import appeng.util.ConfigInventory;
import appeng.util.Platform;
import appeng.util.prioritylist.DefaultPriorityList;
import appeng.util.prioritylist.IPartitionList;

public class FormationPlaneBlockEntity extends AENetworkedBlockEntity
        implements FormationPlaneMenuHost, ServerTickingBlockEntity {

    private final ConfigInventory config = ConfigInventory.configTypes(63)
            .supportedTypes(StackWorldBehaviors.withPlacementStrategy())
            .changeListener(this::onConfigChanged)
            .build();

    private final IConfigManager configManager = IConfigManager.builder(this::onSettingChanged)
            .registerSetting(Settings.PLACE_BLOCK, YesNo.YES)
            .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
            .registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE)
            .build();

    private final IUpgradeInventory upgrades = UpgradeInventories
            .forMachine(AEBlocks.FORMATION_PLANE, 5, this::onUpgradesChanged);

    private PlacementStrategy placementStrategy;
    private IncludeExclude filterMode = IncludeExclude.WHITELIST;
    private IPartitionList cachedFilter = DefaultPriorityList.INSTANCE;
    private boolean filterDirty = true;
    private boolean serverActive;
    private boolean clientActive;

    public FormationPlaneBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    private void onConfigChanged() {
        setChanged();
        filterDirty = true;
        clearPlacementStrategyBlockedCache();
    }

    private void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        setChanged();
        filterDirty = true;
    }

    private void onUpgradesChanged() {
        setChanged();
        filterDirty = true;
        filterMode = upgrades.isInstalled(AEItems.INVERTER_CARD)
                ? IncludeExclude.BLACKLIST
                : IncludeExclude.WHITELIST;
    }

    @Override
    public void onReady() {
        super.onReady();
        clientActive = serverActive;
    }

    @Override
    protected void onOrientationChanged(appeng.api.orientation.BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        resetPlacementStrategy();
    }

    @Override
    public void serverTick() {
        var level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            updateActiveState(false);
            return;
        }

        if (!getMainNode().isActive()) {
            updateActiveState(false);
            return;
        }

        if (!isRedstoneEnabled()) {
            updateActiveState(false);
            return;
        }

        Direction front = getFront();
        var targetPos = getBlockPos().relative(front);
        if (!Platform.areBlockEntitiesTicking(level, targetPos)) {
            updateActiveState(false);
            return;
        }

        var strategy = getPlacementStrategy(serverLevel, front);
        if (strategy == null) {
            updateActiveState(false);
            return;
        }

        // TODO: Implement extraction from the network and placement using the strategy.
        updateActiveState(false);
    }

    private boolean isRedstoneEnabled() {
        if (!hasRedstoneUpgrade()) {
            return true;
        }

        var mode = configManager.getSetting(Settings.REDSTONE_CONTROLLED);
        if (mode == RedstoneMode.IGNORE) {
            return true;
        }

        var level = getLevel();
        boolean powered = level != null && level.hasNeighborSignal(getBlockPos());

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

    private PlacementStrategy getPlacementStrategy(ServerLevel level, Direction front) {
        if (placementStrategy == null) {
            var owningPlayerId = getMainNode().getOwningPlayerProfileId();
            var fromPos = getBlockPos().relative(front);
            placementStrategy = StackWorldBehaviors.createPlacementStrategies(level, fromPos, front.getOpposite(), this,
                    owningPlayerId);
        }

        return placementStrategy;
    }

    private void resetPlacementStrategy() {
        placementStrategy = null;
    }

    private void clearPlacementStrategyBlockedCache() {
        if (placementStrategy != null) {
            placementStrategy.clearBlocked();
        }
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

        var slotsToUse = Math.min(config.size(), 18 + upgrades.getInstalledUpgrades(AEItems.CAPACITY_CARD) * 9);
        for (var i = 0; i < slotsToUse; i++) {
            var key = config.getKey(i);
            if (key != null) {
                builder.add(key);
            }
        }
        return builder.build();
    }

    private boolean hasRedstoneUpgrade() {
        return upgrades.isInstalled(AEItems.REDSTONE_CARD);
    }

    private boolean hasFuzzyUpgrade() {
        return upgrades.isInstalled(AEItems.FUZZY_CARD);
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

    public boolean isFilterInverted() {
        return filterMode == IncludeExclude.BLACKLIST;
    }

    public IPartitionList getCachedFilter() {
        return getFilterList();
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
        filterDirty = true;
    }
}

