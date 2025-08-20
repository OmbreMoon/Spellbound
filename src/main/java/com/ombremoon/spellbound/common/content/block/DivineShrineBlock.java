package com.ombremoon.spellbound.common.content.block;

import com.mojang.datafixers.util.Pair;
import com.ombremoon.spellbound.common.init.SBBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DivineShrineBlock extends Block {
    public DivineShrineBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    public static Pair<BlockPos, BlockState> getNearestShrine(Player player) {
        return getNearestShrine(player.level(), player.getOnPos());
    }

    @Nullable
    public static Pair<BlockPos, BlockState> getNearestShrine(Level level, BlockPos blockPos) {
        for (BlockPos pos : BlockPos.betweenClosed(blockPos.subtract(new Vec3i(7, 7, 7)), blockPos.offset(new Vec3i(7, 7, 7)))) {
            BlockState state = level.getBlockState(pos);

            if (state.is(SBBlocks.DIVINE_SHRINE.get()))
                return Pair.of(pos, state);
        }

        return null;
    }
}
