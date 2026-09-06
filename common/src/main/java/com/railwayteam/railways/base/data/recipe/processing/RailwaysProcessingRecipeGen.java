/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

package com.railwayteam.railways.base.data.recipe.processing;

import net.minecraft.core.HolderLookup;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.base.data.recipe.RailwaysRecipeProvider;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class RailwaysProcessingRecipeGen extends RailwaysRecipeProvider {

	protected static final List<RailwaysProcessingRecipeGen> GENERATORS = new ArrayList<>();

	public static DataProvider registerAll(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		GENERATORS.add(new RailwaysMixingRecipeGen(output, registries));
		GENERATORS.add(new RailwaysItemApplicationRecipeGen(output, registries));

		return new DataProvider() {

			@Override
			public @NotNull String getName() {
				return "Railways' Processing Recipes";
			}

			@Override
			public @NotNull CompletableFuture<?> run(@NotNull CachedOutput dc) {
				return CompletableFuture.allOf(GENERATORS.stream()
					.map(gen -> gen.run(dc))
					.toArray(CompletableFuture[]::new));
			}
		};
	}

	public RailwaysProcessingRecipeGen(PackOutput generator, CompletableFuture<HolderLookup.Provider> registries) {
		super(generator, registries);
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	protected <T extends StandardProcessingRecipe<?>> GeneratedRecipe create(String namespace,
		Supplier<ItemLike> singleIngredient, UnaryOperator<StandardProcessingRecipe.Builder<T>> transform) {
		StandardProcessingRecipe.Serializer<T> serializer = getSerializer();
		GeneratedRecipe generatedRecipe = c -> {
			ItemLike itemLike = singleIngredient.get();
			transform
				.apply(new StandardProcessingRecipe.Builder<>(serializer.factory(),
					ResourceLocation.fromNamespaceAndPath(namespace, RegisteredObjectsHelper.getKeyOrThrow(itemLike.asItem())
						.getPath())).withItemIngredients(Ingredient.of(itemLike)))
				.build(c);
		};
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a processing recipe with a single itemstack ingredient, using its id
	 * as the name of the recipe
	 */
	<T extends StandardProcessingRecipe<?>> GeneratedRecipe create(Supplier<ItemLike> singleIngredient,
		UnaryOperator<StandardProcessingRecipe.Builder<T>> transform) {
		return create(Railways.MOD_ID, singleIngredient, transform);
	}

	protected <T extends StandardProcessingRecipe<?>> GeneratedRecipe createWithDeferredId(Supplier<ResourceLocation> name,
		UnaryOperator<StandardProcessingRecipe.Builder<T>> transform) {
		StandardProcessingRecipe.Serializer<T> serializer = getSerializer();
		GeneratedRecipe generatedRecipe =
			c -> transform.apply(new StandardProcessingRecipe.Builder<>(serializer.factory(), name.get()))
				.build(c);
		all.add(generatedRecipe);
		return generatedRecipe;
	}

	/**
	 * Create a new processing recipe, with recipe definitions provided by the
	 * function
	 */
	protected <T extends StandardProcessingRecipe<?>> GeneratedRecipe create(ResourceLocation name,
		UnaryOperator<StandardProcessingRecipe.Builder<T>> transform) {
		return createWithDeferredId(() -> name, transform);
	}

	/**
	 * Create a new processing recipe, with recipe definitions provided by the
	 * function
	 */
	<T extends StandardProcessingRecipe<?>> GeneratedRecipe create(String name,
		UnaryOperator<StandardProcessingRecipe.Builder<T>> transform) {
		return create(Railways.asResource(name), transform);
	}

	protected abstract IRecipeTypeInfo getRecipeType();

	protected <T extends StandardProcessingRecipe<?>> StandardProcessingRecipe.Serializer<T> getSerializer() {
		return getRecipeType().getSerializer();
	}

	protected Supplier<ResourceLocation> idWithSuffix(Supplier<ItemLike> item, String suffix) {
		return () -> {
			ResourceLocation registryName = RegisteredObjectsHelper.getKeyOrThrow(item.get()
				.asItem());
			return Railways.asResource(registryName.getPath() + suffix);
		};
	}


}
