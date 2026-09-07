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

package com.railwayteam.railways.mixin.conductor_possession;

import com.railwayteam.railways.content.conductor.ConductorPossessionController;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes sure chunks near a camera are sent to the player viewing it, and that block updates in
 * those chunks reach them too.
 *
 * <p>1.21 note: this used to need a {@code @Shadow} of {@code viewDistance} plus a hand-rolled loop
 * calling the five-argument {@code updateChunkTracking} from {@code move}, because tracking was
 * driven per chunk. 1.20.2 replaced all of that with {@link net.minecraft.server.level.ChunkTrackingView}:
 * {@code updateChunkTracking} now derives the whole view from {@code player.chunkPosition()} and
 * re-applies it whenever that position or the view distance changes. So pointing that one call at
 * the camera is enough — sending, dropping and re-centring on dismount all follow for free, and
 * {@code isChunkTracked} consults the same view, which is what gets block updates delivered.
 *
 * <p>{@code ServerPlayer.tick} calls {@code move} every tick and {@code setCamera} calls it when
 * possession starts and ends, so the view keeps up with a moving camera.
 *
 * Confirmed compatible with SecurityCraft
 */
@Mixin(value = ChunkMap.class, priority = 1200)
public abstract class ChunkMapMixin {
	@Redirect(method = "updateChunkTracking", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;chunkPosition()Lnet/minecraft/world/level/ChunkPos;"))
	private ChunkPos railways$securitycraft$trackCameraChunks(ServerPlayer player) {
		Entity camera = player.getCamera();
		if (camera != null && camera != player
				&& (ConductorPossessionController.isPossessingConductor(player)
					|| camera.getClass().getName().equals("net.geforcemods.securitycraft.entity.camera.SecurityCamera")))
			return camera.chunkPosition();

		return player.chunkPosition();
	}
}
