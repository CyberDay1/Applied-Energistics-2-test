package appeng.items.patterns;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EncodedPatternItem extends Item {
    private static final String RECIPE_ID_TAG = "RecipeId";

    public EncodedPatternItem(Properties properties) {
        super(properties);
    }

    public void setRecipeId(ItemStack stack, ResourceLocation recipeId) {
        if (!stack.is(this)) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(RECIPE_ID_TAG, recipeId.toString());
    }

    public Optional<ResourceLocation> getRecipeId(ItemStack stack) {
        if (!stack.is(this)) {
            return Optional.empty();
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(RECIPE_ID_TAG)) {
            return Optional.empty();
        }
        String recipeId = tag.getString(RECIPE_ID_TAG);
        return Optional.ofNullable(ResourceLocation.tryParse(recipeId));
    }

    public void clearRecipeId(ItemStack stack) {
        if (!stack.is(this)) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(RECIPE_ID_TAG);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
    }
}
