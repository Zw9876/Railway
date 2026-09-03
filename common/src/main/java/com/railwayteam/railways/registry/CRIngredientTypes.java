/*
 * Steam 'n' Rails
 * Copyright (c) 2023-2025 The Railways Team
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

package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.compat.tracks.SoftIngredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Custom ingredient types. 1.21 made Ingredient final, so anything that used to
 * subclass it is now an ICustomIngredient registered here.
 */
public class CRIngredientTypes {
    private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, Railways.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<SoftIngredient>> SOFT =
        INGREDIENT_TYPES.register("soft", () -> new IngredientType<>(SoftIngredient.CODEC));

    /** Called from the platform entry point, which owns the mod event bus. */
    public static void register(IEventBus modEventBus) {
        INGREDIENT_TYPES.register(modEventBus);
    }
}
