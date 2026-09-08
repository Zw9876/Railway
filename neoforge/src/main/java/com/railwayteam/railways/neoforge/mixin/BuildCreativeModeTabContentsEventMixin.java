/*
 * Steam 'n' Rails
 * Copyright (c) 2024 The Railways Team
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

import com.railwayteam.railways.Railways;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.InsertableLinkedOpenCustomHashSet;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Offering a stack that a tab already holds is a hard {@code IllegalArgumentException} in NeoForge
 * 21.1 - "Itemstack &lt;x&gt; already exists in the tab's list" - which FML turns into a
 * ModLoadingException and a crash the moment the creative inventory is opened. 1.20 ignored the
 * repeat, so this was invisible before the port.
 *
 * <p>Railways builds its three tabs with its own {@code RegistrateDisplayItemsGenerator}, which
 * already emits a de-duplicated list. The duplicate therefore comes from a listener adding an entry
 * the generator had placed, and the throw is a strictly worse outcome than ignoring the second add -
 * the item is in the tab either way. Skip it, and log the offending stack with the stack trace of
 * whoever offered it so the real source can be named rather than guessed at.
 */
@Mixin(value = BuildCreativeModeTabContentsEvent.class, remap = false)
public abstract class BuildCreativeModeTabContentsEventMixin {

    @Shadow @Final private InsertableLinkedOpenCustomHashSet<ItemStack> parentEntries;
    @Shadow @Final private InsertableLinkedOpenCustomHashSet<ItemStack> searchEntries;

    @Unique
    private boolean railways$reportedDuplicate = false;

    @Inject(method = "accept(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V",
        at = @At("HEAD"), cancellable = true)
    private void railways$skipDuplicateEntry(ItemStack stack, CreativeModeTab.TabVisibility visibility, CallbackInfo ci) {
        if (stack == null || stack.getCount() != 1)
            return;

        // isParentTab/isSearchTab on the event are package-private, and TabVisibility only has the
        // three constants, so test them directly rather than widening more of NeoForge.
        boolean duplicate =
            (visibility != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY && parentEntries.contains(stack))
                || (visibility != CreativeModeTab.TabVisibility.PARENT_TAB_ONLY && searchEntries.contains(stack));
        if (!duplicate)
            return;

        if (!railways$reportedDuplicate) {
            railways$reportedDuplicate = true;
            Railways.LOGGER.warn(
                "Ignoring a repeat creative tab entry for {} - it is already in the tab. Crashing here is worse "
                    + "than ignoring it, but something is adding it twice; the trace below is the second caller.",
                stack, new Throwable("second add of " + stack));
        }
        ci.cancel();
    }
}
