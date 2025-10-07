package appeng.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.Items;

import appeng.AE2Registries;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.registry.AE2Items;

public class AE2RecipeProvider extends RecipeProvider {
    public AE2RecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        var recipeId = new ResourceLocation(AE2Registries.MODID, "crafting/sky_stone_brick_placeholder");
        output.accept(recipeId, new SkyStoneBrickRecipe(recipeId), null);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AE2Items.BASIC_CELL_4K.get())
                .pattern("PPP")
                .pattern("PCP")
                .pattern("PPP")
                .define('P', AE2Items.LOGIC_PROCESSOR.get())
                .define('C', AE2Items.BASIC_CELL_1K.get())
                .unlockedBy("has_basic_cell_1k", has(AE2Items.BASIC_CELL_1K.get()))
                .save(output, new ResourceLocation(AE2Registries.MODID, "crafting/basic_cell_4k"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AE2Items.BASIC_CELL_16K.get())
                .pattern("PPP")
                .pattern("PCP")
                .pattern("PPP")
                .define('P', AE2Items.CALCULATION_PROCESSOR.get())
                .define('C', AE2Items.BASIC_CELL_4K.get())
                .unlockedBy("has_basic_cell_4k", has(AE2Items.BASIC_CELL_4K.get()))
                .save(output, new ResourceLocation(AE2Registries.MODID, "crafting/basic_cell_16k"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AE2Items.BASIC_CELL_64K.get())
                .pattern("PPP")
                .pattern("PCP")
                .pattern("PPP")
                .define('P', AE2Items.ENGINEERING_PROCESSOR.get())
                .define('C', AE2Items.BASIC_CELL_16K.get())
                .unlockedBy("has_basic_cell_16k", has(AE2Items.BASIC_CELL_16K.get()))
                .save(output, new ResourceLocation(AE2Registries.MODID, "crafting/basic_cell_64k"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AE2Items.REDSTONE_CARD.get())
                .pattern("RRR")
                .pattern("RPR")
                .pattern("RRR")
                .define('R', Items.REDSTONE)
                .define('P', AE2Items.LOGIC_PROCESSOR.get())
                .unlockedBy("has_logic_processor", has(AE2Items.LOGIC_PROCESSOR.get()))
                .save(output, new ResourceLocation(AE2Registries.MODID, "crafting/redstone_card"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AE2Items.CAPACITY_CARD.get())
                .pattern("QQQ")
                .pattern("QPQ")
                .pattern("QQQ")
                .define('Q', Items.QUARTZ)
                .define('P', AE2Items.CALCULATION_PROCESSOR.get())
                .unlockedBy("has_calculation_processor", has(AE2Items.CALCULATION_PROCESSOR.get()))
                .save(output, new ResourceLocation(AE2Registries.MODID, "crafting/capacity_card"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AE2Items.SPEED_CARD.get())
                .pattern("GGG")
                .pattern("GPG")
                .pattern("GGG")
                .define('G', Items.GLOWSTONE_DUST)
                .define('P', AE2Items.ENGINEERING_PROCESSOR.get())
                .unlockedBy("has_engineering_processor", has(AE2Items.ENGINEERING_PROCESSOR.get()))
                .save(output, new ResourceLocation(AE2Registries.MODID, "crafting/speed_card"));

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, AE2Items.CRAFTING_MONITOR.get())
                .pattern("QIQ")
                .pattern("PGP")
                .pattern("QIQ")
                .define('Q', Items.QUARTZ)
                .define('I', Items.IRON_INGOT)
                .define('P', AE2Items.CALCULATION_PROCESSOR.get())
                .define('G', Items.GLASS)
                .unlockedBy("has_calculation_processor", has(AE2Items.CALCULATION_PROCESSOR.get()))
                .save(output, new ResourceLocation(AE2Registries.MODID, "crafting/crafting_monitor"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AE2Items.FUZZY_CARD.get())
                .pattern("SWS")
                .pattern("WPW")
                .pattern("SWS")
                .define('S', Items.STRING)
                .define('W', Items.WHITE_WOOL)
                .define('P', AE2Items.CALCULATION_PROCESSOR.get())
                .unlockedBy("has_calculation_processor", has(AE2Items.CALCULATION_PROCESSOR.get()))
                .save(output, new ResourceLocation(AE2Registries.MODID, "crafting/fuzzy_card"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AE2Items.ANNIHILATION_PLANE.get())
                .pattern("CGC")
                .pattern("CAC")
                .pattern("CGC")
                .define('C', AE2Items.CABLE.get())
                .define('G', AEBlocks.QUARTZ_GLASS)
                .define('A', AEItems.ANNIHILATION_CORE)
                .unlockedBy("has_annihilation_core", has(AEItems.ANNIHILATION_CORE))
                .save(output, new ResourceLocation(AE2Registries.MODID, "crafting/annihilation_plane"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AE2Items.FORMATION_PLANE.get())
                .pattern("CGC")
                .pattern("ILI")
                .pattern("CGC")
                .define('C', AE2Items.CABLE.get())
                .define('G', Items.GLASS)
                .define('I', Items.IRON_INGOT)
                .define('L', AE2Items.LOGIC_PROCESSOR.get())
                .unlockedBy("has_logic_processor", has(AE2Items.LOGIC_PROCESSOR.get()))
                .save(output, new ResourceLocation(AE2Registries.MODID, "crafting/formation_plane"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AE2Items.PARTITIONED_CELL.get())
                .requires(AE2Items.ENGINEERING_PROCESSOR.get())
                .requires(AE2Items.BASIC_CELL_1K.get())
                .unlockedBy("has_basic_cell_1k", has(AE2Items.BASIC_CELL_1K.get()))
                .save(output, new ResourceLocation(AE2Registries.MODID, "crafting/partitioned_cell"));
    }

    private static final class SkyStoneBrickRecipe implements FinishedRecipe {
        private final ResourceLocation id;

        private SkyStoneBrickRecipe(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("group", "sky_stone_brick");

            JsonArray ingredients = new JsonArray();
            JsonObject ingredient = new JsonObject();
            ingredient.addProperty("item", AE2Registries.MODID + ":sky_stone");
            ingredients.add(ingredient);
            json.add("ingredients", ingredients);

            JsonObject result = new JsonObject();
            result.addProperty("item", AE2Registries.MODID + ":sky_stone_brick");
            json.add("result", result);
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPELESS_RECIPE;
        }

        @Override
        public ResourceLocation id() {
            return id;
        }

        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Override
        public ResourceLocation advancementId() {
            return null;
        }
    }
}
