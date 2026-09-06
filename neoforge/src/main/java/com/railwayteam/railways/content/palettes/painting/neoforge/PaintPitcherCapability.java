/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

package com.railwayteam.railways.content.palettes.painting.neoforge;

import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintFluid;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.railwayteam.railways.content.palettes.painting.PitcherColor;
import com.railwayteam.railways.registry.CRFluids;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaintPitcherCapability implements IFluidHandlerItem {
    private ItemStack container;

    public PaintPitcherCapability(ItemStack container) {
        this.container = container;
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return container;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    private static FluidStack makeFluidStack(PitcherColor color, int amount) {
        if (color.isSandyWater())
            return new FluidStack(Fluids.WATER, amount);

        // 1.21 FluidStack uses data components like ItemStack, so the colour rides in
        // CUSTOM_DATA and PaintFluid's CompoundTag helpers keep working unchanged.
        FluidStack fluidStack = new FluidStack(CRFluids.PAINT.get().getSource(), amount);
        fluidStack.set(DataComponents.CUSTOM_DATA,
            CustomData.of(PaintFluid.setColor(new CompoundTag(), color.color())));
        return fluidStack;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (container.getItem() instanceof PaintPitcherItem item) {
            return makeFluidStack(new PitcherColor(item.getColor()), (int) item.getFluidAmount(container));
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return (int) (PaintPitcherItem.FLUID_PER_LEVEL * PaintPitcherItem.MAX_LEVELS);
    }

    @Nullable
    private PitcherColor getColor() {
        if (container.getItem() instanceof PaintPitcherItem item) {
            return new PitcherColor(item.getColor());
        }
        return null;
    }

    private int getLevels() {
        if (!(container.getItem() instanceof PaintPitcherItem item)) return 0;
        return item.getLevels(container);
    }

    private static @Nullable PitcherColor getFluidStackColor(@NotNull FluidStack stack) {
        if (Fluids.WATER.isSame(stack.getFluid()))
            return PitcherColor.SANDY_WATER;

        if (!CRFluids.PAINT.get().isSame(stack.getFluid()))
            return null;

        PalettesColor fluidColor = PaintFluid.getColor(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()).orElse(null);
        if (fluidColor == null)
            return null;

        return new PitcherColor(fluidColor);
    }

    @Nullable
    private PitcherColor getColorIfValid(@NotNull FluidStack stack) {
        PitcherColor color = getColor();
        PitcherColor fluidColor = getFluidStackColor(stack);
        if (fluidColor == null) return null;

        // Color mismatches can never be inserted or extracted
        if (color != null && !color.equals(fluidColor)) {
            return null;
        }

        return color == null ? fluidColor : color;
    }

    private ItemStack makeFilledStack(PitcherColor color, int levels) {
        return color.getItemEntry().get().copyAsFilledStack(container, levels);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return getColorIfValid(stack) != null;
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        PitcherColor color = getColorIfValid(stack);
        if (color == null) return 0;

        int currentLevels = getLevels();
        int levelCapacity = PaintPitcherItem.MAX_LEVELS - currentLevels;
        if (levelCapacity <= 0) return 0;
        int filledLevels = (int) Math.min(stack.getAmount() / PaintPitcherItem.FLUID_PER_LEVEL, levelCapacity);
        if (filledLevels <= 0) return 0;

        if (action.execute()) {
            container = makeFilledStack(color, currentLevels + filledLevels);
        }

        return (int) (filledLevels * PaintPitcherItem.FLUID_PER_LEVEL);
    }

    private int drain(PitcherColor color, int maxDrain, FluidAction action) {
        int currentLevels = getLevels();
        int drainedLevels = (int) Math.min(maxDrain / PaintPitcherItem.FLUID_PER_LEVEL, currentLevels);
        if (drainedLevels <= 0) return 0;

        if (action.execute()) {
            container = makeFilledStack(color, currentLevels - drainedLevels);
        }

        return (int) (drainedLevels * PaintPitcherItem.FLUID_PER_LEVEL);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack stack, FluidAction action) {
        PitcherColor color = getColorIfValid(stack);
        if (color == null) return FluidStack.EMPTY;

        int drained = drain(color, stack.getAmount(), action);
        if (drained <= 0) return FluidStack.EMPTY;

        FluidStack drainedStack = stack.copy();
        drainedStack.setAmount(drained);
        return drainedStack;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        PitcherColor color = getColor();
        if (color == null) return FluidStack.EMPTY;

        int drained = drain(color, maxDrain, action);
        if (drained <= 0) return FluidStack.EMPTY;

        return makeFluidStack(color, drained);
    }

}
