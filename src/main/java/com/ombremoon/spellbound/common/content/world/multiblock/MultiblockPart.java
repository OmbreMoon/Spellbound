package com.ombremoon.spellbound.common.content.world.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public abstract class MultiblockPart extends BlockEntity {
    private Multiblock multiblock;
    private MultiblockIndex index;
    private Direction facing;

    public MultiblockPart(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public Multiblock getMultiblock() {
        return this.multiblock;
    }

    public MultiblockIndex getIndex() {
        return this.index;
    }

    public void setIndex(Multiblock multiblock, MultiblockIndex index, Direction facing) {
        this.multiblock = multiblock;
        this.index = index;
        this.facing = facing;
    }

    public Direction getPartDirection() {
        return this.facing;
    }

    public boolean isAssigned() {
        return this.multiblock != null;
    }
}
