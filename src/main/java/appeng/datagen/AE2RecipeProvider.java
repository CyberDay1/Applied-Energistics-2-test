package appeng.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import appeng.AE2Registries;

public class AE2RecipeProvider extends RecipeProvider {
    public AE2RecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        var recipeId = new ResourceLocation(AE2Registries.MODID, "crafting/sky_stone_brick_placeholder");
        output.accept(recipeId, new SkyStoneBrickRecipe(recipeId), null);
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
