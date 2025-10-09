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

public class RemoveItemUpgradeRecipeSerializer implements RecipeSerializer<RemoveItemUpgradeRecipe> {

    @Override
    public MapCodec<RemoveItemUpgradeRecipe> codec() {
        return RemoveItemUpgradeRecipe.CODEC;
    }

//? if (<=1.21.4) {
    // TODO(stonecutter): Verify remove-upgrade FriendlyByteBuf stream once regression harness is ready.
    @Override
    public RemoveItemUpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return RemoveItemUpgradeRecipe.STREAM_CODEC.decode(buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, RemoveItemUpgradeRecipe recipe) {
        RemoveItemUpgradeRecipe.STREAM_CODEC.encode(buffer, recipe);
    }
//? } else {
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RemoveItemUpgradeRecipe> streamCodec() {
        return RemoveItemUpgradeRecipe.STREAM_CODEC;
    }
//? }
}
