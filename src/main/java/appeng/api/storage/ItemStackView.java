package appeng.api.storage;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemStackView(Item item, int count) {
    public ItemStack asStack() {
        ItemStack stack = new ItemStack(item);
        stack.setCount(count);
        return stack;
    }
}
