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

package com.railwayteam.railways.neoforge.events;

import net.neoforged.neoforge.event.tick.LevelTickEvent;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolbox;
import com.railwayteam.railways.content.fuel.LiquidFuelManager;
import com.railwayteam.railways.events.CommonEvents;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber
public class CommonEventsForge {
	// 1.21 split TickEvent into per-type events whose Pre/Post are separate classes,
	// so the old phase check becomes the subscription itself.
	@SubscribeEvent
	public static void onWorldTick(LevelTickEvent.Pre event) {
		CommonEvents.onWorldTickStart(event.getLevel());
	}

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player)
			CommonEvents.onPlayerJoin(player);
	}

	@SubscribeEvent
	public static void onTagsUpdated(TagsUpdatedEvent event) {
		if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
			CommonEvents.onTagsUpdated();
	}

	@SubscribeEvent
	public static void addReloadListeners(AddReloadListenerEvent event) {
		event.addListener(LiquidFuelManager.ReloadListener.INSTANCE);
	}
}
