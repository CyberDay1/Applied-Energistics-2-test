package appeng.integration.processing;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;

/**
 * Processing machine wrapper for the vanilla blast furnace.
 */
public class BlastFurnaceProcessingMachine extends FurnaceProcessingMachine {
    public BlastFurnaceProcessingMachine(ServerLevel level, BlockPos furnacePos, UUID gridId) {
        super(level, furnacePos, gridId);
    }

    @Override
    protected RecipeType<? extends AbstractCookingRecipe> getRecipeType() {
        return RecipeType.BLASTING;
    }

    @Override
    protected boolean isValidFurnace(AbstractFurnaceBlockEntity furnace) {
        return furnace instanceof BlastFurnaceBlockEntity;
    }

    @Override
    protected String getStartTranslationKey() {
        return "message.appliedenergistics2.processing_job.external_blast_furnace_started";
    }

    @Override
    protected String getCompleteTranslationKey() {
        return "message.appliedenergistics2.processing_job.external_blast_furnace_complete";
    }

    @Override
    protected String getFailedTranslationKey() {
        return "message.appliedenergistics2.processing_job.external_blast_furnace_failed";
    }

    @Override
    public String toString() {
        return "BlastFurnaceProcessingMachine{" + getLevel().dimension().location() + "@" + getFurnacePos() + "}";
    }
}
