package com.ombremoon.spellbound.common.content.world.multiblock;

import com.mojang.serialization.MapCodec;

public interface MultiblockSerializer<T extends Multiblock> {

    MapCodec<T> codec();

}
