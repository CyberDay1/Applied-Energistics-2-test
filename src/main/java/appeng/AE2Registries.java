package appeng;

import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AE2Registries {
    public static final String MODID = "appliedenergistics2";

    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
        DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<net.minecraft.world.item.Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<net.minecraft.sounds.SoundEvent> SOUNDS =
        DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);
    public static final DeferredRegister<net.minecraft.world.item.crafting.RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
    public static final DeferredRegister<net.neoforged.neoforge.loot.GlobalLootModifierSerializer<?>> LOOT_MODIFIERS =
        DeferredRegister.create(Registries.LOOT_MODIFIER_SERIALIZER, MODID);

    private AE2Registries() {}
}
