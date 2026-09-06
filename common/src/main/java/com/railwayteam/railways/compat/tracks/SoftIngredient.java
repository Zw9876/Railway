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

package com.railwayteam.railways.compat.tracks;

import org.jetbrains.annotations.Nullable;
import dev.architectury.injectables.annotations.ExpectPlatform;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.railwayteam.railways.registry.CRIngredientTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Represents a special ingredient for datagen - it references an item that does not necessarily exist.
 * <p>
 * 1.21 made {@link Ingredient} final, so this can no longer extend it. NeoForge's
 * ICustomIngredient is the supported replacement: {@link #toVanilla()} produces an
 * Ingredient suitable for Create's TrackMaterial fields, and
 * {@link Ingredient#getCustomIngredient()} recovers this instance at runtime.
 * <p>
 * NOTE: the generated recipe JSON is no longer a bare {@code {"item": "<id>"}}. It is now a
 * custom ingredient type, so datapacks overriding compat-track recipes need updating.
 */
public class SoftIngredient implements ICustomIngredient {
    public static final MapCodec<SoftIngredient> CODEC = RecordCodecBuilder.mapCodec(i -> i
        .group(ResourceLocation.CODEC.fieldOf("item").forGetter(s -> s.item))
        .apply(i, SoftIngredient::new));

    public final ResourceLocation item;

    public SoftIngredient(ResourceLocation item) {
        this.item = item;
    }

    public static SoftIngredient of(ResourceLocation item) {
        return new SoftIngredient(item);
    }

    /** Convenience for the many sites that need a vanilla Ingredient to hand to Create. */
    public static Ingredient vanillaOf(ResourceLocation item) {
        return of(item).toVanilla();
    }

    /**
     * Reverse of {@link #toVanilla()}.
     * <p>
     * NeoForge patches {@code Ingredient} itself with a {@code getCustomIngredient()} accessor,
     * and unlike most of its additions that patch is not behind an extension interface, so it
     * only exists in the patched Minecraft jar that the platform module compiles against - not
     * in :common. Hence the platform hop.
     *
     * @return the wrapped SoftIngredient, or null if this is any other kind of ingredient
     */
    @ExpectPlatform
    public static @Nullable SoftIngredient unwrap(Ingredient ingredient) {
        throw new AssertionError();
    }

    /**
     * Never matches: the referenced item may not be installed. This mirrors the old
     * behaviour, which passed an empty value stream to the Ingredient constructor.
     */
    @Override
    public boolean test(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull Stream<ItemStack> getItems() {
        return Stream.empty();
    }

    /**
     * False so NeoForge routes matching through {@link #test} rather than deriving it
     * from the (empty) item list, which would make this look like an empty ingredient.
     * The old class overrode isEmpty() to false for the same reason.
     */
    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return CRIngredientTypes.SOFT.get();
    }
}
