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

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.RegistryAccess;
import com.railwayteam.railways.content.minecarts.MinecartJukebox;
import com.railwayteam.railways.multiloader.S2CPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class JukeboxCartPacket implements S2CPacket {
  final int id;
  final ItemStack record;
  /** Only needed when writing; null on a packet decoded from the wire. */
  private final RegistryAccess registries;

  public JukeboxCartPacket(Entity target, ItemStack disc) {
    id = target.getId();
    record = disc;
    registries = target.level().registryAccess();
  }

  public JukeboxCartPacket(FriendlyByteBuf buf) {
    id = buf.readInt();
    // 1.21 removed FriendlyByteBuf.readItem/writeItem: stacks travel through a codec
    // that needs registry access to resolve their data components.
    RegistryAccess access = Minecraft.getInstance().getConnection().registryAccess();
    record = ItemStack.OPTIONAL_STREAM_CODEC.decode(new RegistryFriendlyByteBuf(buf, access));
    registries = null;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeInt(this.id);
    ItemStack.OPTIONAL_STREAM_CODEC.encode(new RegistryFriendlyByteBuf(buffer, registries), this.record);
  }

  @Override
  @Environment(EnvType.CLIENT)
  public void handle(Minecraft mc) {
    Level level = mc.level;
    if (level != null) {
      Entity target = level.getEntity(this.id);
      if (target instanceof MinecartJukebox juke) {
        juke.insertRecord(this.record);
      }
    }
  }
}
