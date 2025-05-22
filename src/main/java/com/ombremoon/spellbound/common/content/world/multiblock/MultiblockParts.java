package com.ombremoon.spellbound.common.content.world.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public interface MultiblockParts<T extends Multiblock> {
    BooleanProperty ASSIGNED = BooleanProperty.create("assigned");
    EnumProperty<Direction> MULTIBLOCK_FACING = EnumProperty.create("multiblock_facing", Direction.class);

    T getMultiblock();

    default MultiblockIndex getIndex(BlockState state) {
        return state.getValue(getMultiblock().getType());
    }

    default void setIndex(LevelAccessor level, BlockPos blockPos, MultiblockIndex index, Direction facing, boolean assign) {
        BlockState blockState = level.getBlockState(blockPos);
        level.setBlock(blockPos, blockState.setValue(ASSIGNED, assign).setValue(getMultiblock().getType(), index).setValue(MULTIBLOCK_FACING, facing), 3);
    }

    default Direction getPartDirection(BlockState state) {
        return state.getValue(MULTIBLOCK_FACING);
    }

    default boolean isAssigned(BlockState state) {
        return state.getValue(ASSIGNED);
    }

    default boolean tryCreateMultiblock(Level level, Player player, BlockPos blockPos, Direction facing) {
        Multiblock multiblock = getMultiblock();
        Multiblock.MultiblockPattern pattern = multiblock.findPattern(level, blockPos, facing);
        if (pattern != null) {
            pattern.assignMultiblock();
            multiblock.onActivate(player, level, pattern);
            return true;
        }
        return false;
    }
}
