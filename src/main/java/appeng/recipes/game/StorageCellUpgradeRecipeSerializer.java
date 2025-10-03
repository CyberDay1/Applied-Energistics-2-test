package appeng.recipes.game;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class StorageCellUpgradeRecipeSerializer implements RecipeSerializer<StorageCellUpgradeRecipe> {

    @Override
    public MapCodec<StorageCellUpgradeRecipe> codec() {
        return StorageCellUpgradeRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, StorageCellUpgradeRecipe> streamCodec() {
        return StorageCellUpgradeRecipe.STREAM_CODEC;
    }
}
