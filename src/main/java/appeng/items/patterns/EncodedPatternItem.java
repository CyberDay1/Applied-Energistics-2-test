package appeng.items.patterns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack.TooltipContext;
import net.minecraft.resources.ResourceLocation;

public class EncodedPatternItem extends Item {
    private static final String RECIPE_ID_TAG = "RecipeId";
    private static final String INPUTS_TAG = "Inputs";
    private static final String OUTPUTS_TAG = "Outputs";
    private static final String ITEM_TAG = "item";
    private static final String COUNT_TAG = "count";

    public EncodedPatternItem(Properties properties) {
        super(properties);
    }

    public void setRecipe(ItemStack stack, ResourceLocation recipeId, List<ItemStack> inputs, List<ItemStack> outputs) {
        if (!stack.is(this)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (recipeId != null) {
            tag.putString(RECIPE_ID_TAG, recipeId.toString());
        }

        tag.put(INPUTS_TAG, writeStacks(inputs));
        tag.put(OUTPUTS_TAG, writeStacks(outputs));
    }

    public Optional<ResourceLocation> getRecipeId(ItemStack stack) {
        if (!stack.is(this)) {
            return Optional.empty();
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(RECIPE_ID_TAG, Tag.TAG_STRING)) {
            return Optional.empty();
        }
        String recipeId = tag.getString(RECIPE_ID_TAG);
        return Optional.ofNullable(ResourceLocation.tryParse(recipeId));
    }

    public List<ItemStack> getInputs(ItemStack stack) {
        return Collections.unmodifiableList(readStacks(stack, INPUTS_TAG));
    }

    public List<ItemStack> getOutputs(ItemStack stack) {
        return Collections.unmodifiableList(readStacks(stack, OUTPUTS_TAG));
    }

    private static ListTag writeStacks(List<ItemStack> stacks) {
        ListTag list = new ListTag();
        if (stacks == null) {
            return list;
        }

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }

            CompoundTag entry = new CompoundTag();
            entry.putString(ITEM_TAG, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            entry.putInt(COUNT_TAG, stack.getCount());
            list.add(entry);
        }
        return list;
    }

    private static List<ItemStack> readStacks(ItemStack stack, String key) {
        if (!(stack.getItem() instanceof EncodedPatternItem)) {
            return List.of();
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(key, Tag.TAG_LIST)) {
            return List.of();
        }

        ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
        List<ItemStack> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.contains(ITEM_TAG, Tag.TAG_STRING)) {
                continue;
            }

            var itemId = ResourceLocation.tryParse(entry.getString(ITEM_TAG));
            if (itemId == null) {
                continue;
            }

            var item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
            if (item == null) {
                continue;
            }

            int count = entry.contains(COUNT_TAG, Tag.TAG_INT) ? entry.getInt(COUNT_TAG) : 1;
            count = Math.max(1, count);

            ItemStack value = new ItemStack(item);
            value.setCount(count);
            result.add(value);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack,
            TooltipContext context,
            List<Component> lines,
            TooltipFlag flags) {
        super.appendHoverText(stack, context, lines, flags);

        var outputs = getOutputs(stack);
        if (!outputs.isEmpty()) {
            lines.add(Component.translatable("tooltip.appliedenergistics2.encoded_pattern.output",
                    formatStackList(outputs)));
        }

        var inputs = getInputs(stack);
        if (!inputs.isEmpty()) {
            lines.add(Component.translatable("tooltip.appliedenergistics2.encoded_pattern.inputs",
                    formatStackList(inputs)));
        }
    }

    private static Component formatStackList(List<ItemStack> stacks) {
        MutableComponent result = Component.empty();
        boolean first = true;
        for (ItemStack stack : stacks) {
            if (!first) {
                result.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }
            result.append(formatStack(stack));
            first = false;
        }
        return result;
    }

    private static MutableComponent formatStack(ItemStack stack) {
        MutableComponent name = Component.translatable(stack.getDescriptionId());
        int count = stack.getCount();
        if (count <= 1) {
            return name;
        }

        return Component.empty()
                .append(Component.literal(Integer.toString(count)).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" x ").withStyle(ChatFormatting.GRAY))
                .append(name);
    }
}
