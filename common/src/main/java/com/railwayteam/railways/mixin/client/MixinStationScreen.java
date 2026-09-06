/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

import net.minecraft.client.gui.components.Tooltip;
import com.google.common.collect.ImmutableList;
import com.railwayteam.railways.mixin_interfaces.ILimited;
import com.railwayteam.railways.registry.CRPackets;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainIconType;
import com.simibubi.create.content.trains.station.AbstractStationScreen;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.content.trains.station.StationScreen;
import com.simibubi.create.content.trains.station.TrainEditPacket;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = StationScreen.class, remap = false)
public abstract class MixinStationScreen extends AbstractStationScreen {
    @Shadow private EditBox trainNameBox;
    private Checkbox limitEnableCheckbox;
    private List<ResourceLocation> iconTypes;
    private ScrollInput iconTypeScroll;

    private MixinStationScreen(StationBlockEntity te, GlobalStation station) {
        super(te, station);
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/trains/station/StationScreen;tickTrainDisplay()V"), remap = true)
    private void initCheckbox(CallbackInfo ci) {
        int x = guiLeft;
        int y = guiTop;
        limitEnableCheckbox = Checkbox.builder(Component.translatable("railways.station.train_limit"), font)
                .pos(x + background.getWidth() - 98, y + background.getHeight() - 26)
                .selected(station != null && ((ILimited) station).isLimitEnabled())
                .onValueChange((checkbox, selected) ->
                        CRPackets.PACKETS.send(ILimited.makeLimitEnabledPacket(blockEntity.getBlockPos(), selected)))
                // 1.21 made Checkbox's constructor package-private and gave it a builder, so the
                // old anonymous subclass is gone: onPress becomes onValueChange, and the
                // hand-rolled two-line hover tooltip becomes a real Tooltip. The builder sizes the
                // widget from its text rather than the previous fixed 50x20.
                .tooltip(Tooltip.create(Component.translatable("railways.station.train_limit.tooltip.1")
                        .append("\n")
                        .append(Component.translatable("railways.station.train_limit.tooltip.2"))))
                .build();
        addRenderableWidget(limitEnableCheckbox);

        iconTypes = TrainIconType.REGISTRY.keySet()
                .stream()
                .toList();
        iconTypeScroll = new ScrollInput(x + 4, y + 17, 184, 14).titled(CreateLang.translateDirect("station.icon_type"));
        iconTypeScroll.withRange(0, iconTypes.size());
        iconTypeScroll.withStepFunction(ctx -> -iconTypeScroll.standardStep()
                .apply(ctx));
        iconTypeScroll.calling(s -> {
            Train train = displayedTrain.get();
            if (train != null) {
                train.icon = TrainIconType.byId(iconTypes.get(s));
                CRPackets.PACKETS.send(new TrainEditPacket.Serverbound(train.id, trainNameBox.getValue(), train.icon.getId(), train.mapColorIndex));
            }
        });
        iconTypeScroll.active = false;
    }

    @Inject(method = "tickTrainDisplay", at = @At("HEAD"))
    private void tickIconScroll(CallbackInfo ci) {
        Train train = displayedTrain.get();

        if (train == null) {
            if (iconTypeScroll.active) {
                iconTypeScroll.active = false;
                removeWidget(iconTypeScroll);
            }

            Train imminentTrain = getImminent();

            if (imminentTrain != null) {
                iconTypeScroll.active = true;
                iconTypeScroll.setState(iconTypes.indexOf(imminentTrain.icon.getId()));
                addRenderableWidget(iconTypeScroll);
            }
        }
    }
}
