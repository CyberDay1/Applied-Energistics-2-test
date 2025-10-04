package appeng.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record InscriberRecipe(ItemStack top, ItemStack middle, ItemStack bottom, ItemStack result)
        implements Recipe<CraftingContainer> {

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return true; // TODO: implement slot matching logic
    }

    @Override
    public ItemStack assemble(CraftingContainer container, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public ItemStack getResultItem() {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AE2RecipeSerializers.INSCRIBER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return AE2RecipeTypes.INSCRIBER.get();
    }

    public static class Serializer implements RecipeSerializer<InscriberRecipe> {
        public static final Codec<InscriberRecipe> CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemStack.CODEC.fieldOf("top").forGetter(InscriberRecipe::top),
                ItemStack.CODEC.fieldOf("middle").forGetter(InscriberRecipe::middle),
                ItemStack.CODEC.fieldOf("bottom").forGetter(InscriberRecipe::bottom),
                ItemStack.CODEC.fieldOf("result").forGetter(InscriberRecipe::result)).apply(i, InscriberRecipe::new));

        @Override
        public Codec<InscriberRecipe> codec() {
            return CODEC;
        }
    }
}
