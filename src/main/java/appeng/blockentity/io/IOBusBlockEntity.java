package appeng.blockentity.io;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNodeListener;
import appeng.api.orientation.BlockOrientation;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.api.networking.security.IActionSource;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.helpers.IConfigInvHost;
import appeng.me.helpers.MachineSource;
import appeng.util.ConfigInventory;
import appeng.util.Platform;
import appeng.util.prioritylist.DefaultPriorityList;
import appeng.util.prioritylist.IPartitionList;
import appeng.core.AELog;
import appeng.core.definitions.AEItems;

public abstract class IOBusBlockEntity extends AENetworkedBlockEntity
        implements IConfigInvHost, IConfigurableObject, IUpgradeableObject {

    private final ConfigInventory config = ConfigInventory.configTypes(63).changeListener(this::onConfigChanged).build();
    private final IConfigManager configManager;
    protected final IActionSource source = new MachineSource(this);
    private IPartitionList cachedFilter = DefaultPriorityList.INSTANCE;
    private boolean filterDirty = true;
    private final IUpgradeInventory upgrades;

    protected IOBusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.configManager = createConfigManager();
        this.upgrades = UpgradeInventories.forMachine(getUpgradeableItem(), getUpgradeSlotCount(), this::onUpgradesChanged);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    protected abstract ItemLike getUpgradeableItem();

    protected int getUpgradeSlotCount() {
        return 4;
    }

    protected IConfigManager createConfigManager() {
        return IConfigManager.builder(this::onSettingChanged)
                .registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE)
                .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
                .registerSetting(Settings.PARTITION_MODE, IncludeExclude.WHITELIST)
                .build();
    }

    private void onConfigChanged() {
        setChanged();
        onFiltersChanged();
    }

    protected void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        setChanged();
        onSettingsChanged();
    }

    protected void onFiltersChanged() {
        filterDirty = true;
        wakeDevice();
    }

    protected void onSettingsChanged() {
        filterDirty = true;
        wakeDevice();
    }

    protected void wakeDevice() {
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
    }

    public void onNeighborChanged() {
        onTargetChanged();
        wakeDevice();
    }

    protected void onTargetChanged() {
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        onTargetChanged();
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        config.writeToChildTag(data, "config", registries);
        configManager.writeToNBT(data, registries);
        upgrades.writeToNBT(data, "upgrades", registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        config.readFromChildTag(data, "config", registries);
        configManager.readFromNBT(data, registries);
        upgrades.readFromNBT(data, "upgrades", registries);
    }

    @Override
    public ConfigInventory getConfig() {
        return config;
    }

    @Override
    public IConfigManager getConfigManager() {
        return configManager;
    }

    protected IPartitionList getFilterList() {
        if (filterDirty) {
            cachedFilter = buildFilter();
            filterDirty = false;
        }
        return cachedFilter;
    }

    protected IPartitionList buildFilter() {
        var builder = IPartitionList.builder();
        if (hasFuzzyUpgrade()) {
            builder.fuzzyMode(getConfigManager().getSetting(Settings.FUZZY_MODE));
        }

        int slotsToUse = getAvailableConfigSlots();
        for (int i = 0; i < slotsToUse; i++) {
            builder.add(config.getKey(i));
        }

        return builder.build();
    }

    protected int getAvailableConfigSlots() {
        return Math.min(config.size(), 18 + getCapacityUpgradeCount() * 9);
    }

    public int getActiveConfigSlots() {
        return getAvailableConfigSlots();
    }

    public int getOperationsPerTransfer() {
        return getOperationsPerTick();
    }

    public int getTransferCooldownTicks() {
        return getTransferCooldown();
    }

    public IncludeExclude getPartitionMode() {
        return getConfigManager().getSetting(Settings.PARTITION_MODE);
    }

    public boolean hasFuzzyCard() {
        return hasFuzzyUpgrade();
    }

    public boolean hasRedstoneCard() {
        return hasRedstoneUpgrade();
    }

    public boolean hasInverterCard() {
        return isUpgradedWith(AEItems.INVERTER_CARD);
    }

    public boolean isFilterInverted() {
        return hasInverterCard() && getPartitionMode() == IncludeExclude.BLACKLIST;
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

    protected boolean canDoBusWork() {
        var level = getLevel();
        if (level == null || level.isClientSide()) {
            return false;
        }

        if (!getMainNode().isActive()) {
            return false;
        }

        if (!isRedstoneEnabled()) {
            return false;
        }

        Direction front = getFront();
        var targetPos = getBlockPos().relative(front);
        return Platform.areBlockEntitiesTicking(level, targetPos);
    }

    protected boolean isRedstoneEnabled() {
        if (!hasRedstoneUpgrade()) {
            return true;
        }

        var mode = getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
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

    public boolean isRedstoneActive() {
        return isRedstoneEnabled();
    }

    protected int getTransferCooldown() {
        var speedUpgrades = Math.min(getSpeedUpgradeCount(), 4);
        var baseCooldown = 20;
        var reductionPerCard = 5;
        var minCooldown = 5;

        var cooldown = baseCooldown - speedUpgrades * reductionPerCard;
        return Math.max(minCooldown, cooldown);
    }

    protected int getOperationsPerTick() {
        var baseAmount = 8;
        var perCardIncrease = 8;
        return baseAmount + getCapacityUpgradeCount() * perCardIncrease;
    }

    protected void logUpgradeEffects() {
        AELog.debug(
                "IO bus at {} upgrades: speed={}, capacity={}, redstone={}, fuzzy={}, cooldown={} ticks, ops/tick={}",
                getBlockPos(),
                getSpeedUpgradeCount(),
                getCapacityUpgradeCount(),
                hasRedstoneUpgrade(),
                hasFuzzyUpgrade(),
                getTransferCooldown(),
                getOperationsPerTick());
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    protected int getCapacityUpgradeCount() {
        return upgrades.getInstalledUpgrades(AEItems.CAPACITY_CARD);
    }

    protected int getSpeedUpgradeCount() {
        return upgrades.getInstalledUpgrades(AEItems.SPEED_CARD);
    }

    protected boolean hasRedstoneUpgrade() {
        return upgrades.isInstalled(AEItems.REDSTONE_CARD);
    }

    protected boolean hasFuzzyUpgrade() {
        return upgrades.isInstalled(AEItems.FUZZY_CARD);
    }

    private void onUpgradesChanged() {
        setChanged();
        onFiltersChanged();
        wakeDevice();
        logUpgradeEffects();
    }
}
