package appeng.recipes.game;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
//? <=1.21.4 {
import net.minecraft.network.FriendlyByteBuf;
//?}
//? >=1.21.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
//?}
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
import net.minecraft.world.level.block.Block;

import appeng.registry.AE2RecipeSerializers;
import appeng.registry.AE2RecipeTypes;

/**
 * Used to handle upgrading and removal of upgrades for crafting units (in-world).
 */
public class CraftingUnitTransformRecipe extends CustomRecipe {
//? <=1.21.1 {
    @Deprecated(forRemoval = true, since = "1.21.1")
    public static final RecipeType<CraftingUnitTransformRecipe> TYPE = new RecipeType<>() {
    };
//?}
    public static final MapCodec<CraftingUnitTransformRecipe> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
            .group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("upgraded_block")
                            .forGetter(CraftingUnitTransformRecipe::getUpgradedBlock),
                    BuiltInRegistries.ITEM.byNameCodec().fieldOf("upgrade_item")
                            .forGetter(CraftingUnitTransformRecipe::getUpgradeItem))
            .apply(builder, CraftingUnitTransformRecipe::new));

//? <=1.21.4 {
    // TODO(stonecutter): Confirm registry codec fallback with FriendlyByteBuf for <= 1.21.4.
    public static final StreamCodec<FriendlyByteBuf, CraftingUnitTransformRecipe> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.registry(BuiltInRegistries.BLOCK.key()),
                    CraftingUnitTransformRecipe::getUpgradedBlock,
                    ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()),
                    CraftingUnitTransformRecipe::getUpgradeItem,
                    CraftingUnitTransformRecipe::new);
//?} else {
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingUnitTransformRecipe> STREAM_CODEC = StreamCodec
            .composite(
                    ByteBufCodecs.registry(BuiltInRegistries.BLOCK.key()),
                    CraftingUnitTransformRecipe::getUpgradedBlock,
                    ByteBufCodecs.registry(BuiltInRegistries.ITEM.key()),
                    CraftingUnitTransformRecipe::getUpgradeItem,
                    CraftingUnitTransformRecipe::new);
//?}

    private final Block upgradedBlock;
    private final Item upgradeItem;

    public CraftingUnitTransformRecipe(Block upgradedBlock, Item upgradeItem) {
        super(CraftingBookCategory.MISC);
        this.upgradedBlock = upgradedBlock;
        this.upgradeItem = upgradeItem;
    }

    public Block getUpgradedBlock() {
        return this.upgradedBlock;
    }

    public Item getUpgradeItem() {
        return upgradeItem;
    }

    /**
     * Gets the upgrade that would be returned from removing an upgrade from the given crafting block.
     *
     * @return Empty stack if no upgrade removal is possible.
     */
    public static ItemStack getRemovedUpgrade(Level level, Block upgradedBlock) {
        var recipeManager = level.getRecipeManager();

        for (var holder : recipeManager.byType(AE2RecipeTypes.CRAFTING_UNIT_TRANSFORM.get())) {
            if (holder.value().upgradedBlock == upgradedBlock) {
                return holder.value().upgradeItem.getDefaultInstance();
            }
        }

        return ItemStack.EMPTY;
    }

    /**
     * Search for the resulting upgraded block when upgrading a crafting unit with the given upgrade item.
     */
    public static Block getUpgradedBlock(Level level, ItemStack upgradeItem) {
        for (var holder : level.getRecipeManager().byType(AE2RecipeTypes.CRAFTING_UNIT_TRANSFORM.get())) {
            if (upgradeItem.is(holder.value().getUpgradeItem())) {
                return holder.value().upgradedBlock;
            }
        }
        return null;
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
        return AE2RecipeSerializers.CRAFTING_UNIT_TRANSFORM.get();
    }

    @Override
    public RecipeType<?> getType() {
        return AE2RecipeTypes.CRAFTING_UNIT_TRANSFORM.get();
    }
}
