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

package com.railwayteam.railways.content.fuel.tank;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.ModelFile;

/**
 * Blockstate datagen for the fuel tank: TOP/BOTTOM pick the vertical section and SHAPE picks the
 * window cutout, each mapping to the matching {@code block/fuel_tank/block_*} partial.
 *
 * <p>This was a Fabric-only class, left commented out on the Forge side because the Fabric
 * subproject emitted the shared blockstate. Dropping Fabric took the generator with it, so the
 * tank fell back to a single-variant stub and every section of a multiblock tank rendered as the
 * same model. Ported here rather than rewritten so the emitted models stay byte-identical to what
 * Fabric produced.
 */
public class FuelTankGenerator extends SpecialBlockStateGen {

    @Override
    protected Property<?>[] getIgnoredProperties() {
        // 1.21's FuelTankBlock no longer declares LIGHT_LEVEL, which was the only ignored property.
        return new Property<?>[0];
    }

    @Override
    protected int getXRotation(BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        return 0;
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                BlockState state) {
        boolean top = state.getValue(FuelTankBlock.TOP);
        boolean bottom = state.getValue(FuelTankBlock.BOTTOM);
        FuelTankBlock.Shape shape = state.getValue(FuelTankBlock.SHAPE);

        String section = top && bottom ? "single" : top ? "top" : bottom ? "bottom" : "middle";
        String modelName = section + (shape == FuelTankBlock.Shape.PLAIN ? "" : "_" + shape.getSerializedName());

        return AssetLookup.partialBaseModel(ctx, prov, modelName);
    }
}
