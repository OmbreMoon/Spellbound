package com.ombremoon.spellbound.common.content.block.entity;

import com.ombremoon.spellbound.common.init.SBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SummonBlockEntity extends BlockEntity {
    protected SummonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public SummonBlockEntity(BlockPos pos, BlockState blockState) {
        this(SBBlockEntities.SUMMON_PORTAL.get(), pos, blockState);
    }



    public boolean shouldRenderFace(Direction face) {
        return face.getAxis() == Direction.Axis.Y;
    }
}
