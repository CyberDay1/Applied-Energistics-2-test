package appeng.registry;

import appeng.AE2Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.RegistryObject;

public final class AE2Blocks {
    public static final RegistryObject<Block> CERTUS_QUARTZ_ORE = AE2Registries.BLOCKS.register(
        "certus_quartz_ore",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(3.0f, 3.0f)));

    public static final RegistryObject<Block> INSCRIBER = AE2Registries.BLOCKS.register(
        "inscriber",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));

    public static final RegistryObject<Block> CHARGER = AE2Registries.BLOCKS.register(
        "charger",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)));

    public static final RegistryObject<Block> SKY_STONE = AE2Registries.BLOCKS.register(
        "sky_stone",
        () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(50.0f, 1200.0f)));

    private AE2Blocks() {}
}
