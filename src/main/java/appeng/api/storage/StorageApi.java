package appeng.api.storage;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.AECapabilities;
import appeng.api.storage.MEStorage;
import appeng.core.AE2InteropValidator;

/**
 * Public helpers for looking up ME storage providers exposed through the capability system.
 */
public final class StorageApi {
    public static final StorageApi INSTANCE = new StorageApi();

    private StorageApi() {
        AE2InteropValidator.markBridgeInitialized("ME storage helpers");
    }

    public Optional<MEStorage> findStorage(Level level, BlockPos pos, @Nullable Direction side) {
        var blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(level.getCapability(AECapabilities.ME_STORAGE, pos,
                blockEntity.getBlockState(), blockEntity, side));
    }

    public Optional<MEStorage> findStorage(BlockEntity blockEntity, @Nullable Direction side) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return Optional.empty();
        }
        var level = blockEntity.getLevel();
        return Optional.ofNullable(level.getCapability(AECapabilities.ME_STORAGE, blockEntity.getBlockPos(),
                blockEntity.getBlockState(), blockEntity, side));
    }
}
