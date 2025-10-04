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
import appeng.recipes.handlers.ChargerRecipe;

final class ChargerCategory implements IRecipeCategory<ChargerRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    ChargerCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(AE2JeiPlugin.TEXTURE, 0, 60, 150, 60);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, AEBlocks.CHARGER.stack());
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<ChargerRecipe> getRecipeType() {
        return AE2JeiPlugin.CHARGER;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gui.ae2.jei.charger");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ChargerRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 24)
                .addIngredients(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 98, 24)
                .addItemStack(recipe.getResultItem());
    }
}
