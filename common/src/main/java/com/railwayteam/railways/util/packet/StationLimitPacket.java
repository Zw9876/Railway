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

import com.railwayteam.railways.mixin_interfaces.ILimited;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.simibubi.create.Create;
import com.simibubi.create.content.trains.graph.TrackGraphLocation;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Toggles a station's "only one train at a time" limit.
 * <p>
 * Up to 1.20 this rode along on Create's StationEditPacket: a mixin added a nullable
 * boolean and injected into its writeSettings/readSettings. Those methods no longer exist -
 * 1.21 serialises BlockEntityConfigurationPacket through a StreamCodec - so the flag now
 * travels in its own packet rather than through codec surgery on Create's.
 */
public record StationLimitPacket(BlockPos pos, boolean limitEnabled) implements C2SPacket {
    public StationLimitPacket(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(limitEnabled);
    }

    @Override
    public void handle(ServerPlayer sender) {
        if (!sender.level().isLoaded(pos))
            return;
        BlockEntity be = sender.level().getBlockEntity(pos);
        if (!(be instanceof StationBlockEntity station))
            return;
        // Matches the reach check Create's own BlockEntityConfigurationPacket applies.
        if (!sender.canInteractWithBlock(pos, 8))
            return;

        GlobalStation globalStation = station.getStation();
        TrackGraphLocation graphLocation = station.edgePoint.determineGraphLocation();
        if (globalStation == null || graphLocation == null)
            return;

        ((ILimited) globalStation).setLimitEnabled(limitEnabled);
        Create.RAILWAYS.sync.pointAdded(graphLocation.graph, globalStation);
        Create.RAILWAYS.markTracksDirty();
    }
}
