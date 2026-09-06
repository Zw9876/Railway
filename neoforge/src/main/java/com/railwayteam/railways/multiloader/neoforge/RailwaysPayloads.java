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

import com.railwayteam.railways.Railways;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Carries {@link com.railwayteam.railways.multiloader.PacketSet}'s raw buffers over 1.21's
 * payload networking.
 * <p>
 * PacketSet predates CustomPacketPayload: it serialises its own varint discriminator plus body
 * into a FriendlyByteBuf and hands that to the platform with a channel id. Rather than rewrite
 * all ~24 packets as individual payload types, one payload per direction carries the channel id
 * and the raw bytes, and dispatch stays where it already lives - PacketSetImpl.HANDLERS.
 */
public final class RailwaysPayloads {
    private RailwaysPayloads() {}

    public record S2C(ResourceLocation channel, byte[] data) implements CustomPacketPayload {
        public static final Type<S2C> TYPE = new Type<>(Railways.asResource("s2c"));
        public static final StreamCodec<RegistryFriendlyByteBuf, S2C> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeResourceLocation(payload.channel);
                buf.writeByteArray(payload.data);
            },
            buf -> new S2C(buf.readResourceLocation(), buf.readByteArray())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record C2S(ResourceLocation channel, byte[] data) implements CustomPacketPayload {
        public static final Type<C2S> TYPE = new Type<>(Railways.asResource("c2s"));
        public static final StreamCodec<RegistryFriendlyByteBuf, C2S> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeResourceLocation(payload.channel);
                buf.writeByteArray(payload.data);
            },
            buf -> new C2S(buf.readResourceLocation(), buf.readByteArray())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Wraps an outgoing PacketSet buffer's bytes, which is all the payload needs to carry. */
    public static byte[] toBytes(FriendlyByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }

    private static FriendlyByteBuf toBuf(byte[] data) {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        // The PacketSet already carries its own version and checks it on join, so this
        // registrar's version string only has to be stable.
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(S2C.TYPE, S2C.CODEC, RailwaysPayloads::handleS2C);
        registrar.playToServer(C2S.TYPE, C2S.CODEC, RailwaysPayloads::handleC2S);
    }

    private static void handleS2C(S2C payload, IPayloadContext context) {
        var set = PacketSetImpl.HANDLERS.get(payload.channel());
        if (set == null) {
            Railways.LOGGER.warn("Received an S2C packet on unregistered channel {}", payload.channel());
            return;
        }
        // PacketSet.handleS2CPacket does its own mc.execute, so no enqueueWork here.
        set.handleS2CPacket(net.minecraft.client.Minecraft.getInstance(), toBuf(payload.data()));
    }

    private static void handleC2S(C2S payload, IPayloadContext context) {
        var set = PacketSetImpl.HANDLERS.get(payload.channel());
        if (set == null) {
            Railways.LOGGER.warn("Received a C2S packet on unregistered channel {}", payload.channel());
            return;
        }
        if (context.player() instanceof ServerPlayer sender) {
            set.handleC2SPacket(sender, toBuf(payload.data()));
        }
    }
}
