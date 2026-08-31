/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Phase 1 skeleton entrypoint.
 * <p>
 * Deliberately references nothing from :common, so that the NeoForge build and
 * runtime plumbing can be validated on 1.21.1 before the 63k-line common module
 * is ported. Phase 2 replaces this with the real RailwaysImpl.
 */
@Mod(RailwaysNeoForge.MOD_ID)
public class RailwaysNeoForge {
    public static final String MOD_ID = "railways";
    private static final Logger LOGGER = LoggerFactory.getLogger("Steam 'n' Rails");

    public RailwaysNeoForge(IEventBus modEventBus) {
        LOGGER.info("Steam 'n' Rails skeleton: constructing on NeoForge");
        modEventBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Steam 'n' Rails skeleton: common setup reached — NeoForge 1.21.1 plumbing OK");
    }
}
