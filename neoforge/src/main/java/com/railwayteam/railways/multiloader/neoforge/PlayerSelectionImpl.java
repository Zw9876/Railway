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

package com.railwayteam.railways.multiloader.neoforge;

import com.railwayteam.railways.multiloader.PlayerSelection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 1.21 replaced Forge's PacketTarget objects with static PacketDistributor.sendToX methods, so a
 * selection is now a Consumer of the payload rather than a target you hand a packet to. The two
 * "...With(predicate)" selections have no distributor equivalent and iterate explicitly, which is
 * what the old custom PacketDistributors did anyway.
 */
public class PlayerSelectionImpl extends PlayerSelection {

    private final Consumer<CustomPacketPayload> sender;

    private PlayerSelectionImpl(Consumer<CustomPacketPayload> sender) {
        this.sender = sender;
    }

    /** Used by PacketSetImpl to forward a Create/catnip payload to the same selection. */
    public void send(CustomPacketPayload payload) {
        sender.accept(payload);
    }

    @Override
    public void accept(ResourceLocation id, FriendlyByteBuf buffer) {
        sender.accept(new RailwaysPayloads.S2C(id, RailwaysPayloads.toBytes(buffer)));
    }

    public static PlayerSelection all() {
        return new PlayerSelectionImpl(PacketDistributor::sendToAllPlayers);
    }

    public static PlayerSelection allWith(Predicate<ServerPlayer> condition) {
        return new PlayerSelectionImpl(payload -> {
            for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                if (condition.test(player))
                    PacketDistributor.sendToPlayer(player, payload);
            }
        });
    }

    public static PlayerSelection of(ServerPlayer player) {
        return new PlayerSelectionImpl(payload -> PacketDistributor.sendToPlayer(player, payload));
    }

    public static PlayerSelection tracking(Entity entity) {
        return new PlayerSelectionImpl(payload -> PacketDistributor.sendToPlayersTrackingEntity(entity, payload));
    }

    public static PlayerSelection trackingWith(Entity entity, Predicate<ServerPlayer> condition) {
        // No distributor takes a predicate, so filter the tracking set by hand. Sending to a
        // tracked player individually reaches exactly the same people as the unfiltered call.
        return new PlayerSelectionImpl(payload -> {
            if (!(entity.level() instanceof ServerLevel level))
                return;
            for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(
                    new net.minecraft.world.level.ChunkPos(entity.blockPosition()), false)) {
                if (condition.test(player))
                    PacketDistributor.sendToPlayer(player, payload);
            }
        });
    }

    public static PlayerSelection tracking(BlockEntity be) {
        return tracking((ServerLevel) be.getLevel(), be.getBlockPos());
    }

    public static PlayerSelection tracking(ServerLevel level, BlockPos pos) {
        return new PlayerSelectionImpl(payload -> PacketDistributor.sendToPlayersTrackingChunk(
                level, new net.minecraft.world.level.ChunkPos(pos), payload));
    }

    public static PlayerSelection trackingAndSelf(ServerPlayer player) {
        return new PlayerSelectionImpl(payload -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, payload));
    }
}
