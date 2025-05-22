package com.ombremoon.spellbound.common.content.world.multiblock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class MultiblockIndexProperty extends Property<MultiblockIndex> {
    private final ImmutableSet<MultiblockIndex> values;
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    protected MultiblockIndexProperty(String name, int maxX, int maxY, int maxZ) {
        super(name, MultiblockIndex.class);
        if (maxX > 9 || maxX < 1) {
            throw new IllegalArgumentException("Max X value of " + name + " must be between 1 and 9");
        } else if (maxY > 9 || maxY < 1) {
            throw new IllegalArgumentException("Max Y value of " + name + " must be between 1 and 9");
        } else if (maxZ > 9 || maxZ < 1) {
            throw new IllegalArgumentException("Max Z value of " + name + " must be between 1 and 9");
        } else {
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            Set<MultiblockIndex> set = Sets.newHashSet();

            for (int i = 0; i < maxX; i++) {
                for (int j = 0; j < maxY; j++) {
                    for (int k = 0; k < maxZ; k++) {
                        set.add(new MultiblockIndex(i, j, k));
                    }
                }
            }

            this.values = ImmutableSet.copyOf(set);
        }
    }

    @Override
    public Collection<MultiblockIndex> getPossibleValues() {
        return this.values;
    }

    @Override
    public int generateHashCode() {
        return 31 * super.generateHashCode() + this.values.hashCode();
    }

    public static MultiblockIndexProperty create(Holder<Multiblock> multiblock) {
        return create(multiblock.getRegisteredName(), multiblock.value().getWidth(), multiblock.value().getHeight(), multiblock.value().getDepth());
    }

    public static MultiblockIndexProperty create(String name, int maxX, int maxY, int maxZ) {
        return new MultiblockIndexProperty(name, maxX, maxY, maxZ);
    }

    @Override
    public Optional<MultiblockIndex> getValue(String value) {
        int[] array = Arrays.stream(value.split("_"))
                .mapToInt(Integer::parseInt)
                .toArray();
        int i1 = array[0];
        int i2 = array[1];
        int i3 = array[2];
        return i1 > this.maxX || i2 > maxY || i3 > maxZ ? Optional.empty() : Optional.of(new MultiblockIndex(i1, i2, i3));
    }

    @Override
    public String getName(MultiblockIndex value) {
        return value.toString();
    }
}
