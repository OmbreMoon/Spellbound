package com.ombremoon.spellbound.common.content.block;

import com.ombremoon.spellbound.common.content.block.entity.ExtendedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public interface ExtendedBlock {

    /*
   How to use:
   implement fullBlockShape and make it return the whole shape
   directional = true if the fullBlockShape needs directions

   For placement:
   Override setPlacedBy - return place
   Override getStateForPlacement - return getStateForPlacementHelper

   For destroying:
   Override updateShape - return updateShapeHelper
   Override canSurvive - return canSurviveHelper
   Optionally Override extraSurviveRequirements
   Optionally add preventCreativeDrops into playerWillDestroy

   growHelper - for bone meal and tick growth

    */

    Stream<BlockPos> fullBlockShape(@Nullable Direction direction, BlockPos center);
    boolean isDirectional(); // False if the BlockState doesn't have directions

    default Stream<BlockPos> fullBlockShape(BlockPos center, @Nullable BlockState state){
        if (!isDirectional() || state == null) return fullBlockShape(null, center);
        return fullBlockShape(state.getValue(HorizontalDirectionalBlock.FACING), center);
    }


    default void place(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity pPlacer, ItemStack stack){
        if (level.isClientSide()) return;
        fullBlockShape(pos, state).forEach(blockPos -> {
            blockPos = blockPos.immutable();
            int flags = level.isClientSide ? 0 : 3;
            level.setBlock(blockPos, state.setValue(AbstractExtendedBlock.CENTER, pos.equals(blockPos)), flags);
            if(level.getBlockEntity(blockPos) instanceof ExtendedBlockEntity entity) {
                entity.setCenter(pos);
            }
        });
    }

    default BlockState getStateForPlacementHelper(BlockPlaceContext context, Block ts) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = ts.defaultBlockState();

        if (isDirectional()){
            state = state.setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection());
        }

        return canPlace(level, pos, state) ? state : null;
    }

    default boolean canPlace(LevelReader level, BlockPos center, BlockState state) {
        return fullBlockShape(center, state).allMatch(blockPos -> level.getBlockState(blockPos).canBeReplaced() && extraSurviveRequirements(level, blockPos, state));
    }

    default void destroy(BlockPos center, Level level, BlockState state){
        if (level.isClientSide()) return;
        fullBlockShape(center, state).forEach(pos ->{
            BlockState blockState = level.getBlockState(pos);
            Block block = state.getBlock();
            if (blockState.is(block)) {
                level.destroyBlock(pos, true);
            }
        });
    }

    default boolean allBlocksPresent(LevelReader level, BlockPos pos, BlockState state, Block originalBlock){
        if (level.isClientSide()) return true;
        BlockPos center = getCenter(level, pos);

        boolean ret;
        ret = fullBlockShape(center, state).allMatch(blockPos -> level.getBlockState(blockPos).is(originalBlock));

        if (ret && level.getBlockEntity(pos) instanceof ExtendedBlockEntity entity && !entity.isPlaced) {
            fullBlockShape(center, state).forEach(blockPos -> ExtendedBlockEntity.setPlaced(level, blockPos));
        }

        return ret;
    }

    default BlockState updateShapeHelper(BlockState state, LevelAccessor level, BlockPos pos){
        if (level.getBlockEntity(pos) instanceof ExtendedBlockEntity entity){
            boolean canSurvive = state.canSurvive(level, pos);
            if (!canSurvive){
                destroy(entity.getCenter(), (Level) level, state);
                return Blocks.AIR.defaultBlockState();
            }
        }else {
            level.destroyBlock(pos, true);
            return Blocks.AIR.defaultBlockState();
        }

        return state;
    }

    default boolean canSurviveHelper(BlockState state, LevelReader level, BlockPos pos, Block thisBlock){
        if (level.getBlockEntity(pos) instanceof ExtendedBlockEntity entity){
            //survive logic
            boolean extraSurvive = fullBlockShape(entity.getCenter(), state).allMatch(blockPos -> extraSurviveRequirements(level, blockPos, state));
            return (allBlocksPresent(level, pos, state, thisBlock) || !entity.isPlaced) && extraSurvive;
        } else {
            //placement logic
            return canPlace(level, pos, state);
        }
    }

    //Override this one to check for other blocks (like if there ground below etc...)
    //Runs for every single block
    default boolean extraSurviveRequirements(LevelReader level, BlockPos pos, BlockState state){
        return true;
    }

    default void preventCreativeDrops(Player player, Level level, BlockPos pos){
        if (player.isCreative() && level.getBlockEntity(pos) instanceof ExtendedBlockEntity entity) {
            level.destroyBlock(entity.center, false);
        }
    }

    default BlockPos getCenter(LevelReader level, BlockPos pos){
        if (level.getBlockEntity(pos) instanceof ExtendedBlockEntity entity){
            return entity.getCenter();
        }
        return pos;
    }

    default boolean isCenter(LevelReader level, BlockPos pos){
        if (level.getBlockEntity(pos) instanceof ExtendedBlockEntity entity) {
            return entity.getCenter().equals(pos);
        }
        return false;
    }
    default void growHelper(Level level, BlockPos blockPos, BlockState blockState){
        Block block = blockState.getBlock();
        // This was originally using my own cropBlock, it may not work with the vanilla one idk
        if (block instanceof CropBlock cropBlock) {
            if(level.getBlockEntity(blockPos) instanceof ExtendedBlockEntity entity) {
                fullBlockShape(entity.getCenter(), level.getBlockState(blockPos)).forEach(pos -> {
                    if(level.getBlockState(pos).is(block)) {
                        cropBlock.growCrops(level, pos, level.getBlockState(pos));
                    }else {
                        level.destroyBlock(pos, false);
                    }
                });
            } else level.destroyBlock(blockPos, true);
        }
    }

}
