package com.ombremoon.spellbound.common.content.world.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record MultiblockIndex(int x, int y, int z) implements Comparable<MultiblockIndex> {
    public static MultiblockIndex ORIGIN = new MultiblockIndex(0, 0, 0);

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
        int[] array = new int[3];
        array[0] = this.x;
        array[1] = this.y;
        array[2] = this.z;
        return Arrays.stream(array)
                .mapToObj(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("")
                .replaceAll(",", "_");
//        return "" + this.x + "" + this.y + "" + this.z;
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
