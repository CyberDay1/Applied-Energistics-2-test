package appeng.client.jei;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.core.definitions.AEItems;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;

final class QuartzCuttingCategory implements IRecipeCategory<QuartzCuttingRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    QuartzCuttingCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(AE2JeiPlugin.TEXTURE, 0, 120, 150, 60);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, AEItems.CERTUS_QUARTZ_DUST.stack());
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<QuartzCuttingRecipe> getRecipeType() {
        return AE2JeiPlugin.QUARTZ_CUTTING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.ae2.jei.quartz_cutting");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, QuartzCuttingRecipe recipe, IFocusGroup focuses) {
        var ingredients = recipe.getIngredients();
        int baseX = 16;
        int baseY = 6;
        for (int i = 0; i < ingredients.size(); i++) {
            int x = baseX + (i % 3) * 18;
            int y = baseY + (i / 3) * 18;
            builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                    .addIngredients(ingredients.get(i));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 120, 24)
                .addItemStack(recipe.getResultItem(registryAccess()));
    }

    private static net.minecraft.core.HolderLookup.Provider registryAccess() {
        var minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level != null) {
            return level.registryAccess();
        }
        var connection = minecraft.getConnection();
        return connection != null ? connection.registryAccess() : null;
    }
}
