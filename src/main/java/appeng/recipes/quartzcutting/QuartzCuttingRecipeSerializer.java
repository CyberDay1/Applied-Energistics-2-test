package appeng.recipes.quartzcutting;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class QuartzCuttingRecipeSerializer implements RecipeSerializer<QuartzCuttingRecipe> {

    public MapCodec<QuartzCuttingRecipe> codec() {
        return QuartzCuttingRecipe.CODEC;
    }

    public StreamCodec<RegistryFriendlyByteBuf, QuartzCuttingRecipe> streamCodec() {
        return QuartzCuttingRecipe.STREAM_CODEC;
    }
}
