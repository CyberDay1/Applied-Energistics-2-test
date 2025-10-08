package appeng.recipes.game;

import com.mojang.serialization.MapCodec;

//? if (<=1.21.4) {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
//? else {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
//? endif
import net.minecraft.world.item.crafting.RecipeSerializer;

public class StorageCellUpgradeRecipeSerializer implements RecipeSerializer<StorageCellUpgradeRecipe> {

    @Override
    public MapCodec<StorageCellUpgradeRecipe> codec() {
        return StorageCellUpgradeRecipe.CODEC;
    }

//? if (<=1.21.4) {
    // TODO(stonecutter): Confirm storage cell upgrade FriendlyByteBuf codec matches >=1.21.5 runtime.
    @Override
    public StorageCellUpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return StorageCellUpgradeRecipe.STREAM_CODEC.decode(buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, StorageCellUpgradeRecipe recipe) {
        StorageCellUpgradeRecipe.STREAM_CODEC.encode(buffer, recipe);
    }
//? else {
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, StorageCellUpgradeRecipe> streamCodec() {
        return StorageCellUpgradeRecipe.STREAM_CODEC;
    }
//? endif
}
