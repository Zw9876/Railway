/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.equipment.potatoCannon.AllPotatoProjectileEntityHitActions.PotionEffect;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;

@SuppressWarnings("SameParameterValue")
public class CRPotatoProjectileTypes {
    public static void bootstrap(BootstrapContext<PotatoCannonProjectileType> ctx) {
        register(ctx, "paint_pitcher", builder()
            .damage(3)
            .reloadTicks(15)
            .velocity(1.25f)
            .knockback(1.5f)
            .renderTumbling()
            .onEntityHit(new PotionEffect(MobEffects.POISON, 1, 100, false))
            .onBlockHit(PaintAction.INSTANCE)
            .addItems(CRItems.FILLED_PITCHERS.toArray(ItemEntry[]::new))
        );
    }

    private static PotatoCannonProjectileType.Builder builder() {
        return new PotatoCannonProjectileType.Builder();
    }

    private static void register(BootstrapContext<PotatoCannonProjectileType> ctx, String name, PotatoCannonProjectileType.Builder builder) {
        ctx.register(ResourceKey.create(CreateRegistries.POTATO_PROJECTILE_TYPE, Railways.asResource(name)), builder.build());
    }

    public enum PaintAction implements PotatoProjectileBlockHitAction {
        INSTANCE;

        public static final MapCodec<PaintAction> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean execute(LevelAccessor level, ItemStack projectile, BlockHitResult ray) {
            if (projectile.getItem() instanceof PaintPitcherItem item) {
                item.projectilePaint(projectile, level, ray);
                return true;
            }
            return false;
        }

        @Override
        public MapCodec<? extends PotatoProjectileBlockHitAction> codec() {
            return CODEC;
        }
    }

    /**
     * 1.21 freezes the modded registries once the registry events are done, so this can no longer
     * run from FMLCommonSetupEvent - Registry.register threw "Registry is already frozen". Create's
     * own actions go in through AllPotatoProjectileBlockHitActions during registration, and ours
     * ride a DeferredRegister on the mod bus for the same reason.
     */
    private static final DeferredRegister<MapCodec<? extends PotatoProjectileBlockHitAction>> BLOCK_HIT_ACTIONS =
        DeferredRegister.create(CreateRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION, Railways.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends PotatoProjectileBlockHitAction>, MapCodec<PaintAction>> PAINT =
        BLOCK_HIT_ACTIONS.register("paint", () -> PaintAction.CODEC);

    /** Called from the platform entry point, which owns the mod event bus. */
    public static void register(IEventBus modEventBus) {
        BLOCK_HIT_ACTIONS.register(modEventBus);
    }
}
