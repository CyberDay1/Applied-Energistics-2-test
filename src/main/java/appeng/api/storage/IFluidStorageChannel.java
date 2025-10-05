package appeng.api.storage;

import net.neoforged.neoforge.fluids.FluidStack;

public interface IFluidStorageChannel extends IStorageChannel<FluidStack> {
    FluidStack insert(FluidStack stack, boolean simulate);

    FluidStack extract(FluidStack filter, int amount, boolean simulate);

    Iterable<FluidStackView> getAll();
}
