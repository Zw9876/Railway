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

package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.railwayteam.railways.mixin.AccessorTrainRelocator;
import com.railwayteam.railways.multiloader.S2CPacket;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;

/**
 * Create's TrainPacket used to double as a Train serialiser, and this packet borrowed it.
 * In 1.21 that job belongs to Train.STREAM_CODEC, which needs registry access, so the
 * sending side captures a RegistryAccess and the receiving side takes one from the
 * connection. {@code registries} is only read when writing, so it is null on the client.
 */
public record ShadowTrainRestorePacket(Train train, RegistryAccess registries) implements S2CPacket {
    public ShadowTrainRestorePacket(FriendlyByteBuf buf) {
        this(readTrain(buf), null);
    }

    private static Train readTrain(FriendlyByteBuf buf) {
        RegistryAccess access = Minecraft.getInstance().getConnection().registryAccess();
        return Train.STREAM_CODEC.decode(new RegistryFriendlyByteBuf(buf, access));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        Train.STREAM_CODEC.encode(new RegistryFriendlyByteBuf(buffer, registries), train);
    }

    @Override
    public void handle(Minecraft mc) {
        mc.execute(() -> {
            if (mc.player == null) return;

            AccessorTrainRelocator.railways$setRelocatingTrain(ShadowRealm.MARKER);
            AccessorTrainRelocator.railways$setRelocatingOrigin(BlockPos.containing(mc.player.position()));
            AccessorTrainRelocator.railways$setRelocatingEntityId(-1);
            ShadowRealm.clientShadowRestoringTrain = train;
        });
    }
}
