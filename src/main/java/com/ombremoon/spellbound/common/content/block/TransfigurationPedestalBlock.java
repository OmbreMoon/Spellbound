package com.ombremoon.spellbound.common.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TransfigurationPedestalBlock extends Block {
    public static final MapCodec<TransfigurationPedestalBlock> CODEC = simpleCodec(TransfigurationPedestalBlock::new);

    public TransfigurationPedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
