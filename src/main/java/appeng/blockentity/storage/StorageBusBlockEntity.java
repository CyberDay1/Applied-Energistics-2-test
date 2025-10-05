package appeng.blockentity.storage;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.YesNo;
import appeng.api.orientation.BlockOrientation;
import appeng.api.networking.GridFlags;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.StorageApi;
import appeng.api.storage.MEStorage;
import appeng.api.behaviors.ExternalStorageStrategy;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.core.definitions.AEBlocks;
import appeng.core.AELog;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.StorageBusBlockMenu;
import appeng.menu.locator.MenuLocators;
import appeng.util.ConfigInventory;
import appeng.util.prioritylist.IPartitionList;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.NullInventory;
import appeng.me.storage.CompositeStorage;
import appeng.core.definitions.AEItems;
import appeng.parts.automation.StackWorldBehaviors;

public class StorageBusBlockEntity extends AENetworkedBlockEntity
        implements IStorageProvider, IConfigInvHost, IPriorityHost, IConfigurableObject, IUpgradeableObject {

    private final ConfigInventory config = ConfigInventory.configTypes(63).changeListener(this::onConfigChanged).build();
    private final IConfigManager configManager = IConfigManager.builder(this::onSettingsChanged)
            .registerSetting(Settings.ACCESS, AccessRestriction.READ_WRITE)
            .registerSetting(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY)
            .registerSetting(Settings.FILTER_ON_EXTRACT, YesNo.YES)
            .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
            .build();

    private int priority = 0;
    private final IUpgradeInventory upgrades;

    public StorageBusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
        getMainNode().addService(IStorageProvider.class, this);
        this.upgrades = UpgradeInventories.forMachine(AEBlocks.STORAGE_BUS, 4, this::onUpgradesChanged);
    }

    @Override
    public void onReady() {
        super.onReady();
        IStorageProvider.requestUpdate(getMainNode());
    }

    @Override
    protected void onOrientationChanged(BlockOrientation orientation) {
        super.onOrientationChanged(orientation);
        IStorageProvider.requestUpdate(getMainNode());
    }

    public void onNeighborChanged() {
        IStorageProvider.requestUpdate(getMainNode());
    }

    private void onConfigChanged() {
        setChanged();
        IStorageProvider.requestUpdate(getMainNode());
    }

    private void onSettingsChanged(IConfigManager manager, Setting<?> setting) {
        setChanged();
        IStorageProvider.requestUpdate(getMainNode());
    }

    private final MEInventoryHandler handler = new MEInventoryHandler(NullInventory.of());

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        var level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        updateMountedStorage();

        if (!(handler.getDelegate() instanceof NullInventory)) {
            storageMounts.mount(handler, getPriority());
        }
    }

    private void updateMountedStorage() {
        var level = getLevel();
        if (level == null) {
            handler.setDelegate(NullInventory.of());
            return;
        }

        var front = getFront();
        var targetPos = getBlockPos().relative(front);

        MEStorage newDelegate;
        if (level instanceof ServerLevel serverLevel) {
            newDelegate = StorageApi.INSTANCE
                    .findStorage(level, targetPos, front.getOpposite())
                    .orElseGet(() -> createExternalComposite(serverLevel, targetPos, front));
        } else {
            newDelegate = StorageApi.INSTANCE
                    .findStorage(level, targetPos, front.getOpposite())
                    .orElse(null);
        }

        if (newDelegate == null) {
            newDelegate = NullInventory.of();
        }

        handler.setDelegate(newDelegate);

        if (newDelegate instanceof NullInventory) {
            AELog.debug("Storage bus at {} has no adjacent storage to mount", getBlockPos());
        } else {
            var description = newDelegate.getDescription();
            AELog.debug("Storage bus at {} mounted {}", getBlockPos(),
                    description != null ? description.getString() : newDelegate);
        }

        var access = getConfigManager().getSetting(Settings.ACCESS);
        handler.setAllowExtraction(access.isAllowExtraction());
        handler.setAllowInsertion(access.isAllowInsertion());

        handler.setWhitelist(isUpgradedWith(AEItems.INVERTER_CARD) ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
        handler.setPartitionList(createFilter());

        var filterOnExtract = getConfigManager().getSetting(Settings.FILTER_ON_EXTRACT) == YesNo.YES;
        var extractableOnly = getConfigManager().getSetting(Settings.STORAGE_FILTER) == StorageFilter.EXTRACTABLE_ONLY;
        handler.setExtractFiltering(filterOnExtract, extractableOnly && filterOnExtract);
        handler.setVoidOverflow(isUpgradedWith(AEItems.VOID_CARD));
    }

    private MEStorage createExternalComposite(ServerLevel level, BlockPos targetPos, Direction front) {
        Map<AEKeyType, ExternalStorageStrategy> strategies = StackWorldBehaviors
                .createExternalStorageStrategies(level, targetPos, front.getOpposite());

        if (strategies.isEmpty()) {
            return NullInventory.of();
        }

        var storages = new IdentityHashMap<AEKeyType, MEStorage>(strategies.size());
        var extractableOnly = getConfigManager().getSetting(Settings.STORAGE_FILTER) == StorageFilter.EXTRACTABLE_ONLY;

        strategies.forEach((type, strategy) -> {
            var wrapper = strategy.createWrapper(extractableOnly, this::onExternalStorageChanged);
            if (wrapper != null) {
                storages.put(type, wrapper);
            }
        });

        if (storages.isEmpty()) {
            return NullInventory.of();
        }

        return new CompositeStorage(storages);
    }

    private void onExternalStorageChanged() {
        IStorageProvider.requestUpdate(getMainNode());
    }

    private void onUpgradesChanged() {
        setChanged();
        updateMountedStorage();
        IStorageProvider.requestUpdate(getMainNode());
    }

    private IPartitionList createFilter() {
        var builder = IPartitionList.builder();

        if (isUpgradedWith(AEItems.FUZZY_CARD)) {
            builder.fuzzyMode(getConfigManager().getSetting(Settings.FUZZY_MODE));
        }

        int slotsToUse = Math.min(config.size(), 18 + getUpgrades().getInstalledUpgrades(AEItems.CAPACITY_CARD) * 9);
        for (int i = 0; i < slotsToUse; i++) {
            builder.add(config.getKey(i));
        }

        return builder.build();
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        config.writeToChildTag(data, "config", registries);
        data.putInt("priority", priority);
        configManager.writeToNBT(data, registries);
        upgrades.writeToNBT(data, "upgrades", registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        config.readFromChildTag(data, "config", registries);
        priority = data.getInt("priority");
        configManager.readFromNBT(data, registries);
        upgrades.readFromNBT(data, "upgrades", registries);
    }

    @Override
    public ConfigInventory getConfig() {
        return config;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int newValue) {
        if (priority != newValue) {
            priority = newValue;
            setChanged();
            IStorageProvider.requestUpdate(getMainNode());
        }
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(StorageBusBlockMenu.TYPE, player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return AEBlocks.STORAGE_BUS.stack();
    }

    @Override
    public IConfigManager getConfigManager() {
        return configManager;
    }

    @Nullable
    public Component getConnectedToDescription() {
        var level = getLevel();
        if (level == null) {
            return null;
        }

        var front = getFront();
        var targetPos = getBlockPos().relative(front);
        var state = level.getBlockState(targetPos);
        if (state.isAir()) {
            return null;
        }

        var name = state.getBlock().getName();
        return Objects.requireNonNullElse(name, Component.empty());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        var level = getLevel();
        if (level != null && !level.isClientSide()) {
            IStorageProvider.requestUpdate(getMainNode());
        }
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }
}
