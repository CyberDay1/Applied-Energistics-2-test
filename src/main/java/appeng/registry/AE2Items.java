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

    public static final RegistryObject<Item> INSCRIBER_BLOCK_ITEM = AE2Registries.ITEMS.register(
        "inscriber",
        () -> new BlockItem(AE2Blocks.INSCRIBER.get(), new Properties()));

    private AE2Items() {}
}
