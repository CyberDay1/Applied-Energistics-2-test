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

public class StorageCellDisassemblyRecipeSerializer implements RecipeSerializer<StorageCellDisassemblyRecipe> {

    @Override
    public MapCodec<StorageCellDisassemblyRecipe> codec() {
        return StorageCellDisassemblyRecipe.CODEC;
    }

//? if (<=1.21.4) {
    // TODO(stonecutter): Confirm storage cell disassembly FriendlyByteBuf codec before removing legacy branch.
    @Override
    public StorageCellDisassemblyRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return StorageCellDisassemblyRecipe.STREAM_CODEC.decode(buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, StorageCellDisassemblyRecipe recipe) {
        StorageCellDisassemblyRecipe.STREAM_CODEC.encode(buffer, recipe);
    }
//? else {
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, StorageCellDisassemblyRecipe> streamCodec() {
        return StorageCellDisassemblyRecipe.STREAM_CODEC;
    }
//? endif
}
