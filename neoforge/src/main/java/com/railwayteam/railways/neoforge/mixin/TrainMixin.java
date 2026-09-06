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

package com.railwayteam.railways.neoforge.mixin;

import net.minecraft.world.item.crafting.RecipeType;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.railwayteam.railways.registry.CRTags;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Train.class)
public class TrainMixin {
    /**
     * Create 6 burns fuel via {@code stack.getBurnTime(null)} - an instance method NeoForge adds
     * to ItemStack - where 1.20 called the static {@code ForgeHooks.getBurnTime(stack, type)}.
     * The old @ModifyArg swapped the stack argument for AIR; a receiver cannot be modified that
     * way, so this wraps the call and reports a burn time of 0 instead. Same effect: an item
     * tagged not_train_fuel never burns.
     */
    @WrapOperation(method = "burnFuel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getBurnTime(Lnet/minecraft/world/item/crafting/RecipeType;)I"), remap = false)
    private int railways$disableFuelConsumptionBasedOnTag(ItemStack stack, RecipeType<?> recipeType, Operation<Integer> original) {
        if (stack.is(CRTags.AllItemTags.NOT_TRAIN_FUEL.tag)) {
            return 0;
        }
        return original.call(stack, recipeType);
    }
}

