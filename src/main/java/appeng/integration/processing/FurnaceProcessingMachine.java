package appeng.integration.processing;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import appeng.api.integration.machines.SingleSlotProcessingMachine;
import appeng.crafting.CraftingJob;

/**
 * Wraps a vanilla furnace block entity and exposes it as an {@link appeng.api.integration.machines.IProcessingMachine}.
 */
public class FurnaceProcessingMachine extends SingleSlotProcessingMachine {
    private static final Logger LOG = LoggerFactory.getLogger(FurnaceProcessingMachine.class);

    private final ServerLevel level;
    private final BlockPos furnacePos;
    private final UUID gridId;

    private ItemStack pendingInput = ItemStack.EMPTY;
    private ItemStack pendingOutput = ItemStack.EMPTY;
    private int pendingCookTime = CraftingJob.DEFAULT_TICKS_REQUIRED;

    public FurnaceProcessingMachine(ServerLevel level, BlockPos furnacePos, UUID gridId) {
        this.level = level;
        this.furnacePos = furnacePos.immutable();
        this.gridId = gridId;
    }

    @Override
    public UUID getGridId() {
        return gridId;
    }

    public BlockPos getFurnacePos() {
        return furnacePos;
    }

    public ServerLevel getLevel() {
        return level;
    }

    @Override
    public boolean canProcess(CraftingJob job) {
        if (!super.canProcess(job)) {
            return false;
        }
        if (gridId == null) {
            return false;
        }
        if (getFurnace() == null) {
            return false;
        }
        if (job.getInputs().size() != 1 || job.getOutputs().size() != 1) {
            return false;
        }
        ItemStack input = job.getInputs().get(0).asStack();
        return hasRecipe(input);
    }

    @Override
    protected void handleSingleSlotInput(CraftingJob job, ProcessingMachineTransfer transfer) {
        if (job.getInputs().isEmpty()) {
            throw new IllegalStateException("Processing job does not define furnace inputs.");
        }
        transfer.pushToMachine(job.getInputs().get(0).asStack());
    }

    @Override
    protected void handleSingleSlotOutput(CraftingJob job, ProcessingMachineTransfer transfer) {
        if (job.getOutputs().isEmpty()) {
            return;
        }
        transfer.pullFromMachine(job.getOutputs().get(0).asStack());
    }

    /**
     * Inserts the provided stack into the furnace input slot. Returns any remainder that could not be inserted.
     */
    @Override
    protected ItemStack insertInput(ItemStack stack) {
        AbstractFurnaceBlockEntity furnace = getFurnace();
        if (furnace == null) {
            return stack;
        }
        if (!furnace.getItem(0).isEmpty()) {
            return stack;
        }

        Optional<RecipeHolder<? extends AbstractCookingRecipe>> recipe = findRecipe(stack);
        if (recipe.isEmpty()) {
            LOG.debug("No furnace recipe found for {} at {}", stack, furnacePos);
            return stack;
        }

        ItemStack result = recipe.get().value().getResultItem(level.registryAccess()).copy();
        if (result.isEmpty()) {
            return stack;
        }
        result.setCount(result.getCount() * stack.getCount());

        pendingInput = stack.copy();
        pendingOutput = result;
        int cookTime = recipe.get().value().getCookingTime();
        if (cookTime <= 0) {
            cookTime = CraftingJob.DEFAULT_TICKS_REQUIRED;
        }
        pendingCookTime = Math.max(cookTime, 1) * Math.max(stack.getCount(), 1);

        furnace.setItem(0, stack.copy());
        furnace.setItem(2, ItemStack.EMPTY);
        furnace.setChanged();

        return ItemStack.EMPTY;
    }

    /**
     * Removes any pending input from the furnace, returning it for network reinsertion.
     */
    @Override
    protected ItemStack releaseInput() {
        AbstractFurnaceBlockEntity furnace = getFurnace();
        ItemStack toReturn = ItemStack.EMPTY;
        if (furnace != null) {
            ItemStack slot = furnace.getItem(0);
            if (!slot.isEmpty()) {
                toReturn = slot.copy();
                furnace.setItem(0, ItemStack.EMPTY);
                furnace.setChanged();
            }
        }
        if (toReturn.isEmpty() && !pendingInput.isEmpty()) {
            toReturn = pendingInput.copy();
        }
        pendingInput = ItemStack.EMPTY;
        return toReturn;
    }

    /**
     * Performs the smelting step for the pending input and prepares the output in the furnace.
     *
     * @return The number of ticks that should be recorded for this job.
     */
    @Override
    protected int runMachine(CraftingJob job) {
        AbstractFurnaceBlockEntity furnace = getFurnace();
        if (furnace == null) {
            throw new IllegalStateException("Furnace block entity is no longer present.");
        }
        if (pendingInput.isEmpty()) {
            return CraftingJob.DEFAULT_TICKS_REQUIRED;
        }

        if (!job.getOutputs().isEmpty()) {
            ItemStack expected = job.getOutputs().get(0).asStack();
            if (!expected.isEmpty()) {
                pendingOutput = expected.copy();
            }
        }

        furnace.setItem(0, ItemStack.EMPTY);
        furnace.setItem(2, pendingOutput.copy());
        furnace.setChanged();

        pendingInput = ItemStack.EMPTY;
        return Math.max(pendingCookTime, CraftingJob.DEFAULT_TICKS_REQUIRED);
    }

    /**
     * Extracts the prepared output from the furnace.
     */
    @Override
    protected ItemStack extractOutput(ItemStack requested) {
        AbstractFurnaceBlockEntity furnace = getFurnace();
        if (furnace == null) {
            return ItemStack.EMPTY;
        }
        ItemStack output = furnace.getItem(2).copy();
        if (output.isEmpty() && !pendingOutput.isEmpty()) {
            output = pendingOutput.copy();
        }
        furnace.setItem(2, ItemStack.EMPTY);
        furnace.setChanged();
        pendingOutput = ItemStack.EMPTY;
        pendingCookTime = CraftingJob.DEFAULT_TICKS_REQUIRED;
        return output;
    }

    @Override
    protected void dropItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        Containers.dropItemStack(level, furnacePos.getX() + 0.5, furnacePos.getY() + 0.5, furnacePos.getZ() + 0.5,
                stack);
    }

    private boolean hasRecipe(ItemStack stack) {
        return findRecipe(stack).isPresent();
    }

    private Optional<RecipeHolder<? extends AbstractCookingRecipe>> findRecipe(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        SimpleContainer container = new SimpleContainer(stack.copy());
        return level.getRecipeManager().getRecipeFor(getRecipeType(), container, level);
    }

    private AbstractFurnaceBlockEntity getFurnace() {
        if (!level.isLoaded(furnacePos)) {
            return null;
        }
        if (level.getBlockEntity(furnacePos) instanceof AbstractFurnaceBlockEntity furnace) {
            if (isValidFurnace(furnace)) {
                return furnace;
            }
        }
        return null;
    }

    protected RecipeType<? extends AbstractCookingRecipe> getRecipeType() {
        return RecipeType.SMELTING;
    }

    protected boolean isValidFurnace(AbstractFurnaceBlockEntity furnace) {
        return true;
    }

    @Override
    public void finishProcessing(CraftingJob job) {
        super.finishProcessing(job);
        pendingInput = ItemStack.EMPTY;
        pendingOutput = ItemStack.EMPTY;
        pendingCookTime = CraftingJob.DEFAULT_TICKS_REQUIRED;
    }

    @Override
    protected String getStartTranslationKey() {
        return "message.appliedenergistics2.processing_job.external_furnace_started";
    }

    @Override
    protected String getCompleteTranslationKey() {
        return "message.appliedenergistics2.processing_job.external_furnace_complete";
    }

    @Override
    protected String getFailedTranslationKey() {
        return "message.appliedenergistics2.processing_job.external_furnace_failed";
    }

    @Override
    public String toString() {
        return "FurnaceProcessingMachine{" + level.dimension().location() + "@" + furnacePos + "}";
    }
}
