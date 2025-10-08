package appeng.recipes.handlers;

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

public class ChargerRecipeSerializer implements RecipeSerializer<ChargerRecipe> {
    @Override
    public MapCodec<ChargerRecipe> codec() {
        return ChargerRecipe.CODEC;
    }

//? <=1.21.4 {
    // TODO(stonecutter): Remove legacy serialization once versions <= 1.21.4 are phased out.
    @Override
    public ChargerRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return ChargerRecipe.STREAM_CODEC.decode(buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ChargerRecipe recipe) {
        ChargerRecipe.STREAM_CODEC.encode(buffer, recipe);
    }
//?} else {
    /*@Override
    public StreamCodec<RegistryFriendlyByteBuf, ChargerRecipe> streamCodec() {
        return ChargerRecipe.STREAM_CODEC;
    }
*///?}
}
