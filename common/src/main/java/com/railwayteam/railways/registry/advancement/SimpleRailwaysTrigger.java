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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SimpleRailwaysTrigger extends CriterionTriggerBase<SimpleRailwaysTrigger.Instance> {

	/**
	 * 1.21 replaced createInstance(JsonObject, DeserializationContext) with a
	 * Codec. This trigger carries no data of its own, so the only field is the
	 * optional player predicate every SimpleInstance has.
	 */
	public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i
		.group(ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Instance::player))
		.apply(i, Instance::new));

	public SimpleRailwaysTrigger(String id) {
		super(id);
	}

	@Override
	public Codec<Instance> codec() {
		return CODEC;
	}

	public void trigger(ServerPlayer player) {
		// Cast disambiguates our (ServerPlayer, List) overload from
		// SimpleCriterionTrigger's (ServerPlayer, Predicate).
		super.trigger(player, (List<Supplier<Object>>) null);
	}

	public Instance instance() {
		return new Instance(Optional.empty());
	}

	public static class Instance extends CriterionTriggerBase.Instance {

		public Instance(Optional<ContextAwarePredicate> player) {
			super(player);
		}

		@Override
		protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
			return true;
		}
	}
}
