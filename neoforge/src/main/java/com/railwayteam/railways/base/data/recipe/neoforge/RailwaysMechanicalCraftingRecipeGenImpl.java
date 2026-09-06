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

package com.railwayteam.railways.base.data.recipe.neoforge;

import com.railwayteam.railways.base.data.recipe.RailwaysMechanicalCraftingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * 1.21 replaced Consumer&lt;FinishedRecipe&gt; with RecipeOutput, and RecipeProvider takes the
 * registries future. The wrapper exists only because RailwaysMechanicalCraftingRecipeGen is
 * abstract; it forwards to a concrete instance.
 */
public class RailwaysMechanicalCraftingRecipeGenImpl extends RailwaysMechanicalCraftingRecipeGen {
    protected RailwaysMechanicalCraftingRecipeGenImpl(PackOutput pPackoutput,
                                                      CompletableFuture<HolderLookup.Provider> registries) {
        super(pPackoutput, registries);
    }

    public static RecipeProvider create(PackOutput gen, CompletableFuture<HolderLookup.Provider> registries) {
        RailwaysMechanicalCraftingRecipeGenImpl provider =
            new RailwaysMechanicalCraftingRecipeGenImpl(gen, registries);
        return new RecipeProvider(gen, registries) {
            @Override
            protected void buildRecipes(@NotNull RecipeOutput writer) {
                provider.buildRecipes(writer);
            }
        };
    }
}
