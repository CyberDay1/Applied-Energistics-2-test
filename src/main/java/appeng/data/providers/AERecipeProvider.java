package appeng.data.providers;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

public final class AERecipeProvider extends RecipeProvider {
    public AERecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(RecipeOutput out) {
        // ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEItems.CERTUS_QUARTZ.get())
        //     .define('#', Items.QUARTZ)
        //     .pattern(" # ")
        //     .pattern("###")
        //     .pattern(" # ")
        //     .unlockedBy("has_quartz", has(Items.QUARTZ))
        //     .save(out);
    }
}
