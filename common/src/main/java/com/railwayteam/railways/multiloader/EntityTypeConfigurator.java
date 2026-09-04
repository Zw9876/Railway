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

package com.railwayteam.railways.multiloader;

import net.minecraft.world.entity.EntityType;

/**
 * Thin wrapper over {@link EntityType.Builder}.
 * <p>
 * This used to be an @ExpectPlatform abstraction because Fabric and Forge exposed
 * different builder types. This is a NeoForge-only tree now, so it delegates directly
 * and no longer needs a platform implementation.
 */
public class EntityTypeConfigurator {
	private final EntityType.Builder<?> builder;

	private EntityTypeConfigurator(EntityType.Builder<?> builder) {
		this.builder = builder;
	}

	public static EntityTypeConfigurator of(Object builder) {
		if (builder instanceof EntityType.Builder<?> typeBuilder)
			return new EntityTypeConfigurator(typeBuilder);
		throw new IllegalArgumentException("builder must be an EntityType.Builder");
	}

	public EntityTypeConfigurator size(float width, float height) {
		builder.sized(width, height);
		return this;
	}

	/** 1.21 moved eye height off the entity (getStandingEyeHeight) onto the type. */
	public EntityTypeConfigurator eyeHeight(float eyeHeight) {
		builder.eyeHeight(eyeHeight);
		return this;
	}

	public EntityTypeConfigurator fireImmune() {
		builder.fireImmune();
		return this;
	}
}
