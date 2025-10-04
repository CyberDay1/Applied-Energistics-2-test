package appeng.recipe;

import appeng.AE2Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.RegistryObject;

public final class AE2RecipeTypes {
    public static final RegistryObject<RecipeType<InscriberRecipe>> INSCRIBER =
        AE2Registries.RECIPE_TYPES.register("inscriber", () -> new RecipeType<>() {});

    public static final RegistryObject<RecipeType<ChargerRecipe>> CHARGER =
        AE2Registries.RECIPE_TYPES.register("charger", () -> new RecipeType<>() {});

    private AE2RecipeTypes() {}
}
