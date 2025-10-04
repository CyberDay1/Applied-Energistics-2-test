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

public record ChargerRecipe(ItemStack input, ItemStack result, int time)
        implements Recipe<CraftingContainer> {

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return true;
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
        return AE2RecipeSerializers.CHARGER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return AE2RecipeTypes.CHARGER.get();
    }

    public static class Serializer implements RecipeSerializer<ChargerRecipe> {
        public static final Codec<ChargerRecipe> CODEC = RecordCodecBuilder.create(i -> i.group(
                ItemStack.CODEC.fieldOf("input").forGetter(ChargerRecipe::input),
                ItemStack.CODEC.fieldOf("result").forGetter(ChargerRecipe::result),
                Codec.INT.fieldOf("time").forGetter(ChargerRecipe::time)).apply(i, ChargerRecipe::new));

        @Override
        public Codec<ChargerRecipe> codec() {
            return CODEC;
        }
    }
}
