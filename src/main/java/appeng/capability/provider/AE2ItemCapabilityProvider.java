package appeng.capability.provider;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

/**
 * Base implementation for capability providers that target AE2 item stacks.
 */
public abstract class AE2ItemCapabilityProvider implements ICapabilityProvider<ItemStack, Void> {
    @Override
    public <T> T getCapability(ItemStack stack, Capability<T> cap, Void ctx) {
        return null;
    }
}
