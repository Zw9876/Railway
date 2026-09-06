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


package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

/**
 * 1.21 turned ArmorMaterial from an interface into a record held in the
 * ARMOR_MATERIAL registry, so the conductor cap's material can no longer be an
 * inner class on the item. Mirrors Create's own AllArmorMaterials.
 */
public class CRArmorMaterials {
    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
        DeferredRegister.create(Registries.ARMOR_MATERIAL, Railways.MOD_ID);

    /**
     * The conductor cap is cosmetic: the old ConductorArmorMaterial returned 0 for
     * durability, defense, enchantment value, toughness and knockback resistance, and
     * Ingredient.EMPTY for repair. The layer list is empty because ConductorCapModel
     * draws the cap, not the vanilla armor layer.
     */
    public static final Holder<ArmorMaterial> CONDUCTOR_CAP = ARMOR_MATERIALS.register("conductor_cap", () -> {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        for (ArmorItem.Type type : ArmorItem.Type.values())
            defense.put(type, 0);
        return new ArmorMaterial(defense, 0, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.EMPTY,
            List.of(), 0.0F, 0.0F);
    });

    /** Called from the platform entry point, which owns the mod event bus. */
    public static void register(IEventBus modEventBus) {
        ARMOR_MATERIALS.register(modEventBus);
    }
}
