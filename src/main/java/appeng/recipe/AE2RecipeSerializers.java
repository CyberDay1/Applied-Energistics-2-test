package appeng.recipe;

import appeng.AE2Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.RegistryObject;

public final class AE2RecipeSerializers {
    public static final RegistryObject<RecipeSerializer<InscriberRecipe>> INSCRIBER =
        AE2Registries.RECIPE_SERIALIZERS.register("inscriber", InscriberRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ChargerRecipe>> CHARGER =
        AE2Registries.RECIPE_SERIALIZERS.register("charger", ChargerRecipe.Serializer::new);

    private AE2RecipeSerializers() {}
}
