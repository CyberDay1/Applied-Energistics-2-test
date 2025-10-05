package appeng.blockentity.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.registry.AE2BlockEntities;
import appeng.registry.AE2Items;
import appeng.items.patterns.EncodedPatternItem;

public class PatternEncodingTerminalBlockEntity extends AEBaseBlockEntity {
    private static final int CRAFTING_GRID_SIZE = 9;
    private static final int PATTERN_SLOT_COUNT = 2;
    private static final int BLANK_PATTERN_SLOT = 0;
    private static final int ENCODED_PATTERN_SLOT = 1;

    private final NonNullList<ItemStack> craftingItems =
            NonNullList.withSize(CRAFTING_GRID_SIZE, ItemStack.EMPTY);
    private final NonNullList<ItemStack> patternItems =
            NonNullList.withSize(PATTERN_SLOT_COUNT, ItemStack.EMPTY);

    private final Container craftingMatrix = new Container() {
        @Override
        public int getContainerSize() {
            return CRAFTING_GRID_SIZE;
        }

        @Override
        public boolean isEmpty() {
            for (var stack : craftingItems) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return craftingItems.get(index);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            var removed = ContainerHelper.removeItem(craftingItems, index, count);
            if (!removed.isEmpty()) {
                onCraftingMatrixChanged();
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            var stack = craftingItems.get(index);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            craftingItems.set(index, ItemStack.EMPTY);
            onCraftingMatrixChanged();
            return stack;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            craftingItems.set(index, stack);
            if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
            onCraftingMatrixChanged();
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < CRAFTING_GRID_SIZE; i++) {
                craftingItems.set(i, ItemStack.EMPTY);
            }
            onCraftingMatrixChanged();
        }

        @Override
        public void startOpen(Player player) {
        }

        @Override
        public void stopOpen(Player player) {
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return true;
        }
    };

    private final Container patternInventory = new Container() {
        @Override
        public int getContainerSize() {
            return PATTERN_SLOT_COUNT;
        }

        @Override
        public boolean isEmpty() {
            for (var stack : patternItems) {
                if (!stack.isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int index) {
            return patternItems.get(index);
        }

        @Override
        public ItemStack removeItem(int index, int count) {
            var removed = ContainerHelper.removeItem(patternItems, index, count);
            if (!removed.isEmpty()) {
                setChanged();
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int index) {
            var stack = patternItems.get(index);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            patternItems.set(index, ItemStack.EMPTY);
            setChanged();
            return stack;
        }

        @Override
        public void setItem(int index, ItemStack stack) {
            if (index == ENCODED_PATTERN_SLOT && stack.getCount() > 1) {
                stack.setCount(1);
            }
            if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
            patternItems.set(index, stack);
            setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < PATTERN_SLOT_COUNT; i++) {
                patternItems.set(i, ItemStack.EMPTY);
            }
            setChanged();
        }

        @Override
        public void startOpen(Player player) {
        }

        @Override
        public void stopOpen(Player player) {
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            if (index == BLANK_PATTERN_SLOT) {
                return stack.is(AE2Items.BLANK_PATTERN.get());
            }
            return stack.is(AE2Items.ENCODED_PATTERN.get());
        }
    };

    @Nullable
    private RecipeHolder<CraftingRecipe> lastRecipe;

    public PatternEncodingTerminalBlockEntity(BlockPos pos, BlockState state) {
        super(AE2BlockEntities.PATTERN_ENCODING_TERMINAL.get(), pos, state);
    }

    public Container getCraftingMatrix() {
        return craftingMatrix;
    }

    public Container getPatternInventory() {
        return patternInventory;
    }

    public ItemStack getBlankPatternStack() {
        return patternItems.get(BLANK_PATTERN_SLOT);
    }

    public void setBlankPatternStack(ItemStack stack) {
        patternItems.set(BLANK_PATTERN_SLOT, stack);
        setChanged();
    }

    public ItemStack getEncodedPatternStack() {
        return patternItems.get(ENCODED_PATTERN_SLOT);
    }

    public void setEncodedPatternStack(ItemStack stack) {
        if (!stack.isEmpty() && stack.getCount() > 1) {
            stack.setCount(1);
        }
        patternItems.set(ENCODED_PATTERN_SLOT, stack);
        setChanged();
    }

    public ItemStack getCraftingResult() {
        var level = getLevel();
        if (level == null) {
            return ItemStack.EMPTY;
        }

        var input = createCraftingInput();
        if (input.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return findRecipe(level, input.get())
                .map(recipe -> recipe.value().assemble(input.get(), level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    public EncodeResult encode() {
        var level = getLevel();
        if (level == null || level.isClientSide()) {
            return EncodeResult.error(Component.translatable("message.appliedenergistics2.pattern_encoding.internal"));
        }

        if (!patternItems.get(ENCODED_PATTERN_SLOT).isEmpty()) {
            return EncodeResult.error(
                    Component.translatable("message.appliedenergistics2.pattern_encoding.output_occupied"));
        }

        var blank = patternItems.get(BLANK_PATTERN_SLOT);
        if (blank.isEmpty() || !blank.is(AE2Items.BLANK_PATTERN.get())) {
            return EncodeResult.error(
                    Component.translatable("message.appliedenergistics2.pattern_encoding.need_blank_pattern"));
        }

        var input = createCraftingInput();
        if (input.isEmpty()) {
            return EncodeResult
                    .error(Component.translatable("message.appliedenergistics2.pattern_encoding.no_ingredients"));
        }

        var recipe = findRecipe(level, input.get()).orElse(null);
        if (recipe == null) {
            return EncodeResult
                    .error(Component.translatable("message.appliedenergistics2.pattern_encoding.invalid_recipe"));
        }

        var result = recipe.value().assemble(input.get(), level.registryAccess()).copy();
        if (result.isEmpty()) {
            return EncodeResult
                    .error(Component.translatable("message.appliedenergistics2.pattern_encoding.invalid_recipe"));
        }

        var encodedStack = new ItemStack(AE2Items.ENCODED_PATTERN.get());
        if (!(encodedStack.getItem() instanceof EncodedPatternItem encodedPatternItem)) {
            return EncodeResult.error(
                    Component.translatable("message.appliedenergistics2.pattern_encoding.internal"));
        }

        var inputs = summarizeInputs();
        encodedPatternItem.setRecipe(encodedStack, recipe.id(), inputs, List.of(result));

        var remaining = blank.copy();
        remaining.shrink(1);
        setBlankPatternStack(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
        setEncodedPatternStack(encodedStack);

        return EncodeResult.success(
                Component.translatable("message.appliedenergistics2.pattern_encoding.success", result.getHoverName()));
    }

    private List<ItemStack> summarizeInputs() {
        var summarized = new ArrayList<ItemStack>();
        for (var stack : craftingItems) {
            if (stack.isEmpty()) {
                continue;
            }

            var unit = stack.copy();
            unit.setCount(1);

            boolean merged = false;
            for (var existing : summarized) {
                if (ItemStack.isSameItemSameComponents(existing, unit)) {
                    existing.grow(1);
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                summarized.add(unit);
            }
        }
        return summarized;
    }

    public record EncodeResult(boolean success, Component message) {
        public static EncodeResult success(Component message) {
            return new EncodeResult(true, message);
        }

        public static EncodeResult error(Component message) {
            return new EncodeResult(false, message);
        }
    }

    private Optional<CraftingInput> createCraftingInput() {
        boolean hasIngredient = false;
        var items = NonNullList.withSize(CRAFTING_GRID_SIZE, ItemStack.EMPTY);
        for (int i = 0; i < CRAFTING_GRID_SIZE; i++) {
            var stack = craftingItems.get(i);
            if (!stack.isEmpty()) {
                hasIngredient = true;
            }
            items.set(i, stack.copy());
        }

        if (!hasIngredient) {
            return Optional.empty();
        }

        return Optional.of(CraftingInput.of(3, 3, items));
    }

    private Optional<RecipeHolder<CraftingRecipe>> findRecipe(Level level, CraftingInput input) {
        if (lastRecipe != null && lastRecipe.value().matches(input, level)) {
            return Optional.of(lastRecipe);
        }

        var optional = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        lastRecipe = optional.orElse(null);
        return optional;
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        if (tag.contains("Crafting")) {
            ContainerHelper.loadAllItems(tag.getCompound("Crafting"), craftingItems);
        }
        if (tag.contains("Pattern")) {
            ContainerHelper.loadAllItems(tag.getCompound("Pattern"), patternItems);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        var craftingTag = new CompoundTag();
        ContainerHelper.saveAllItems(craftingTag, craftingItems);
        tag.put("Crafting", craftingTag);

        var patternTag = new CompoundTag();
        ContainerHelper.saveAllItems(patternTag, patternItems);
        tag.put("Pattern", patternTag);
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (var stack : craftingItems) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
        for (var stack : patternItems) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        for (int i = 0; i < craftingItems.size(); i++) {
            craftingItems.set(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < patternItems.size(); i++) {
            patternItems.set(i, ItemStack.EMPTY);
        }
        lastRecipe = null;
        setChanged();
    }

    private void onCraftingMatrixChanged() {
        lastRecipe = null;
        setChanged();
    }
}
