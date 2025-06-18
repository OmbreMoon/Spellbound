package com.ombremoon.spellbound.common.content.block.entity;

import com.ombremoon.spellbound.common.init.SBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleMultiBlockEntity extends MultiBlockEntity{
    public SimpleMultiBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    public SimpleMultiBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(SBBlockEntities.SIMPLE_MULTIBLOCK.get(), pPos, pBlockState);
    }

}
