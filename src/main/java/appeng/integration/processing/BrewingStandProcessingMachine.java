package appeng.integration.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.network.chat.Component;

import appeng.api.integration.machines.MultiSlotProcessingMachine;
import appeng.api.integration.machines.ProcessingMachineHealth;
import appeng.crafting.CraftingJob;

/**
 * Wraps a vanilla brewing stand block entity and exposes it as an
 * {@link appeng.api.integration.machines.IProcessingMachine} implementation.
 */
public class BrewingStandProcessingMachine extends MultiSlotProcessingMachine {
    private static final Logger LOG = LoggerFactory.getLogger(BrewingStandProcessingMachine.class);

    private static final int BOTTLE_SLOTS = 3;

    private final ServerLevel level;
    private final BlockPos standPos;
    private final UUID gridId;

    private final ItemStack[] stagedBottles = new ItemStack[BOTTLE_SLOTS];
    private final ItemStack[] preparedOutputs = new ItemStack[BOTTLE_SLOTS];

    private ItemStack stagedIngredient = ItemStack.EMPTY;
    private int nextInputSlot;
    private int nextOutputSlot;

    public BrewingStandProcessingMachine(ServerLevel level, BlockPos standPos, UUID gridId) {
        this.level = level;
        this.standPos = standPos.immutable();
        this.gridId = gridId;
        resetState();
    }

    @Override
    public UUID getGridId() {
        return gridId;
    }

    public BlockPos getStandPos() {
        return standPos;
    }

    public ServerLevel getLevel() {
        return level;
    }

    @Override
    public ProcessingMachineHealth getHealth() {
        BrewingStandBlockEntity stand = getBrewingStand();
        if (stand == null || stand.isRemoved()) {
            return ProcessingMachineHealth.offline(
                    Component.literal("Brewing stand at " + standPos + " is unavailable"));
        }
        return ProcessingMachineHealth.healthy();
    }

    @Override
    public void beginProcessing(CraftingJob job) {
        super.beginProcessing(job);
        resetState();
    }

    @Override
    public boolean canProcess(CraftingJob job) {
        if (!super.canProcess(job)) {
            return false;
        }
        if (gridId == null) {
            return false;
        }
        if (getBrewingStand() == null) {
            return false;
        }
        if (job.getInputs().size() < BOTTLE_SLOTS + 1) {
            return false;
        }

        var brewing = level.potionBrewing();
        ItemStack ingredient = job.getInputs().get(BOTTLE_SLOTS).asStack();
        if (ingredient.isEmpty() || !brewing.isIngredient(ingredient)) {
            return false;
        }

        boolean hasBottle = false;
        for (int i = 0; i < BOTTLE_SLOTS; i++) {
            ItemStack bottle = job.getInputs().get(i).asStack();
            if (bottle.isEmpty()) {
                continue;
            }

            hasBottle = true;

            if (!brewing.hasMix(ingredient, bottle)) {
                return false;
            }

            ItemStack brewed = brewing.mix(ingredient, bottle);
            if (brewed.isEmpty()) {
                return false;
            }

            if (i < job.getOutputs().size()) {
                ItemStack expected = job.getOutputs().get(i).asStack();
                if (!expected.isEmpty()
                        && !ItemStack.isSameItemSameComponents(expected, brewed)) {
                    return false;
                }
            }
        }

        return hasBottle;
    }

    @Override
    protected List<ItemStack> mapInputs(CraftingJob job) {
        List<ItemStack> inputs = new ArrayList<>(BOTTLE_SLOTS + 1);
        for (int i = 0; i < BOTTLE_SLOTS; i++) {
            if (i < job.getInputs().size()) {
                inputs.add(job.getInputs().get(i).asStack());
            } else {
                inputs.add(ItemStack.EMPTY);
            }
        }
        if (BOTTLE_SLOTS < job.getInputs().size()) {
            inputs.add(job.getInputs().get(BOTTLE_SLOTS).asStack());
        } else {
            inputs.add(ItemStack.EMPTY);
        }
        return inputs;
    }

    @Override
    protected List<ItemStack> mapOutputs(CraftingJob job) {
        List<ItemStack> outputs = new ArrayList<>(BOTTLE_SLOTS);
        for (int i = 0; i < BOTTLE_SLOTS; i++) {
            if (i < job.getOutputs().size()) {
                outputs.add(job.getOutputs().get(i).asStack());
            } else {
                outputs.add(ItemStack.EMPTY);
            }
        }
        return outputs;
    }

    @Override
    protected ItemStack insertInput(ItemStack stack) {
        BrewingStandBlockEntity stand = getBrewingStand();
        if (stand == null || stack.isEmpty()) {
            return stack;
        }

        if (nextInputSlot < BOTTLE_SLOTS) {
            int slot = nextInputSlot;
            if (!stand.getItem(slot).isEmpty()) {
                return stack;
            }

            ItemStack copy = stack.copy();
            stagedBottles[slot] = copy;
            stand.setItem(slot, copy);
            stand.setChanged();
            nextInputSlot++;
            return ItemStack.EMPTY;
        }

        if (nextInputSlot == BOTTLE_SLOTS) {
            if (!stand.getItem(BrewingStandBlockEntity.INGREDIENT_SLOT).isEmpty()) {
                return stack;
            }

            stagedIngredient = stack.copy();
            stand.setItem(BrewingStandBlockEntity.INGREDIENT_SLOT, stagedIngredient.copy());
            stand.setChanged();
            nextInputSlot++;
            return ItemStack.EMPTY;
        }

        return stack;
    }

    @Override
    protected ItemStack releaseInput() {
        BrewingStandBlockEntity stand = getBrewingStand();
        if (stand != null) {
            boolean changed = false;
            for (int slot = 0; slot < BOTTLE_SLOTS; slot++) {
                if (!stand.getItem(slot).isEmpty()) {
                    stand.setItem(slot, ItemStack.EMPTY);
                    changed = true;
                }
            }
            if (!stand.getItem(BrewingStandBlockEntity.INGREDIENT_SLOT).isEmpty()) {
                stand.setItem(BrewingStandBlockEntity.INGREDIENT_SLOT, ItemStack.EMPTY);
                changed = true;
            }
            if (changed) {
                stand.setChanged();
            }
        }

        resetState();
        return ItemStack.EMPTY;
    }

    @Override
    protected int runMachine(CraftingJob job) {
        BrewingStandBlockEntity stand = getBrewingStand();
        if (stand == null) {
            throw new IllegalStateException("Brewing stand block entity is no longer present.");
        }
        if (stagedIngredient.isEmpty()) {
            stagedIngredient = stand.getItem(BrewingStandBlockEntity.INGREDIENT_SLOT).copy();
        }
        if (stagedIngredient.isEmpty()) {
            return CraftingJob.DEFAULT_TICKS_REQUIRED;
        }

        PotionBrewing brewing = level.potionBrewing();
        ItemStack ingredient = stagedIngredient.copy();

        for (int slot = 0; slot < BOTTLE_SLOTS; slot++) {
            ItemStack bottle = stagedBottles[slot];
            if (bottle.isEmpty()) {
                bottle = stand.getItem(slot).copy();
            }
            if (bottle.isEmpty()) {
                preparedOutputs[slot] = ItemStack.EMPTY;
                continue;
            }

            ItemStack brewed = brewing.mix(ingredient, bottle);
            if (brewed.isEmpty()) {
                throw new IllegalStateException("Unable to brew input " + bottle + " with ingredient " + ingredient);
            }

            if (slot < job.getOutputs().size()) {
                ItemStack expected = job.getOutputs().get(slot).asStack();
                if (!expected.isEmpty()) {
                    if (!ItemStack.isSameItemSameComponents(expected, brewed)) {
                        throw new IllegalStateException("Brewed result " + brewed + " does not match expected " + expected);
                    }
                    brewed = expected.copy();
                }
            }

            preparedOutputs[slot] = brewed.copy();
            stand.setItem(slot, brewed.copy());
        }

        stand.setItem(BrewingStandBlockEntity.INGREDIENT_SLOT, ItemStack.EMPTY);
        stand.setChanged();

        stagedIngredient = ItemStack.EMPTY;
        nextOutputSlot = 0;
        Arrays.fill(stagedBottles, ItemStack.EMPTY);

        return Math.max(PotionBrewing.BREWING_TIME_SECONDS * 20, CraftingJob.DEFAULT_TICKS_REQUIRED);
    }

    @Override
    protected ItemStack extractOutput(ItemStack requested) {
        BrewingStandBlockEntity stand = getBrewingStand();
        if (stand == null) {
            return ItemStack.EMPTY;
        }

        while (nextOutputSlot < BOTTLE_SLOTS) {
            int slot = nextOutputSlot++;
            ItemStack output = stand.getItem(slot).copy();
            if (output.isEmpty()) {
                output = preparedOutputs[slot];
            }

            preparedOutputs[slot] = ItemStack.EMPTY;

            if (output.isEmpty()) {
                continue;
            }

            stand.setItem(slot, ItemStack.EMPTY);
            stand.setChanged();

            if (!requested.isEmpty() && !ItemStack.isSameItemSameComponents(output, requested)) {
                LOG.debug("Requested output {} did not match brewed stack {}; returning brewed result.", requested, output);
            }

            return output;
        }

        return ItemStack.EMPTY;
    }

    @Override
    protected void dropItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        Containers.dropItemStack(level, standPos.getX() + 0.5, standPos.getY() + 0.5, standPos.getZ() + 0.5, stack);
    }

    @Override
    public void finishProcessing(CraftingJob job) {
        super.finishProcessing(job);
        resetState();
    }

    @Override
    protected String getStartTranslationKey() {
        return "message.appliedenergistics2.processing_job.external_brewing_started";
    }

    @Override
    protected String getCompleteTranslationKey() {
        return "message.appliedenergistics2.processing_job.external_brewing_complete";
    }

    @Override
    protected String getFailedTranslationKey() {
        return "message.appliedenergistics2.processing_job.external_brewing_failed";
    }

    @Override
    public String toString() {
        return "BrewingStandProcessingMachine{" + level.dimension().location() + "@" + standPos + "}";
    }

    private BrewingStandBlockEntity getBrewingStand() {
        if (!level.isLoaded(standPos)) {
            LOG.debug("Brewing stand chunk at {} is not loaded for {}", standPos, this);
            return null;
        }
        var blockEntity = level.getBlockEntity(standPos);
        if (blockEntity instanceof BrewingStandBlockEntity stand) {
            return stand;
        }
        LOG.debug("Expected brewing stand at {} but found {}", standPos, blockEntity);
        return null;
    }

    private void resetState() {
        Arrays.fill(stagedBottles, ItemStack.EMPTY);
        Arrays.fill(preparedOutputs, ItemStack.EMPTY);
        stagedIngredient = ItemStack.EMPTY;
        nextInputSlot = 0;
        nextOutputSlot = 0;
    }
}
