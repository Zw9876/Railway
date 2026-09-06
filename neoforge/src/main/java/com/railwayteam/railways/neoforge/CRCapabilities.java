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

package com.railwayteam.railways.neoforge;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolbox;
import com.railwayteam.railways.content.palettes.painting.EmptyPaintPitcherItem;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.railwayteam.railways.content.palettes.painting.neoforge.PaintPitcherCapability;
import com.railwayteam.railways.mixin.AccessorToolboxBlockEntity;
import com.railwayteam.railways.registry.CREntities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * 1.21 capabilities are registered centrally instead of attached per object.
 * <p>
 * Forge asked each object for its capabilities - items overrode initCapabilities, and entities
 * got providers bolted on during AttachCapabilitiesEvent, all returning LazyOptional. NeoForge
 * inverts that: you declare up front which capability each item/entity/block-entity type
 * provides, and return the handler or null.
 */
public class CRCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {
        registerPaintPitchers(event);
        registerConductorToolbox(event);
    }

    /**
     * Every paint pitcher exposes a fluid handler. The old code overrode initCapabilities on the
     * two item classes, so rather than restate the item list here - which the palette colours
     * generate - the registry is filtered by those same classes.
     */
    private static void registerPaintPitchers(RegisterCapabilitiesEvent event) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof PaintPitcherItem || item instanceof EmptyPaintPitcherItem) {
                event.registerItem(Capabilities.FluidHandler.ITEM,
                    (stack, context) -> new PaintPitcherCapability(stack), item);
            }
        }
    }

    /**
     * A conductor exposes its mounted toolbox's inventory, so hoppers and the like can reach it.
     * Returning null when there is no toolbox is the NeoForge equivalent of the old
     * LazyOptional.empty().
     */
    private static void registerConductorToolbox(RegisterCapabilitiesEvent event) {
        event.registerEntity(Capabilities.ItemHandler.ENTITY, CREntities.CONDUCTOR.get(),
            (ConductorEntity conductor, Void context) -> {
                MountedToolbox toolbox = conductor.getToolbox();
                return toolbox == null ? null : ((AccessorToolboxBlockEntity) toolbox).getInventory();
            });
    }
}
