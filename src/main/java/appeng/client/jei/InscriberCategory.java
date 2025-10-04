package appeng.client.jei;

import net.minecraft.network.chat.Component;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.core.definitions.AEBlocks;
import appeng.recipes.handlers.InscriberRecipe;

final class InscriberCategory implements IRecipeCategory<InscriberRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    InscriberCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(AE2JeiPlugin.TEXTURE, 0, 0, 150, 60);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, AEBlocks.INSCRIBER.stack());
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<InscriberRecipe> getRecipeType() {
        return AE2JeiPlugin.INSCRIBER;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.ae2.jei.inscriber");
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
    public void setRecipe(IRecipeLayoutBuilder builder, InscriberRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 6)
                .addIngredients(recipe.getTopOptional());
        builder.addSlot(RecipeIngredientRole.INPUT, 62, 24)
                .addIngredients(recipe.getMiddleInput());
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 42)
                .addIngredients(recipe.getBottomOptional());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 110, 24)
                .addItemStack(recipe.getResultItem());
    }
}
