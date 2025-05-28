package com.ombremoon.spellbound.common.content.world.multiblock;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.IntStream;

public record MultiblockIndex(int x, int y, int z) implements Comparable<MultiblockIndex> {
    public static final MultiblockIndex ORIGIN = new MultiblockIndex(0, 0, 0);
    public static final Codec<MultiblockIndex> CODEC = Codec.INT_STREAM
            .comapFlatMap(
                    intStream -> Util.fixedSize(intStream, 3).map(array -> new MultiblockIndex(array[0], array[1], array[2])),
                    index -> IntStream.of(index.x(), index.y(), index.z())
            )
            .stable();

    public static MultiblockIndex of(int x, int y, int z) {
        return new MultiblockIndex(x, y, z);
    }

    public BlockPos toPos(Direction facing, BlockPos reference) {
        return reference.relative(facing.getClockWise(), x).relative(Direction.UP, y).relative(facing, z);
    }

    @Override
    public int compareTo(@NotNull MultiblockIndex o) {
        if (this.x < o.x || this.y < o.y || this.z < o.z) {
            return -1;
        } else if (this.x == o.x && this.y == o.y && this.z == o.z) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", this.x()).add("y", this.y()).add("z", this.z()).toString();

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof MultiblockIndex pos)) {
            return false;
        } else if (pos.x != this.x) {
            return false;
        } else {
            return pos.y == this.y && pos.z == this.z;
        }
    }

    @Override
    public int hashCode() {
        int i = Integer.hashCode(this.x);
        i = 31 * i + Integer.hashCode(this.y);
        return 31 * i + Integer.hashCode(this.z);
    }
}
