package com.ombremoon.spellbound.common.content.block;

import com.mojang.serialization.MapCodec;
import com.ombremoon.spellbound.common.content.block.entity.SimpleMultiBlockEntity;
import com.ombremoon.spellbound.common.init.SBStats;
import com.ombremoon.spellbound.networking.PayloadHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.stream.Stream;

public class UnnamedWorkbenchBlock extends AbstractMultiBlock {
    public static final MapCodec<UnnamedWorkbenchBlock> CODEC = simpleCodec(UnnamedWorkbenchBlock::new);
    public static final EnumProperty<WorkbenchPart> PART = EnumProperty.create("workbench", WorkbenchPart.class);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public UnnamedWorkbenchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(PART, WorkbenchPart.LEFT).setValue(AbstractMultiBlock.CENTER, false));
    }

    @Override
    public Stream<BlockPos> fullBlockShape(@Nullable Direction direction, BlockPos center) {
        return BlockPos.betweenClosedStream(center, center.relative(direction).above());
    }

    @Override
    public boolean isDirectional() {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SimpleMultiBlockEntity(blockPos, blockState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            PayloadHandler.openWorkbenchScreen(player);
            player.awardStat(SBStats.INTERACT_WITH_BENCH.get());
            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return super.getRenderShape(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, AbstractMultiBlock.CENTER);
    }

    @Override
    public @org.jetbrains.annotations.Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = this.defaultBlockState();

        if (isDirectional()){
            state = state.setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getClockWise());
        }

        return canPlace(level, pos, state) ? state : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            BlockPos blockpos = pos.relative(state.getValue(FACING));
            level.setBlock(blockpos, state.setValue(PART, WorkbenchPart.RIGHT), 3);
            level.setBlock(pos.above(), state.setValue(PART, WorkbenchPart.TOP_LEFT), 3);
            level.setBlock(blockpos.above(), state.setValue(PART, WorkbenchPart.TOP_RIGHT), 3);

            level.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(level, pos, 3);
        }
    }

    public enum WorkbenchPart implements StringRepresentable {
        LEFT("left"),
        RIGHT("right"),
        TOP_LEFT("top_left"),
        TOP_RIGHT("top_right");

        private final String name;

        WorkbenchPart(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
       return switch (state.getValue(UnnamedWorkbenchBlock.FACING)){
           case DOWN, UP -> Shapes.block();
           case NORTH -> switch (state.getValue(UnnamedWorkbenchBlock.PART)){
               case LEFT -> makeShapeWest();
               case RIGHT -> makeShapeWest().move(0,0,1);
               case TOP_LEFT -> makeShapeWest().move(0,-1,0);
               case TOP_RIGHT -> makeShapeWest().move(0,-1,1);
           };
           case SOUTH -> switch (state.getValue(UnnamedWorkbenchBlock.PART)){
               case LEFT -> makeShapeEast();
               case RIGHT -> makeShapeEast().move(0,0,-1);
               case TOP_LEFT -> makeShapeEast().move(0,-1,0);
               case TOP_RIGHT -> makeShapeEast().move(0,-1,-1);
           };
           case WEST -> switch (state.getValue(UnnamedWorkbenchBlock.PART)){
               case LEFT -> makeShapeNorth();
               case RIGHT -> makeShapeNorth().move(1,0,0);
               case TOP_LEFT -> makeShapeNorth().move(0,-1,0);
               case TOP_RIGHT -> makeShapeNorth().move(1,-1,0);
           };
           case EAST -> switch (state.getValue(UnnamedWorkbenchBlock.PART)){
               case LEFT -> makeShapeSouth();
               case RIGHT -> makeShapeSouth().move(-1,0,0);
               case TOP_LEFT -> makeShapeSouth().move(0,-1,0);
               case TOP_RIGHT -> makeShapeSouth().move(-1,-1,0);
           };
       };
    }

    public VoxelShape makeShapeNorth(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(-1, 0, 0, 1, 0.875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1, 0.875, 0.875, 1, 1.75, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.8125, 0, 0, 0.8125, 0.625, 0.8125), BooleanOp.ONLY_FIRST);

        return shape;
    }

    public VoxelShape makeShapeEast(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.875, 2), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.875, 0, 1, 1.75, 2), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.1875, 0.8125, 0.625, 1.8125), BooleanOp.ONLY_FIRST);

        return shape;
    }

    public VoxelShape makeShapeSouth(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 2, 0.875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.875, 0, 2, 1.75, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0, 0.1875, 1.8125, 0.625, 1), BooleanOp.ONLY_FIRST);

        return shape;
    }


    public VoxelShape makeShapeWest(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, -1, 1, 0.875, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.875, -1, 0.125, 1.75, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0, -0.8125, 1, 0.625, 0.8125), BooleanOp.ONLY_FIRST);

        return shape;
    }

}
