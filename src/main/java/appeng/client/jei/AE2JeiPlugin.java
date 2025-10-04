package appeng.client.jei;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import appeng.api.compat.JeiCompat;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.quartzcutting.QuartzCuttingRecipe;
import appeng.registry.AE2RecipeTypes;

@JeiPlugin
public final class AE2JeiPlugin implements IModPlugin {
    static final ResourceLocation TEXTURE = AppEng.makeId("textures/guis/jei.png");

    public static final RecipeType<InscriberRecipe> INSCRIBER = new RecipeType<>(
            Objects.requireNonNull(AE2RecipeTypes.INSCRIBER.getId()), InscriberRecipe.class);
    public static final RecipeType<ChargerRecipe> CHARGER = new RecipeType<>(
            Objects.requireNonNull(AE2RecipeTypes.CHARGER.getId()), ChargerRecipe.class);
    public static final RecipeType<QuartzCuttingRecipe> QUARTZ_CUTTING = new RecipeType<>(
            Objects.requireNonNull(AE2RecipeTypes.QUARTZ_CUTTING.getId()), QuartzCuttingRecipe.class);

    private static final ResourceLocation ID = AppEng.makeId("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new InscriberCategory(guiHelper),
                new ChargerCategory(guiHelper),
                new QuartzCuttingCategory(guiHelper));
        JeiCompat.reportBridgeInitialized();
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(INSCRIBER, unwrap(findRecipes(AE2RecipeTypes.INSCRIBER.get())));
        registration.addRecipes(CHARGER, unwrap(findRecipes(AE2RecipeTypes.CHARGER.get())));
        registration.addRecipes(QUARTZ_CUTTING, unwrap(findRecipes(AE2RecipeTypes.QUARTZ_CUTTING.get())));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(AEBlocks.INSCRIBER.stack(), INSCRIBER);
        registration.addRecipeCatalyst(AEBlocks.CHARGER.stack(), CHARGER);
        registration.addRecipeCatalyst(AEItems.CERTUS_QUARTZ_DUST.stack(), QUARTZ_CUTTING);
    }

    private static <T extends Recipe<?>> List<T> unwrap(Collection<RecipeHolder<T>> recipes) {
        return recipes.stream().map(RecipeHolder::value).toList();
    }

    private static <T extends Recipe<?>> Collection<RecipeHolder<T>> findRecipes(net.minecraft.world.item.crafting.RecipeType<T> type) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        return level.getRecipeManager().getAllRecipesFor(type);
    }
}
