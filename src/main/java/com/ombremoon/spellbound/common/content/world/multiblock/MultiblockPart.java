package com.ombremoon.spellbound.common.content.world.multiblock;

import net.minecraft.core.Direction;

public interface MultiblockPart {

    Multiblock getMultiblock();

    MultiblockIndex getIndex();

    void setIndex(Multiblock multiblock, MultiblockIndex index, Direction facing);

    Direction getPartDirection();

    boolean isAssigned();
}
