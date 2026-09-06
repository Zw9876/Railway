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


package com.railwayteam.railways.mixin;

import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Carries the handcar flag over the network.
 * <p>
 * Up to 1.20 this was done by appending a boolean to Create's TrainPacket. In 1.21 that
 * packet was split into AddTrainPacket/RemoveTrainPacket, and the train itself is
 * serialised by Train.STREAM_CODEC, so the flag is appended to the codec instead. That is
 * also the better place for it: every consumer of Train.STREAM_CODEC now carries the flag,
 * including AddTrainPacket and our own ShadowTrainRestorePacket.
 * <p>
 * Replacing the field from inside Train's own static initialiser is safe: the JLS
 * guarantees a class is fully initialised before any read of its static fields returns, so
 * AddTrainPacket.STREAM_CODEC (which is Train.STREAM_CODEC.map(...)) and every other reader
 * necessarily observes the wrapper.
 */
@Mixin(value = Train.class, remap = false)
public class MixinTrainSync {
    @Shadow @Final @Mutable
    public static StreamCodec<RegistryFriendlyByteBuf, Train> STREAM_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void railways$appendHandcarFlag(CallbackInfo ci) {
        StreamCodec<RegistryFriendlyByteBuf, Train> original = STREAM_CODEC;
        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public Train decode(RegistryFriendlyByteBuf buffer) {
                Train train = original.decode(buffer);
                ((IHandcarTrain) train).railways$setHandcar(buffer.readBoolean());
                return train;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buffer, Train train) {
                original.encode(buffer, train);
                buffer.writeBoolean(((IHandcarTrain) train).railways$isHandcar());
            }
        };
    }
}
