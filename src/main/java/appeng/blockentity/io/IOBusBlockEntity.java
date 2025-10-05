package appeng.blockentity.io;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.helpers.IConfigInvHost;
import appeng.util.ConfigInventory;

public abstract class IOBusBlockEntity extends AENetworkedBlockEntity
        implements IConfigInvHost, IConfigurableObject, IUpgradeableObject {

    private final ConfigInventory config = ConfigInventory.configTypes(63).changeListener(this::onConfigChanged).build();
    private final IConfigManager configManager;

    protected IOBusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.configManager = createConfigManager();
    }

    protected IConfigManager createConfigManager() {
        return IConfigManager.builder(this::onSettingChanged)
                .registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE)
                .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
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
        wakeDevice();
    }

    protected void onSettingsChanged() {
        wakeDevice();
    }

    protected void wakeDevice() {
        getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
    }

    public void onNeighborChanged() {
        wakeDevice();
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        config.writeToChildTag(data, "config", registries);
        configManager.writeToNBT(data, registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        config.readFromChildTag(data, "config", registries);
        configManager.readFromNBT(data, registries);
    }

    @Override
    public ConfigInventory getConfig() {
        return config;
    }

    @Override
    public IConfigManager getConfigManager() {
        return configManager;
    }
}
