package com.ombremoon.spellbound.common.content.block;

import com.mojang.serialization.MapCodec;
import com.ombremoon.spellbound.common.init.StatInit;
import com.ombremoon.spellbound.networking.PayloadHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stat;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class UnnamedWorkbenchBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<UnnamedWorkbenchBlock> CODEC = simpleCodec(UnnamedWorkbenchBlock::new);
    public static final EnumProperty<WorkbenchPart> PART = EnumProperty.create("workbench", WorkbenchPart.class);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    public UnnamedWorkbenchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(PART, WorkbenchPart.LEFT));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            if (state.getValue(PART) != WorkbenchPart.RIGHT) {
                pos = pos.relative(state.getValue(FACING));
                state = level.getBlockState(pos);
                if (!state.is(this)) return InteractionResult.CONSUME;
            }

            PayloadHandler.openWorkbenchScreen(player);
            player.awardStat(StatInit.INTERACT_WITH_BENCH.get());
            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        WorkbenchPart part = state.getValue(PART);
        boolean isAdjacent = facing == getNeighbourDirection(state.getValue(PART), state.getValue(FACING));
        boolean isVertical = (part == WorkbenchPart.LEFT && facing == Direction.UP) || (part == WorkbenchPart.TOP_LEFT && facing == Direction.DOWN);
        if (isAdjacent || isVertical) {
            return facingState.is(this) && facingState.getValue(PART) != state.getValue(PART) ? state : Blocks.AIR.defaultBlockState();
        } else return state;
    }

    private static Direction getNeighbourDirection(WorkbenchPart part, Direction direction) {
        return part == WorkbenchPart.LEFT || part == WorkbenchPart.TOP_LEFT ? direction : direction.getOpposite();
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            WorkbenchPart part = state.getValue(PART);
            if (part == WorkbenchPart.LEFT) {
                BlockPos blockpos = pos.relative(getNeighbourDirection(part, state.getValue(FACING)));
                BlockState blockstate = level.getBlockState(blockpos);
                if (blockstate.is(this) && blockstate.getValue(PART) == WorkbenchPart.RIGHT) {
                    level.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                    level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
                }
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getClockWise();
        BlockPos blockpos = context.getClickedPos();
        BlockPos blockpos1 = blockpos.relative(direction);
        Level level = context.getLevel();
        return level.getBlockState(blockpos1).canBeReplaced(context) && level.getWorldBorder().isWithinBounds(blockpos1)
                ? this.defaultBlockState().setValue(FACING, direction)
                : null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return super.getRenderShape(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
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
}
