package appeng.api.storage;

import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Lightweight representation of a stored fluid stack for listings.
 */
public record FluidStackView(Fluid fluid, int amount) {
    public FluidStack asStack() {
        return new FluidStack(fluid, amount);
    }
}
