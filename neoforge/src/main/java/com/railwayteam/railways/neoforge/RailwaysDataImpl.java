/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

package com.railwayteam.railways.neoforge;

import com.railwayteam.railways.Railways;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Datagen entry point. The dropped Fabric module used to be the only caller of
 * {@link Railways#gatherData}, so nothing ran datagen after the port and the committed output under
 * {@code common/src/generated/resources} was still 1.20-shaped.
 *
 * <p>Registered at {@code HIGHEST} priority on purpose: gatherData also installs Registrate's own
 * generators through {@code REGISTRATE.addDataGenerator}, and those have to be in place before
 * Registrate's listener — added at default priority by {@code registerEventListeners} — builds its
 * provider.
 */
public class RailwaysDataImpl {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();
        Railways.gatherData(generator.getVanillaPack(true), registries);
    }
}
