package com.ombremoon.spellbound.common.content.block;

import com.google.common.base.Predicates;
import com.mojang.serialization.MapCodec;
import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SBBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class SummonStoneBlock extends Block {
    public static final MapCodec<SummonStoneBlock> CODEC = simpleCodec(SummonStoneBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static BlockPattern portalShape;
    private final ResourceLocation spell;

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public SummonStoneBlock(Properties properties) {
        this(CommonClass.customLocation(""), properties);
    }

    public SummonStoneBlock(ResourceLocation spell, Properties properties) {
        super(properties);
        this.spell = spell;
        this.registerDefaultState(this.getStateDefinition().any().setValue(POWERED, Boolean.FALSE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, Boolean.FALSE);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.REDSTONE)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            } else {
                this.activateStone(state, level, pos, player, hand);
                return ItemInteractionResult.CONSUME;
            }
        }
        return ItemInteractionResult.FAIL;
    }

    public void activateStone(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!state.getValue(POWERED)) {
            Constants.LOG.info("POWERED");
            BlockState blockState = state.setValue(POWERED, Boolean.TRUE);
            level.setBlock(pos, blockState, 3);
            level.updateNeighbourForOutputSignal(pos, SBBlocks.SUMMON_STONE.get());
            player.getItemInHand(hand).shrink(1);
            level.levelEvent(1503, pos, 0);
            BlockPattern.BlockPatternMatch blockPatternMatch = getOrCreatePortalShape().find(level, pos);
            if (blockPatternMatch != null) {
                BlockPos blockPos = blockPatternMatch.getFrontTopLeft().offset(-3, 0, -3);
                BlockPos blockPos1 = blockPatternMatch.getFrontTopLeft().offset(-2, 0, -2);

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        level.setBlock(blockPos.offset(i, 0, j), SBBlocks.SUMMON_PORTAL.get().defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    public static BlockPattern getOrCreatePortalShape() {
        if (portalShape == null) {
            portalShape = BlockPatternBuilder.start()
                    .aisle("?$$$?", "$???$", "$?*?$", "$???$", "?$$$?")
                    .where('?', BlockInWorld.hasState(BlockStatePredicate.ANY))
                    .where('$',
                            BlockInWorld.hasState(
                                    BlockStatePredicate.forBlock(SBBlocks.SUMMON_STONE.get())
                                            .where(POWERED, Predicates.equalTo(true))
                            )
                    )
                    .where('*',
                            BlockInWorld.hasState(
                                    state -> state.getBlock() instanceof SummonStoneBlock block && block.spell != null && state.getValue(POWERED)
                            )
                    )
                    .build();
        }
        return portalShape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}