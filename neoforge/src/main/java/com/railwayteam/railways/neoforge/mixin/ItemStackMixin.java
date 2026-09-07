/*
 * Steam 'n' Rails
 * Copyright (c) 2025-2026 The Railways Team
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

package com.railwayteam.railways.neoforge.mixin;

import com.railwayteam.railways.mixin_interfaces.ItemStackDuck;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 1.21 note: ItemStack no longer keeps a Holder.Reference delegate beside the item, and NeoForge's
 * forgeInit is gone with the old capability system, so swapping the item is now just the one field.
 *
 * <p>The component map still has to be rebuilt, though: it is a patch layered over the item's
 * default components, and the two paint pitchers do not share defaults (the filled ones are
 * stacksTo(1), the empty one is not). Re-prototyping onto the new item's defaults keeps the patch —
 * colour, fill level — while picking up the new item's own defaults.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemStackDuck {
    @Mutable
    @Shadow @Final private Item item;

    @Mutable
    @Shadow @Final PatchedDataComponentMap components;

    @Override
    public void railways$setItem(Item item) {
        DataComponentPatch patch = this.components.asPatch();
        this.item = item;
        this.components = PatchedDataComponentMap.fromPatch(item.components(), patch);
    }
}
