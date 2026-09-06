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


package com.railwayteam.railways.util;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;

/**
 * 1.21 split {@code Block#use} into {@code useItemOn}, which returns an {@link ItemInteractionResult},
 * and {@code useWithoutItem}, which still returns an {@link InteractionResult}. Our block entities and
 * Create's {@code IBE#onBlockEntityUse} still speak {@link InteractionResult}, so bridge the two here
 * instead of churning every block entity signature.
 */
public class InteractionUtils {

    /**
     * PASS becomes PASS_TO_DEFAULT_BLOCK_INTERACTION so the item click still falls through to
     * {@code useWithoutItem} and then to normal item placement, matching the pre-1.21 behaviour of
     * returning PASS from {@code use}.
     */
    public static ItemInteractionResult toItemResult(InteractionResult result) {
        return switch (result) {
            case SUCCESS, SUCCESS_NO_ITEM_USED -> ItemInteractionResult.SUCCESS;
            case CONSUME -> ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            case FAIL -> ItemInteractionResult.FAIL;
            case PASS -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }
}
