package com.ombremoon.spellbound.common.content.world.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TransfigurationMultiblockPart extends BlockEntity implements MultiblockPart {
    private Multiblock multiblock;
    private MultiblockIndex index;
    private Direction facing;

    public TransfigurationMultiblockPart(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public Multiblock getMultiblock() {
        return this.multiblock;
    }

    @Override
    public MultiblockIndex getIndex() {
        return this.index;
    }

    @Override
    public void setIndex(Multiblock multiblock, MultiblockIndex index, Direction facing) {
        this.multiblock = multiblock;
        this.index = index;
        this.facing = facing;
    }

    @Override
    public Direction getPartDirection() {
        return this.facing;
    }

    @Override
    public boolean isAssigned() {
        return this.multiblock != null;
    }
}
