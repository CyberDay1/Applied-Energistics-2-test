package appeng.registry;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.RegistryObject;

import appeng.core.definitions.AEItems;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.entropy.EntropyRecipeSerializer;
import appeng.recipes.game.AddItemUpgradeRecipe;
import appeng.recipes.game.AddItemUpgradeRecipeSerializer;
import appeng.recipes.game.CraftingUnitTransformRecipe;
import appeng.recipes.game.CraftingUnitTransformRecipeSerializer;
import appeng.recipes.game.FacadeRecipe;
import appeng.recipes.game.RemoveItemUpgradeRecipe;
import appeng.recipes.game.RemoveItemUpgradeRecipeSerializer;
import appeng.recipes.game.StorageCellDisassemblyRecipe;
import appeng.recipes.game.StorageCellDisassemblyRecipeSerializer;
import appeng.recipes.game.StorageCellUpgradeRecipe;
import appeng.recipes.game.StorageCellUpgradeRecipeSerializer;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.ChargerRecipeSerializer;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.handlers.InscriberRecipeSerializer;
import appeng.recipes.mattercannon.MatterCannonAmmo;
import appeng.recipes.mattercannon.MatterCannonAmmoSerializer;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;
import appeng.recipes.quartzcutting.QuartzCuttingRecipeSerializer;
import appeng.recipes.transform.TransformRecipe;
import appeng.recipes.transform.TransformRecipeSerializer;

public final class AE2RecipeSerializers {
    private AE2RecipeSerializers() {
    }

    public static final RegistryObject<RecipeSerializer<InscriberRecipe>> INSCRIBER =
            AE2Registries.RECIPE_SERIALIZERS.register("inscriber", InscriberRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<FacadeRecipe>> FACADE = AE2Registries.RECIPE_SERIALIZERS
            .register("facade",
                    () -> new SimpleCraftingRecipeSerializer<>(
                            category -> new FacadeRecipe(category, AEItems.FACADE.get())));

    public static final RegistryObject<RecipeSerializer<EntropyRecipe>> ENTROPY =
            AE2Registries.RECIPE_SERIALIZERS.register("entropy", EntropyRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<MatterCannonAmmo>> MATTER_CANNON =
            AE2Registries.RECIPE_SERIALIZERS.register("matter_cannon", MatterCannonAmmoSerializer::new);

    public static final RegistryObject<RecipeSerializer<TransformRecipe>> TRANSFORM =
            AE2Registries.RECIPE_SERIALIZERS.register("transform", TransformRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<ChargerRecipe>> CHARGER =
            AE2Registries.RECIPE_SERIALIZERS.register("charger", ChargerRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<StorageCellUpgradeRecipe>> STORAGE_CELL_UPGRADE =
            AE2Registries.RECIPE_SERIALIZERS.register("storage_cell_upgrade",
                    StorageCellUpgradeRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<AddItemUpgradeRecipe>> ADD_ITEM_UPGRADE =
            AE2Registries.RECIPE_SERIALIZERS.register("add_item_upgrade",
                    AddItemUpgradeRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<RemoveItemUpgradeRecipe>> REMOVE_ITEM_UPGRADE =
            AE2Registries.RECIPE_SERIALIZERS.register("remove_item_upgrade",
                    RemoveItemUpgradeRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<QuartzCuttingRecipe>> QUARTZ_CUTTING =
            AE2Registries.RECIPE_SERIALIZERS.register("quartz_cutting", QuartzCuttingRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<CraftingUnitTransformRecipe>> CRAFTING_UNIT_TRANSFORM =
            AE2Registries.RECIPE_SERIALIZERS.register("crafting_unit_transform",
                    CraftingUnitTransformRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<StorageCellDisassemblyRecipe>> STORAGE_CELL_DISASSEMBLY =
            AE2Registries.RECIPE_SERIALIZERS.register("storage_cell_disassembly",
                    StorageCellDisassemblyRecipeSerializer::new);

    public static void init() {
        // Ensure class loading
    }
}
