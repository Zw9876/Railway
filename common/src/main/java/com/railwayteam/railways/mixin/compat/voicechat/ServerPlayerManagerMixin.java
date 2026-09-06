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

package com.railwayteam.railways.mixin.compat.voicechat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.annotation.mixin.ConditionalMixin;
import com.railwayteam.railways.compat.Mods;
import com.railwayteam.railways.content.conductor.ConductorPossessionController;
import de.maxhenkel.voicechat.voice.server.ServerPlayerManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Simple Voice Chat 2.6 deleted ServerWorldUtils and moved getPlayersInRange onto
 * ServerPlayerManager, where it is split in two: the public static entry point delegates
 * to the instance getPlayersInRangeInternal, and getPlayersInRangeDirect is a second
 * path. Both read ServerPlayer.position(), so both are wrapped here.
 */
@ConditionalMixin(mods = Mods.VOICECHAT)
@Mixin(ServerPlayerManager.class)
public class ServerPlayerManagerMixin {
    @WrapOperation(method = "getPlayersInRangeInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 railways$useConductorSpyPosition(ServerPlayer instance, Operation<Vec3> original) {
        return railways$spyPosition(instance, original);
    }

    @WrapOperation(method = "getPlayersInRangeDirect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 railways$useConductorSpyPositionDirect(ServerPlayer instance, Operation<Vec3> original) {
        return railways$spyPosition(instance, original);
    }

    @Unique
    private static Vec3 railways$spyPosition(ServerPlayer instance, Operation<Vec3> original) {
        if (ConductorPossessionController.isPossessingConductor(instance)) {
            return instance.getCamera().position();
        }
        return original.call(instance);
    }
}
