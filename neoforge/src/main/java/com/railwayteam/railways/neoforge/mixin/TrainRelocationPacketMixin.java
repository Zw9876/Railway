/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
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

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.railwayteam.railways.content.shadow_realm.ShadowRealm.RestorationTarget;
import com.simibubi.create.content.trains.entity.TrainRelocationPacket;
import com.simibubi.create.content.trains.track.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

// earlier priority to bypass OPAC protections, which freak out about there being no entity associated with the relocation
@Mixin(value = TrainRelocationPacket.class, priority = 500)
public class TrainRelocationPacketMixin {
    @Shadow @Final
    UUID trainId;

    @Shadow @Final
    BlockPos pos;

    @Shadow @Final private BezierTrackPointLocation hoveredBezier;

    @Shadow @Final private boolean direction;

    @Shadow @Final
    Vec3 lookAngle;

    // Create 6 does the work directly in handle(ServerPlayer) - there is no enqueueWork lambda
    // any more, so lambda$handle$3 is gone - and the range test is now
    // Player.canInteractWithBlock/Entity rather than Vec3.closerThan.
    @WrapOperation(method = "handle", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/player/Player;canInteractWithBlock(Lnet/minecraft/core/BlockPos;D)Z"))
    private boolean unrestrictBlockRange(Player instance, BlockPos pos, double distance, Operation<Boolean> original) {
        if (instance.isCreative() && CRConfigs.server().unlimitedCreativeRelocation.get())
            return true;
        return original.call(instance, pos, distance);
    }

    @WrapOperation(method = "handle", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/player/Player;canInteractWithEntity(Lnet/minecraft/world/entity/Entity;D)Z"))
    private boolean unrestrictEntityRange(Player instance, Entity entity, double distance, Operation<Boolean> original) {
        if (instance.isCreative() && CRConfigs.server().unlimitedCreativeRelocation.get())
            return true;
        return original.call(instance, entity, distance);
    }

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
    private void relocateShadowTrain(ServerPlayer sender, CallbackInfo ci) {
        RestorationTarget target = new RestorationTarget(sender.level(), pos, hoveredBezier, direction, lookAngle);
        ShadowRealm.handleTrainRelocationPacket(sender, trainId, target, ci);
    }
}
