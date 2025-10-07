package appeng.blockentity.io;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.GridFlags;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.IConfigManager;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.menu.implementations.FormationPlaneMenuHost;
import appeng.util.ConfigInventory;

public class FormationPlaneBlockEntity extends AENetworkedBlockEntity implements FormationPlaneMenuHost {

    private final ConfigInventory config = ConfigInventory.configTypes(63)
            .changeListener(this::onConfigChanged)
            .build();

    private final IConfigManager configManager = IConfigManager.builder(this::onSettingChanged)
            .registerSetting(Settings.PLACE_BLOCK, YesNo.YES)
            .registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL)
            .build();

    private final IUpgradeInventory upgrades = UpgradeInventories
            .forMachine(AEBlocks.FORMATION_PLANE, 5, this::onUpgradesChanged);

    public FormationPlaneBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
    }

    private void onConfigChanged() {
        setChanged();
    }

    private void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        setChanged();
    }

    private void onUpgradesChanged() {
        setChanged();
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
}

