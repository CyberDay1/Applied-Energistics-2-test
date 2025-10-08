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

package appeng.recipes.entropy;

import com.mojang.serialization.MapCodec;

//? if eval(current.version, "<=1.21.4") {
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
//? }
//? if eval(current.version, ">=1.21.5") {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
//? }
import net.minecraft.world.item.crafting.RecipeSerializer;

public class EntropyRecipeSerializer implements RecipeSerializer<EntropyRecipe> {

    @Override
    public MapCodec<EntropyRecipe> codec() {
        return EntropyRecipe.CODEC;
    }

//? if eval(current.version, "<=1.21.4") {
    @Override
    public EntropyRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return EntropyRecipe.STREAM_CODEC.decode(buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, EntropyRecipe recipe) {
        EntropyRecipe.STREAM_CODEC.encode(buffer, recipe);
    }
//? } else {
    @Override
    public StreamCodec<RegistryFriendlyByteBuf, EntropyRecipe> streamCodec() {
        return EntropyRecipe.STREAM_CODEC;
    }
//? }
}
