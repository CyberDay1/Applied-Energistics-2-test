package appeng.registry;

import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.RegistryObject;

import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.recipes.game.StorageCellDisassemblyRecipe;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;
import appeng.recipes.transform.TransformRecipe;

public final class AE2RecipeTypes {
    private AE2RecipeTypes() {
    }

    public static final RegistryObject<RecipeType<InscriberRecipe>> INSCRIBER =
            AE2Registries.RECIPE_TYPES.register("inscriber", () -> InscriberRecipe.TYPE);

    public static final RegistryObject<RecipeType<ChargerRecipe>> CHARGER =
            AE2Registries.RECIPE_TYPES.register("charger", () -> ChargerRecipe.TYPE);

    public static final RegistryObject<RecipeType<EntropyRecipe>> ENTROPY =
            AE2Registries.RECIPE_TYPES.register("entropy", () -> EntropyRecipe.TYPE);

    public static final RegistryObject<RecipeType<TransformRecipe>> TRANSFORM =
            AE2Registries.RECIPE_TYPES.register("transform", () -> TransformRecipe.TYPE);

    public static final RegistryObject<RecipeType<MatterCannonAmmo>> MATTER_CANNON_AMMO = AE2Registries.RECIPE_TYPES
            .register("matter_cannon", () -> MatterCannonAmmo.TYPE);

    public static final RegistryObject<RecipeType<QuartzCuttingRecipe>> QUARTZ_CUTTING = AE2Registries.RECIPE_TYPES
            .register("quartz_cutting", () -> QuartzCuttingRecipe.TYPE);

    public static final RegistryObject<RecipeType<CraftingUnitTransformRecipe>> CRAFTING_UNIT_TRANSFORM =
            AE2Registries.RECIPE_TYPES.register("crafting_unit_transform",
                    () -> CraftingUnitTransformRecipe.TYPE);

    public static final RegistryObject<RecipeType<StorageCellDisassemblyRecipe>> CELL_DISASSEMBLY =
            AE2Registries.RECIPE_TYPES.register("storage_cell_disassembly",
                    () -> StorageCellDisassemblyRecipe.TYPE);

    public static void init() {
        // Ensure class loading
    }
}
