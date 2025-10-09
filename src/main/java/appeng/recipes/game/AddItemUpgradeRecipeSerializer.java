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

public class AddItemUpgradeRecipeSerializer implements RecipeSerializer<AddItemUpgradeRecipe> {

    @Override
    public MapCodec<AddItemUpgradeRecipe> codec() {
        return AddItemUpgradeRecipe.CODEC;
    }

//? if (<=1.21.4) {
    // TODO(stonecutter): Verify FriendlyByteBuf serializer once <= 1.21.4 recipes are runnable.
    @Override
    public AddItemUpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return AddItemUpgradeRecipe.STREAM_CODEC.decode(buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AddItemUpgradeRecipe recipe) {
        AddItemUpgradeRecipe.STREAM_CODEC.encode(buffer, recipe);
    }
//? } else {
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AddItemUpgradeRecipe> streamCodec() {
        return AddItemUpgradeRecipe.STREAM_CODEC;
    }
//? }
}
