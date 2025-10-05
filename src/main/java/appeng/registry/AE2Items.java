package appeng.registry;

import appeng.AE2Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.neoforge.registries.RegistryObject;

import appeng.items.patterns.BlankPatternItem;
import appeng.items.patterns.EncodedPatternItem;
import appeng.items.storage.BasicCell16kItem;
import appeng.items.storage.BasicCell1kItem;
import appeng.items.storage.BasicCell4kItem;
import appeng.items.storage.BasicCell64kItem;
import appeng.items.storage.PartitionedCellItem;
import appeng.items.upgrades.CapacityCardItem;
import appeng.items.upgrades.FuzzyCardItem;
import appeng.items.upgrades.RedstoneCardItem;
import appeng.items.upgrades.SpeedCardItem;

public final class AE2Items {
    public static final RegistryObject<Item> CERTUS_QUARTZ_CRYSTAL = AE2Registries.ITEMS.register(
            "certus_quartz_crystal",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> CHARGED_CERTUS_QUARTZ_CRYSTAL = AE2Registries.ITEMS.register(
            "charged_certus_quartz_crystal",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> SILICON = AE2Registries.ITEMS.register(
            "silicon",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> PRINTED_SILICON = AE2Registries.ITEMS.register(
            "printed_silicon",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> INSCRIBER_SILICON_PRESS = AE2Registries.ITEMS.register(
            "inscriber_silicon_press",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> INSCRIBER_LOGIC_PRESS = AE2Registries.ITEMS.register(
            "inscriber_logic_press",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> PRINTED_LOGIC_PROCESSOR = AE2Registries.ITEMS.register(
            "printed_logic_processor",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> LOGIC_PROCESSOR = AE2Registries.ITEMS.register(
            "logic_processor",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> INSCRIBER_ENGINEERING_PRESS = AE2Registries.ITEMS.register(
            "inscriber_engineering_press",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> PRINTED_ENGINEERING_PROCESSOR = AE2Registries.ITEMS.register(
            "printed_engineering_processor",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> ENGINEERING_PROCESSOR = AE2Registries.ITEMS.register(
            "engineering_processor",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> INSCRIBER_CALCULATION_PRESS = AE2Registries.ITEMS.register(
            "inscriber_calculation_press",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> PRINTED_CALCULATION_PROCESSOR = AE2Registries.ITEMS.register(
            "printed_calculation_processor",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> CALCULATION_PROCESSOR = AE2Registries.ITEMS.register(
            "calculation_processor",
            () -> new Item(new Properties()));

    public static final RegistryObject<Item> REDSTONE_CARD = AE2Registries.ITEMS.register(
            "redstone_card",
            () -> new RedstoneCardItem(new Properties()));

    public static final RegistryObject<Item> CAPACITY_CARD = AE2Registries.ITEMS.register(
            "capacity_card",
            () -> new CapacityCardItem(new Properties()));

    public static final RegistryObject<Item> SPEED_CARD = AE2Registries.ITEMS.register(
            "speed_card",
            () -> new SpeedCardItem(new Properties()));

    public static final RegistryObject<Item> FUZZY_CARD = AE2Registries.ITEMS.register(
            "fuzzy_card",
            () -> new FuzzyCardItem(new Properties()));

    public static final RegistryObject<Item> CERTUS_QUARTZ_ORE = AE2Registries.ITEMS.register(
            "certus_quartz_ore",
            () -> new BlockItem(AE2Blocks.CERTUS_QUARTZ_ORE.get(), new Properties()));

    public static final RegistryObject<Item> INSCRIBER = AE2Registries.ITEMS.register(
            "inscriber",
            () -> new BlockItem(AE2Blocks.INSCRIBER.get(), new Properties()));

    public static final RegistryObject<Item> CHARGER = AE2Registries.ITEMS.register(
            "charger",
            () -> new BlockItem(AE2Blocks.CHARGER.get(), new Properties()));

    public static final RegistryObject<Item> SKY_STONE = AE2Registries.ITEMS.register(
            "sky_stone",
            () -> new BlockItem(AE2Blocks.SKY_STONE.get(), new Properties()));
    public static final RegistryObject<Item> SKY_STONE_CHEST = AE2Registries.ITEMS.register(
            "sky_stone_chest",
            () -> new BlockItem(AE2Blocks.SKY_STONE_CHEST.get(), new Properties()));

    public static final RegistryObject<Item> CONTROLLER = AE2Registries.ITEMS.register(
            "controller",
            () -> new BlockItem(AE2Blocks.CONTROLLER.get(), new Properties()));

    public static final RegistryObject<Item> ENERGY_ACCEPTOR = AE2Registries.ITEMS.register(
            "energy_acceptor",
            () -> new BlockItem(AE2Blocks.ENERGY_ACCEPTOR.get(), new Properties()));

    public static final RegistryObject<Item> CABLE = AE2Registries.ITEMS.register(
            "cable",
            () -> new BlockItem(AE2Blocks.CABLE.get(), new Properties()));

    public static final RegistryObject<Item> DRIVE = AE2Registries.ITEMS.register(
            "drive",
            () -> new BlockItem(AE2Blocks.DRIVE.get(), new Properties()));

    public static final RegistryObject<Item> TERMINAL = AE2Registries.ITEMS.register(
            "terminal",
            () -> new BlockItem(AE2Blocks.TERMINAL.get(), new Properties()));

    public static final RegistryObject<Item> CRAFTING_TERMINAL = AE2Registries.ITEMS.register(
            "crafting_terminal",
            () -> new BlockItem(AE2Blocks.CRAFTING_TERMINAL.get(), new Properties()));

    public static final RegistryObject<Item> PATTERN_TERMINAL = AE2Registries.ITEMS.register(
            "pattern_terminal",
            () -> new BlockItem(AE2Blocks.PATTERN_TERMINAL.get(), new Properties()));

    public static final RegistryObject<Item> PATTERN_ENCODING_TERMINAL_BLOCK = AE2Registries.ITEMS.register(
            "pattern_encoding_terminal_block",
            () -> new BlockItem(AE2Blocks.PATTERN_ENCODING_TERMINAL.get(), new Properties()));

    public static final RegistryObject<Item> CRAFTING_MONITOR = AE2Registries.ITEMS.register(
            "crafting_monitor",
            () -> new BlockItem(AE2Blocks.CRAFTING_MONITOR.get(), new Properties()));

    public static final RegistryObject<Item> BLANK_PATTERN = AE2Registries.ITEMS.register(
            "blank_pattern",
            () -> new BlankPatternItem(new Properties()));

    public static final RegistryObject<Item> ENCODED_PATTERN = AE2Registries.ITEMS.register(
            "encoded_pattern",
            () -> new EncodedPatternItem(new Properties()));

    public static final RegistryObject<Item> BASIC_CELL_1K = AE2Registries.ITEMS.register(
            "basic_cell_1k",
            () -> new BasicCell1kItem(new Properties().stacksTo(1)));

    public static final RegistryObject<Item> BASIC_CELL_4K = AE2Registries.ITEMS.register(
            "basic_cell_4k",
            () -> new BasicCell4kItem(new Properties().stacksTo(1)));

    public static final RegistryObject<Item> BASIC_CELL_16K = AE2Registries.ITEMS.register(
            "basic_cell_16k",
            () -> new BasicCell16kItem(new Properties().stacksTo(1)));

    public static final RegistryObject<Item> BASIC_CELL_64K = AE2Registries.ITEMS.register(
            "basic_cell_64k",
            () -> new BasicCell64kItem(new Properties().stacksTo(1)));

    public static final RegistryObject<Item> PARTITIONED_CELL = AE2Registries.ITEMS.register(
            "partitioned_cell",
            () -> new PartitionedCellItem(new Properties().stacksTo(1)));

    private AE2Items() {}
}
