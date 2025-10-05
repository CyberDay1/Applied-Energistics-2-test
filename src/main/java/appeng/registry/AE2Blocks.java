package appeng.registry;

import appeng.AE2Registries;
import appeng.block.CableBlock;
import appeng.block.ControllerBlock;
import appeng.block.EnergyAcceptorBlock;
import appeng.block.crafting.CraftingMonitorBlock;
import appeng.block.crafting.CraftingUnitType;
import appeng.block.crafting.PatternEncodingTerminalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.RegistryObject;

import appeng.block.simple.DriveBlock;
import appeng.block.terminal.CraftingTerminalBlock;
import appeng.block.terminal.PatternTerminalBlock;
import appeng.block.terminal.TerminalBlock;

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

    public static final RegistryObject<Block> SKY_STONE_CHEST = AE2Registries.BLOCKS.register(
        "sky_stone_chest",
        () -> new ChestBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)
                .mapColor(MapColor.COLOR_BLACK)
                .sound(SoundType.STONE)
                .strength(5.0f)
                .noOcclusion(),
            () -> BlockEntityType.CHEST,
            BlockSetType.STONE));

    public static final RegistryObject<Block> CONTROLLER =
        AE2Registries.BLOCKS.register("controller", ControllerBlock::new);

    public static final RegistryObject<Block> ENERGY_ACCEPTOR =
        AE2Registries.BLOCKS.register("energy_acceptor", EnergyAcceptorBlock::new);

    public static final RegistryObject<Block> CABLE =
        AE2Registries.BLOCKS.register("cable", CableBlock::new);

    public static final RegistryObject<Block> DRIVE =
        AE2Registries.BLOCKS.register("drive", DriveBlock::new);

    public static final RegistryObject<Block> TERMINAL =
        AE2Registries.BLOCKS.register("terminal", TerminalBlock::new);

    public static final RegistryObject<Block> CRAFTING_TERMINAL =
        AE2Registries.BLOCKS.register("crafting_terminal", CraftingTerminalBlock::new);

    public static final RegistryObject<Block> PATTERN_TERMINAL =
        AE2Registries.BLOCKS.register("pattern_terminal", PatternTerminalBlock::new);

    public static final RegistryObject<Block> PATTERN_ENCODING_TERMINAL =
        AE2Registries.BLOCKS.register("pattern_encoding_terminal", PatternEncodingTerminalBlock::new);

    public static final RegistryObject<Block> CRAFTING_MONITOR = AE2Registries.BLOCKS.register("crafting_monitor",
            () -> new CraftingMonitorBlock(CraftingUnitType.MONITOR));

    private AE2Blocks() {}
}
