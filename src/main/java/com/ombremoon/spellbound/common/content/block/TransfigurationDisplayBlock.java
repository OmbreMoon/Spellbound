package com.ombremoon.spellbound.common.content.block;

import com.mojang.serialization.MapCodec;
import com.ombremoon.spellbound.common.content.block.entity.TransfigurationDisplayBlockEntity;
import com.ombremoon.spellbound.common.init.SBBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TransfigurationDisplayBlock extends BaseEntityBlock {
    public static final MapCodec<TransfigurationDisplayBlock> CODEC = simpleCodec(TransfigurationDisplayBlock::new);

    public TransfigurationDisplayBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof TransfigurationDisplayBlockEntity transfigurationDisplay && transfigurationDisplay.currentItem != null) {
            player.addItem(transfigurationDisplay.currentItem);
            transfigurationDisplay.setItem(null);
            level.sendBlockUpdated(pos, state, state, 3);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof TransfigurationDisplayBlockEntity transfigurationDisplay && transfigurationDisplay.currentItem == null) {
            transfigurationDisplay.setItem(stack.copyWithCount(1));
            level.sendBlockUpdated(pos, state, state, 3);
            stack.shrink(1);
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransfigurationDisplayBlockEntity(pos, state);
    }

    @javax.annotation.Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? createTickerHelper(blockEntityType, SBBlockEntities.TRANSFIGURATION_DISPLAY.get(), TransfigurationDisplayBlockEntity::itemAnimationTick) : createTickerHelper(blockEntityType, SBBlockEntities.TRANSFIGURATION_DISPLAY.get(), TransfigurationDisplayBlockEntity::serverTick);
    }
}
