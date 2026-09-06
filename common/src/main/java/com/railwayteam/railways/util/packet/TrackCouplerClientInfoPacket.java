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

package com.railwayteam.railways.util.packet;

import net.minecraft.core.RegistryAccess;
import com.railwayteam.railways.content.coupling.coupler.TrackCouplerBlockEntity;
import com.railwayteam.railways.multiloader.S2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TrackCouplerClientInfoPacket implements S2CPacket {
    final BlockPos blockPos;
    final TrackCouplerBlockEntity.ClientInfo info;
    /**
     * ClientInfo holds error Components, which 1.21 serialises through
     * Component.Serializer with registry access. Only needed when writing.
     */
    private final RegistryAccess registries;

    public TrackCouplerClientInfoPacket(TrackCouplerBlockEntity te) {
        blockPos = te.getBlockPos();
        info = te.getClientInfo();
        registries = te.getLevel().registryAccess();
    }

    public TrackCouplerClientInfoPacket(FriendlyByteBuf buf) {
        blockPos = buf.readBlockPos();
        RegistryAccess access = Minecraft.getInstance().getConnection().registryAccess();
        info = new TrackCouplerBlockEntity.ClientInfo(access, buf.readNbt());
        registries = null;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeNbt(info.write(registries));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void handle(Minecraft mc) {
        Level level = mc.level;
        if (level != null) {
            BlockEntity te = level.getBlockEntity(blockPos);
            if (te instanceof TrackCouplerBlockEntity couplerTile)
                couplerTile.setClientInfo(info);
        }
    }
}
