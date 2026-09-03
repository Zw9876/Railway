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

package com.railwayteam.railways.registry.advancement;

import com.railwayteam.railways.Railways;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 1.21 rewrote the criterion system. SimpleCriterionTrigger now owns all the
 * listener bookkeeping -- addPlayerListener / removePlayerListener /
 * removePlayerListeners are final there -- so this class no longer maintains
 * its own listener map and simply adapts our supplier-list test to the
 * Predicate the parent expects.
 * <p>
 * Triggers are also registered into BuiltInRegistries.TRIGGER_TYPES rather than
 * carrying their own id, and each must supply a Codec instead of a
 * createInstance(JsonObject, DeserializationContext).
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CriterionTriggerBase<T extends CriterionTriggerBase.Instance> extends SimpleCriterionTrigger<T> {

	private final ResourceLocation id;

	public CriterionTriggerBase(String id) {
		this.id = Railways.asResource(id);
	}

	/** Retained for registration; CriterionTrigger.getId() no longer exists. */
	public ResourceLocation getId() {
		return id;
	}

	protected void trigger(ServerPlayer player, @Nullable List<Supplier<Object>> suppliers) {
		super.trigger(player, instance -> instance.test(suppliers));
	}

	public abstract static class Instance implements SimpleCriterionTrigger.SimpleInstance {

		private final Optional<ContextAwarePredicate> player;

		public Instance(Optional<ContextAwarePredicate> player) {
			this.player = player;
		}

		@Override
		public Optional<ContextAwarePredicate> player() {
			return player;
		}

		protected abstract boolean test(@Nullable List<Supplier<Object>> suppliers);
	}
}
