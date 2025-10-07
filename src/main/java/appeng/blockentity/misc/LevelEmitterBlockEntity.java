package appeng.blockentity.misc;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.behaviors.ContainerItemStrategies;
import appeng.api.config.LevelEmitterMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.inventories.InternalInventory;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.block.misc.LevelEmitterBlock;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;

public class LevelEmitterBlockEntity extends AENetworkedBlockEntity
        implements IConfigurableObject, InternalInventoryHost {

    private final IConfigManager configManager = IConfigManager.builder(this::onSettingChanged)
            .registerSetting(Settings.LEVEL_EMITTER_MODE, LevelEmitterMode.GREATER_OR_EQUAL)
            .build();

    private final AppEngInternalInventory filterInventory = new AppEngInternalInventory(this, 1, 1);

    private final IStorageWatcherNode storageWatcherNode = new IStorageWatcherNode() {
        @Override
        public void updateWatcher(IStackWatcher newWatcher) {
            storageWatcher = newWatcher;
            configureStorageWatcher();
            updateStoredAmount();
        }

        @Override
        public void onStackChange(AEKey what, long amount) {
            updateStoredAmount();
        }
    };

    private IStackWatcher storageWatcher;
    private long threshold;
    private long cachedAmount;
    private boolean emitting;

    public LevelEmitterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        getMainNode().setFlags();
        getMainNode().setVisualRepresentation(AEBlocks.LEVEL_EMITTER.stack());
        getMainNode().addService(IStorageWatcherNode.class, storageWatcherNode);
    }

    @Override
    public void onReady() {
        super.onReady();
        updateStoredAmount();
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.threshold = data.getLong("threshold");
        this.cachedAmount = data.getLong("cachedAmount");
        this.emitting = data.getBoolean("emitting");
        configManager.readFromNBT(data, registries);
        filterInventory.readFromNBT(data, "filter", registries);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putLong("threshold", threshold);
        data.putLong("cachedAmount", cachedAmount);
        data.putBoolean("emitting", emitting);
        configManager.writeToNBT(data, registries);
        filterInventory.writeToNBT(data, "filter", registries);
    }

    public void setThreshold(long newThreshold) {
        newThreshold = Math.max(0, newThreshold);
        if (this.threshold != newThreshold) {
            this.threshold = newThreshold;
            setChanged();
            evaluateEmission();
        }
    }

    public long getThreshold() {
        return threshold;
    }

    public LevelEmitterMode getMode() {
        return configManager.getSetting(Settings.LEVEL_EMITTER_MODE);
    }

    public void setMode(LevelEmitterMode mode) {
        configManager.putSetting(Settings.LEVEL_EMITTER_MODE, mode);
    }

    private void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        setChanged();
        evaluateEmission();
    }

    private void configureStorageWatcher() {
        if (this.storageWatcher == null) {
            return;
        }

        this.storageWatcher.reset();

        var filter = getFilterKey();
        if (filter == null) {
            this.storageWatcher.setWatchAll(true);
        } else {
            this.storageWatcher.add(filter);
        }
    }

    private void onFilterChanged() {
        if (isClientSide()) {
            return;
        }

        configureStorageWatcher();
        updateStoredAmount();
        saveChanges();
    }

    @Nullable
    public AEKey getFilterKey() {
        return getFilterKey(filterInventory.getStackInSlot(0));
    }

    @Nullable
    public static AEKey getFilterKey(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        var wrapped = GenericStack.unwrapItemStack(stack);
        if (wrapped != null) {
            return wrapped.what();
        }

        var containedFluid = ContainerItemStrategies.getContainedStack(stack, AEKeyType.fluids());
        if (containedFluid != null) {
            return containedFluid.what();
        }

        return AEItemKey.of(stack);
    }

    public InternalInventory getConfigInventory() {
        return filterInventory;
    }

    private void updateStoredAmount() {
        if (level == null || level.isClientSide()) {
            return;
        }

        if (!getMainNode().isReady()) {
            return;
        }

        var grid = getMainNode().getGrid();
        long total = 0;
        if (grid != null) {
            var filter = getFilterKey();
            if (filter != null) {
                total = grid.getStorageService().getCachedInventory().get(filter);
            } else {
                for (var entry : grid.getStorageService().getCachedInventory()) {
                    if (entry.getKey() instanceof AEItemKey) {
                        total += entry.getLongValue();
                        if (total < 0) {
                            total = Long.MAX_VALUE;
                            break;
                        }
                    }
                }
            }
        }

        if (this.cachedAmount != total) {
            this.cachedAmount = total;
            setChanged();
            evaluateEmission();
        } else {
            evaluateEmission();
        }
    }

    private void evaluateEmission() {
        var mode = getMode();
        boolean shouldEmit = mode.shouldEmit(cachedAmount, threshold);
        setEmitting(shouldEmit);
    }

    private void setEmitting(boolean shouldEmit) {
        if (this.emitting == shouldEmit) {
            return;
        }

        this.emitting = shouldEmit;
        setChanged();

        var level = getLevel();
        if (level == null) {
            return;
        }

        var state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof LevelEmitterBlock)) {
            return;
        }

        if (level.isClientSide()) {
            return;
        }

        if (state.getValue(LevelEmitterBlock.POWERED) != shouldEmit) {
            level.setBlock(worldPosition, state.setValue(LevelEmitterBlock.POWERED, shouldEmit), Block.UPDATE_ALL);
        }

        Platform.notifyBlocksOfNeighbors(level, worldPosition);
    }

    @Override
    public IConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        if (inv == filterInventory) {
            saveChanges();
        }
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv == filterInventory) {
            onFilterChanged();
        }
    }

    @Override
    public boolean isClientSide() {
        return super.isClientSide();
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (ISegmentedInventory.CONFIG.equals(id)) {
            return filterInventory;
        }

        return super.getSubInventory(id);
    }
}
