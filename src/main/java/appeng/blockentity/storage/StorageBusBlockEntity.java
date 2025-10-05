package appeng.blockentity.storage;

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

import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.YesNo;
import appeng.api.orientation.BlockOrientation;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.StorageApi;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.helpers.IConfigInvHost;
import appeng.helpers.IPriorityHost;
import appeng.core.definitions.AEBlocks;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.StorageBusBlockMenu;
import appeng.menu.locator.MenuLocators;
import appeng.util.ConfigInventory;

public class StorageBusBlockEntity extends AENetworkedBlockEntity
        implements IStorageProvider, IConfigInvHost, IPriorityHost, IConfigurableObject {

    private final ConfigInventory config = ConfigInventory.configTypes(63).changeListener(this::onConfigChanged).build();
    private final IConfigManager configManager = IConfigManager.builder(this::onSettingsChanged)
            .registerSetting(Settings.ACCESS, AccessRestriction.READ_WRITE)
            .registerSetting(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY)
            .registerSetting(Settings.FILTER_ON_EXTRACT, YesNo.YES)
            .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
            .build();

    private int priority = 0;

    public StorageBusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        getMainNode().addService(IStorageProvider.class, this);
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

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        var level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        var front = getFront();
        var targetPos = getBlockPos().relative(front);
        StorageApi.INSTANCE.findStorage(level, targetPos, front.getOpposite()).ifPresent(storage -> {
            storageMounts.mount(storage, getPriority());
        });
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        config.writeToChildTag(data, "config", registries);
        data.putInt("priority", priority);
        configManager.writeToNBT(data, registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        config.readFromChildTag(data, "config", registries);
        priority = data.getInt("priority");
        configManager.readFromNBT(data, registries);
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
}
