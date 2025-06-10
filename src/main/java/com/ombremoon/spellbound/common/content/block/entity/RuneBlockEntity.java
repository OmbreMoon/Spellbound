package com.ombremoon.spellbound.common.content.block.entity;

import com.ombremoon.spellbound.common.content.world.multiblock.TransfigurationMultiblockPart;
import com.ombremoon.spellbound.common.init.SBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RuneBlockEntity extends TransfigurationMultiblockPart {

    public RuneBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public RuneBlockEntity(BlockPos pos, BlockState blockState) {
        super(SBBlockEntities.RUNE.get(), pos, blockState);
    }
}
