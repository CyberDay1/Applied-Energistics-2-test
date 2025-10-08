package appeng.recipes.quartzcutting;

import com.mojang.serialization.MapCodec;

//? <=1.21.4 {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
//?}
//? >=1.21.5 {
/*import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
*///?}
import net.minecraft.world.item.crafting.RecipeSerializer;

public class QuartzCuttingRecipeSerializer implements RecipeSerializer<QuartzCuttingRecipe> {

    public MapCodec<QuartzCuttingRecipe> codec() {
        return QuartzCuttingRecipe.CODEC;
    }

//? <=1.21.4 {
    // TODO(stonecutter): Replace FriendlyByteBuf branch after <= 1.21.4 validation.
    @Override
    public QuartzCuttingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return QuartzCuttingRecipe.STREAM_CODEC.decode(buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, QuartzCuttingRecipe recipe) {
        QuartzCuttingRecipe.STREAM_CODEC.encode(buffer, recipe);
    }
//?} else {
    /*@Override
    public StreamCodec<RegistryFriendlyByteBuf, QuartzCuttingRecipe> streamCodec() {
        return QuartzCuttingRecipe.STREAM_CODEC;
    }
*///?}
}
