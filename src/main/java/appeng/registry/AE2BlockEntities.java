package appeng.registry;

import appeng.AE2Registries;
import appeng.blockentity.ChargerBlockEntity;
import appeng.blockentity.InscriberBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.RegistryObject;

public final class AE2BlockEntities {
    public static final RegistryObject<BlockEntityType<InscriberBlockEntity>> INSCRIBER_BE =
        AE2Registries.BLOCK_ENTITIES.register("inscriber",
            () -> BlockEntityType.Builder.of(InscriberBlockEntity::new,
                AE2Blocks.INSCRIBER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ChargerBlockEntity>> CHARGER_BE =
        AE2Registries.BLOCK_ENTITIES.register("charger",
            () -> BlockEntityType.Builder.of(ChargerBlockEntity::new,
                AE2Blocks.CHARGER.get()).build(null));

    private AE2BlockEntities() {}
}
