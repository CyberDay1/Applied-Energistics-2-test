package appeng.recipes.game;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import appeng.registry.AE2RecipeSerializers;
import appeng.registry.AE2RecipeTypes;

/**
 * Used to handle disassembly of the (Portable) Storage Cells.
 */
public class StorageCellDisassemblyRecipe extends CustomRecipe {
    public static final RecipeType<StorageCellDisassemblyRecipe> TYPE = new RecipeType<>() {
    };
    public static final MapCodec<StorageCellDisassemblyRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
            .group(
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("cell")
                            .forGetter(StorageCellDisassemblyRecipe::getStorageCell),
                    ItemStack.CODEC.listOf().fieldOf("cell_disassembly_items")
                            .forGetter(StorageCellDisassemblyRecipe::getCellDisassemblyItems))
            .apply(builder, StorageCellDisassemblyRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageCellDisassemblyRecipe> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()),
                    StorageCellDisassemblyRecipe::getStorageCell,
                    ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    StorageCellDisassemblyRecipe::getCellDisassemblyItems,
                    StorageCellDisassemblyRecipe::new);

    private final List<ItemStack> disassemblyItems;
    private final Item storageCell;

    public StorageCellDisassemblyRecipe(Item storageCell, List<ItemStack> disassemblyItems) {
        super(CraftingBookCategory.MISC);
        this.storageCell = storageCell;
        this.disassemblyItems = disassemblyItems;
    }

    public Item getStorageCell() {
        return this.storageCell;
    }

    public List<ItemStack> getCellDisassemblyItems() {
        return disassemblyItems.stream().map(ItemStack::copy).toList();
    }

    /**
     * @return True when any Disassembly Output is specified.
     */
    public boolean canDisassemble() {
        return !this.disassemblyItems.isEmpty();
    }

    /**
     * Used to get the disassembly result based on recipes for the given cell.
     *
     * @param cell The cell item being disassembled.
     * @return An empty list to indicate the cell cannot be disassembled. Note that stacks in the list must be copied by
     *         the caller.
     */
    public static List<ItemStack> getDisassemblyResult(Level level, Item cell) {
        var recipeManager = level.getRecipeManager();

        for (var holder : recipeManager.byType(AE2RecipeTypes.CELL_DISASSEMBLY.get())) {
            if (holder.value().storageCell == cell) {
                return holder.value().getCellDisassemblyItems();
            }
        }

        return List.of();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AE2RecipeSerializers.STORAGE_CELL_DISASSEMBLY.get();
    }

    @Override
    public RecipeType<?> getType() {
        return AE2RecipeTypes.CELL_DISASSEMBLY.get();
    }
}
