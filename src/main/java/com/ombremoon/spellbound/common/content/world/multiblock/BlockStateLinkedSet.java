package com.ombremoon.spellbound.common.content.world.multiblock;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;
import java.util.Set;

public class BlockStateLinkedSet {
    public static final Hash.Strategy<? super BlockState> TYPE_AND_TAG = new Hash.Strategy<>() {
        @Override
        public int hashCode(BlockState o) {
            if (o != null) {
                int i = 31 * o.getBlock().hashCode();
                return 31 * i + o.getValues().hashCode();
            } else {
                return 0;
            }
        }

        @Override
        public boolean equals(BlockState a, BlockState b) {
            return a == b
                    || a != null
                    && b != null
                    && a.isEmpty() == b.isEmpty()
                    && Objects.equals(a.getValues(), b.getValues());
        }
    };

    public static Set<BlockState> createTypeAndComponentsSet() {
        return new ObjectLinkedOpenCustomHashSet<>(TYPE_AND_TAG);
    }
}
