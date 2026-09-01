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

package com.railwayteam.railways.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ItemUtils {
	@ExpectPlatform
	@Contract // shut
	public static boolean blocksEndermanView(ItemStack stack, Player wearer, EnderMan enderman) {
		throw new AssertionError();
	}

	public static InteractionHand oppositeHand(InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
	}

	/* ---------------- 1.21 data-component shims ----------------
	 *
	 * ItemStack NBT was replaced by data components in 1.20.5. Arbitrary mod
	 * data now lives in DataComponents.CUSTOM_DATA.
	 *
	 * IMPORTANT SEMANTIC CHANGE: getCustomTag() and getOrCreateCustomTag()
	 * return a COPY. The old ItemStack.getOrCreateTag() handed back the LIVE
	 * tag, so mutating it persisted. Mutating what these return does nothing
	 * unless you call setCustomTag() afterwards.
	 *
	 * Prefer mutateCustomTag() for read-modify-write -- it cannot be got wrong.
	 */

	public static @Nullable CompoundTag getCustomTag(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data == null ? null : data.copyTag();
	}

	public static CompoundTag getOrCreateCustomTag(ItemStack stack) {
		CompoundTag tag = getCustomTag(stack);
		return tag == null ? new CompoundTag() : tag;
	}

	public static boolean hasCustomTag(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null && !data.isEmpty();
	}

	/** Passing null or an empty tag removes the component entirely. */
	public static void setCustomTag(ItemStack stack, @Nullable CompoundTag tag) {
		if (tag == null || tag.isEmpty()) {
			stack.remove(DataComponents.CUSTOM_DATA);
		} else {
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}
	}

	/** Atomic read-modify-write; the safe replacement for mutating a live tag. */
	public static void mutateCustomTag(ItemStack stack, Consumer<CompoundTag> mutator) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, mutator);
	}

	public static void removeCustomTagKey(ItemStack stack, String key) {
		if (!hasCustomTag(stack)) return;
		mutateCustomTag(stack, tag -> tag.remove(key));
	}

	/* ------------------------------------------------------------ */

	/**
	 * NOTE: applyComponents MERGES rather than replacing, so unlike the old
	 * setTag-based version the target keeps any component the source does not
	 * define. Every current caller passes a freshly constructed stack, where
	 * the two behaviours coincide.
	 */
	public static void copyStackData(ItemStack source, ItemStack target) {
		target.applyComponents(source.getComponents());
	}

	public static boolean isUnbreakable(ItemStack stack) {
		if (stack.isEmpty()) return false;
		// "Unbreakable" was a vanilla NBT flag; in 1.21 it is a real component.
		return stack.has(DataComponents.UNBREAKABLE);
	}
}
