package appeng.registry;

import appeng.AE2Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.neoforged.neoforge.registries.RegistryObject;

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

    public static final RegistryObject<Item> CONTROLLER = AE2Registries.ITEMS.register(
            "controller",
            () -> new BlockItem(AE2Blocks.CONTROLLER.get(), new Properties()));

    public static final RegistryObject<Item> ENERGY_ACCEPTOR = AE2Registries.ITEMS.register(
            "energy_acceptor",
            () -> new BlockItem(AE2Blocks.ENERGY_ACCEPTOR.get(), new Properties()));

    public static final RegistryObject<Item> CABLE = AE2Registries.ITEMS.register(
            "cable",
            () -> new BlockItem(AE2Blocks.CABLE.get(), new Properties()));

    private AE2Items() {}
}
