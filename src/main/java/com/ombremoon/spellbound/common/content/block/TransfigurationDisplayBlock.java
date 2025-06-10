package com.ombremoon.spellbound.common.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;

public class TransfigurationDisplayBlock extends Block {
    public static final MapCodec<TransfigurationDisplayBlock> CODEC = simpleCodec(TransfigurationDisplayBlock::new);

    public TransfigurationDisplayBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }


}
