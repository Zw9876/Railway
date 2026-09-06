/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.base.data.recipe;

import net.neoforged.neoforge.common.extensions.IRecipeOutputExtension;
import com.railwayteam.railways.Railways;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import com.simibubi.create.content.trains.track.TrackMaterial;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds conditional-recipe support to Create's sequenced assembly builder.
 * <p>
 * This used to hand-translate Fabric resource conditions into Forge ones in raw JSON.
 * NeoForge supports conditions natively via {@link RecipeOutput#withConditions}, and this
 * is a NeoForge-only tree now, so all of that translation is gone.
 */
public class RailwaysSequencedAssemblyRecipeBuilder extends SequencedAssemblyRecipeBuilder {
    private final List<ICondition> recipeConditions = new ArrayList<>();

    public RailwaysSequencedAssemblyRecipeBuilder(ResourceLocation id) {
        super(id);
    }

    /**
     * If the material is from another mod, add a recipe condition for the mod.
     * @param trackMaterial the material
     * @return this
     */
    public RailwaysSequencedAssemblyRecipeBuilder conditionalMaterial(TrackMaterial trackMaterial) {
        String namespace = trackMaterial.id.getNamespace();
        if (!Railways.MOD_ID.equals(namespace)) {
            recipeConditions.add(new ModLoadedCondition(namespace));
        }
        return this;
    }

    @Override
    public void build(RecipeOutput output) {
        super.build(recipeConditions.isEmpty()
            ? output
            // RecipeOutput extends IRecipeOutputExtension under NeoForge, but that patch lives
            // in the patched Minecraft jar, which :common does not compile against. The
            // interface itself ships in neoforge-universal, so cast to reach withConditions.
            : ((IRecipeOutputExtension) output).withConditions(recipeConditions.toArray(new ICondition[0])));
    }
}
