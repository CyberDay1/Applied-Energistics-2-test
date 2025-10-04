/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import appeng.recipe.InscriberRecipe;
import appeng.registry.AE2Items;

public class InscriberRecipeProvider extends RecipeProvider {
    public InscriberRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        output.accept(new ResourceLocation("appliedenergistics2", "inscriber/printed_silicon"),
                new InscriberRecipe(
                        new ItemStack(AE2Items.INSCRIBER_SILICON_PRESS.get()),
                        new ItemStack(AE2Items.SILICON.get()),
                        ItemStack.EMPTY,
                        new ItemStack(AE2Items.PRINTED_SILICON.get())),
                null);

        output.accept(new ResourceLocation("appliedenergistics2", "inscriber/printed_logic"),
                new InscriberRecipe(
                        new ItemStack(AE2Items.INSCRIBER_LOGIC_PRESS.get()),
                        new ItemStack(Items.GOLD_INGOT),
                        ItemStack.EMPTY,
                        new ItemStack(AE2Items.PRINTED_LOGIC_PROCESSOR.get())),
                null);

        output.accept(new ResourceLocation("appliedenergistics2", "inscriber/printed_engineering"),
                new InscriberRecipe(
                        new ItemStack(AE2Items.INSCRIBER_ENGINEERING_PRESS.get()),
                        new ItemStack(Items.DIAMOND),
                        ItemStack.EMPTY,
                        new ItemStack(AE2Items.PRINTED_ENGINEERING_PROCESSOR.get())),
                null);

        output.accept(new ResourceLocation("appliedenergistics2", "inscriber/printed_calculation"),
                new InscriberRecipe(
                        new ItemStack(AE2Items.INSCRIBER_CALCULATION_PRESS.get()),
                        new ItemStack(AE2Items.CERTUS_QUARTZ_CRYSTAL.get()),
                        ItemStack.EMPTY,
                        new ItemStack(AE2Items.PRINTED_CALCULATION_PROCESSOR.get())),
                null);

        output.accept(new ResourceLocation("appliedenergistics2", "inscriber/logic_processor"),
                new InscriberRecipe(
                        new ItemStack(AE2Items.PRINTED_LOGIC_PROCESSOR.get()),
                        new ItemStack(Items.REDSTONE),
                        new ItemStack(AE2Items.PRINTED_SILICON.get()),
                        new ItemStack(AE2Items.LOGIC_PROCESSOR.get())),
                null);

        output.accept(new ResourceLocation("appliedenergistics2", "inscriber/engineering_processor"),
                new InscriberRecipe(
                        new ItemStack(AE2Items.PRINTED_ENGINEERING_PROCESSOR.get()),
                        new ItemStack(Items.REDSTONE),
                        new ItemStack(AE2Items.PRINTED_SILICON.get()),
                        new ItemStack(AE2Items.ENGINEERING_PROCESSOR.get())),
                null);

        output.accept(new ResourceLocation("appliedenergistics2", "inscriber/calculation_processor"),
                new InscriberRecipe(
                        new ItemStack(AE2Items.PRINTED_CALCULATION_PROCESSOR.get()),
                        new ItemStack(Items.REDSTONE),
                        new ItemStack(AE2Items.PRINTED_SILICON.get()),
                        new ItemStack(AE2Items.CALCULATION_PROCESSOR.get())),
                null);
    }
}
