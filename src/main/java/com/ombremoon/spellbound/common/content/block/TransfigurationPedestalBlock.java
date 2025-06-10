package com.ombremoon.spellbound.common.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

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
