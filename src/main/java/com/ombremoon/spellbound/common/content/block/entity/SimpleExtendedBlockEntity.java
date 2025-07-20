package com.ombremoon.spellbound.common.content.block.entity;

import com.ombremoon.spellbound.common.init.SBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleExtendedBlockEntity extends ExtendedBlockEntity {
    public SimpleExtendedBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public SimpleExtendedBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(SBBlockEntities.SIMPLE_MULTIBLOCK.get(), pPos, pBlockState);
    }

}
