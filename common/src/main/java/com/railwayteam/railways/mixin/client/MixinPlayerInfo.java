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


package com.railwayteam.railways.mixin.client;

import com.mojang.authlib.GameProfile;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.annotation.mixin.DevEnvMixin;
import com.railwayteam.railways.util.DevCapeUtils;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * 1.21 note: PlayerInfo no longer holds a mutable map of texture locations, a skin model string,
 * or separate getSkinLocation/getCapeLocation accessors. Everything now comes out of a single
 * {@link PlayerSkin} record from {@code getSkin()}, so both the dev cape and the dev-env skin are
 * applied by rebuilding that record on RETURN.
 */
@Mixin(PlayerInfo.class)
public class MixinPlayerInfo {
    @Shadow @Final private GameProfile profile;

    @Unique private static final ResourceLocation DEV_CAPE = Railways.asResource("textures/misc/dev_cape.png");
    @Unique private static final ResourceLocation DEVENV_SKIN = Railways.asResource("textures/misc/devenv_skin.png");

    // Replaces the skin inside the dev env with the conductor skin, forcing the wide ("steve") model.
    @DevEnvMixin
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void railways$devEnvSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerSkin skin = cir.getReturnValue();
        if (skin == null)
            return;
        cir.setReturnValue(new PlayerSkin(DEVENV_SKIN, skin.textureUrl(), skin.capeTexture(),
                skin.elytraTexture(), PlayerSkin.Model.WIDE, skin.secure()));
    }

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void railways$devCape(CallbackInfoReturnable<PlayerSkin> cir) {
        PlayerSkin skin = cir.getReturnValue();
        if (skin == null)
            return;

        ResourceLocation cape = skin.capeTexture();
        if (DevCapeUtils.INSTANCE.useDevCape(profile.getId()))
            cape = DEV_CAPE;
        else if (Objects.equals(DEV_CAPE, cape)) // the dev opted out again
            cape = null;

        if (!Objects.equals(cape, skin.capeTexture()))
            cir.setReturnValue(new PlayerSkin(skin.texture(), skin.textureUrl(), cape,
                    skin.elytraTexture(), skin.model(), skin.secure()));
    }
}
