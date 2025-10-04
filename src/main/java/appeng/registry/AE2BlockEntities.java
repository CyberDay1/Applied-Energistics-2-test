package appeng.registry;

import appeng.AE2Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.RegistryObject;

public final class AE2BlockEntities {
    public static final RegistryObject<BlockEntityType<?>> INSCRIBER_BE =
        AE2Registries.BLOCK_ENTITIES.register("inscriber",
            () -> BlockEntityType.Builder.of(
                // TODO: replace with InscriberBlockEntity::new
                (pos, state) -> null,
                AE2Blocks.INSCRIBER.get()
            ).build(null));

    private AE2BlockEntities() {}
}
