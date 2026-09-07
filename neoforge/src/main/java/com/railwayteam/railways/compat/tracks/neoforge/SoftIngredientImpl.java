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

package com.railwayteam.railways.compat.tracks.neoforge;

import com.railwayteam.railways.compat.tracks.SoftIngredient;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import org.jetbrains.annotations.Nullable;

/**
 * Platform half of {@link SoftIngredient#unwrap(Ingredient)}. NeoForge patches
 * {@code getCustomIngredient} straight onto {@code Ingredient} rather than putting it behind an
 * I*Extension interface, so it exists only in the patched Minecraft jar this module compiles
 * against and cannot be reached from :common at all.
 */
public class SoftIngredientImpl {
    public static @Nullable SoftIngredient unwrap(Ingredient ingredient) {
        ICustomIngredient custom = ingredient.getCustomIngredient();
        return custom instanceof SoftIngredient soft ? soft : null;
    }
}
