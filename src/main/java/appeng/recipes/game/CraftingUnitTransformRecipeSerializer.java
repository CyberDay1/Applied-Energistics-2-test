package appeng.recipes.game;

import com.mojang.serialization.MapCodec;

//? if (<=1.21.4) {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
//? } else {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
//? }
import net.minecraft.world.item.crafting.RecipeSerializer;

public class CraftingUnitTransformRecipeSerializer implements RecipeSerializer<CraftingUnitTransformRecipe> {

    @Override
    public MapCodec<CraftingUnitTransformRecipe> codec() {
        return CraftingUnitTransformRecipe.CODEC;
    }

//? if (<=1.21.4) {
    // TODO(stonecutter): Confirm FriendlyByteBuf codec parity with >=1.21.5 implementation.
    @Override
    public CraftingUnitTransformRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return CraftingUnitTransformRecipe.STREAM_CODEC.decode(buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, CraftingUnitTransformRecipe recipe) {
        CraftingUnitTransformRecipe.STREAM_CODEC.encode(buffer, recipe);
    }
//? } else {
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CraftingUnitTransformRecipe> streamCodec() {
        return CraftingUnitTransformRecipe.STREAM_CODEC;
    }
//? }
}
