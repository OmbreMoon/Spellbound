package com.ombremoon.spellbound.common.content.world.multiblock;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public abstract class Multiblock {
    private final Map<MultiblockIndex, Predicate<BlockState>> indices;
    private final int width;
    private final int height;
    private final int depth;

    protected Multiblock(MultiblockInfo info) {
        this.indices = info.indices;
        this.width = info.width;
        this.height = info.height;
        this.depth = info.depth;
    }

    public abstract MultiblockIndexProperty getType();

    public abstract MultiblockIndex getActiveIndex();

    public void onActivate(Player player, Level level, MultiblockPattern pattern) {

    }

    public void onRemoved(Player player, Level level, MultiblockPattern pattern) {

    }

    public BlockPos locateOrigin(MultiblockIndex index, BlockPos blockPos, Direction facing) {
        return blockPos.relative(facing.getCounterClockWise(), index.x()).relative(Direction.DOWN, index.y()).relative(facing.getOpposite(), index.z());
    }

    public MultiblockIndex getFinalIndex() {
        return new MultiblockIndex(this.getWidth() - 1, this.getHeight() - 1, this.getDepth() - 1);
    }

    public MultiblockPattern findPattern(LevelAccessor level, BlockPos blockPos, Direction facing) {
        return this.findPattern(level, blockPos, facing, this.getActiveIndex());
    }

    public MultiblockPattern findPattern(LevelAccessor level, BlockPos blockPos, Direction facing, MultiblockIndex index) {
        return this.findPattern(level, blockPos, facing, Direction.UP, index);
    }

    private MultiblockPattern findPattern(LevelAccessor level, BlockPos blockPos, Direction facing, Direction up, MultiblockIndex index) {
        BlockPos origin = this.locateOrigin(index, blockPos, facing);
        LoadingCache<BlockPos, BlockState> cache = createLevelCache(level);
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                for (int k = 0; k < this.depth; k++) {
                    MultiblockIndex currentIndex = new MultiblockIndex(i, j, k);
                    BlockPos currentPos = currentIndex.toPos(facing, origin);
                    BlockState testState = level.getBlockState(currentPos);
                    if (!this.indices.get(currentIndex).test(testState))
                        return null;
                }
            }
        }


        Constants.LOG.debug("Found multiblock pattern for {} from {} to {}", this, origin, this.getFinalIndex().toPos(facing, origin));
        return new MultiblockPattern(level, origin, cache, facing, up, this.width, this.height, this.depth);
    }

    public void clearMultiblock(Player player, Level level, MultiblockPattern pattern) {
        this.onRemoved(player, level, pattern);
        this.clearMultiblock(level, pattern.frontBottomLeft, pattern.facing);
    }

    private void clearMultiblock(LevelAccessor level, BlockPos blockPos, Direction facing) {
        for (int i = 0; i < this.width; i++) {
            for (int j = 0; j < this.height; j++) {
                for (int k = 0; k < this.depth; k++) {
                    MultiblockIndex currentIndex = new MultiblockIndex(i, j, k);
                    BlockPos currentPos = currentIndex.toPos(facing, blockPos);
                    BlockState testState = level.getBlockState(currentPos);
                    if (testState.getBlock() instanceof MultiblockParts<?> part && part.getMultiblock() == this && part.isAssigned(testState))
                        part.setIndex(level, currentPos, MultiblockIndex.ORIGIN, facing, false);
                }
            }
        }

        Constants.LOG.debug("Successfully removed multiblock {} from {} to {}", this, blockPos, this.getFinalIndex().toPos(facing, blockPos));
    }

    public static LoadingCache<BlockPos, BlockState> createLevelCache(LevelReader level) {
        return CacheBuilder.newBuilder().build(new MultiblockCache(level));
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getDepth() {
        return this.depth;
    }
/*
    @Override
    public String toString() {
        return ODFMultiblocks.MULTIBLOCK_REGISTRY.getKey(this).toString();
    }*/

    static class MultiblockCache extends CacheLoader<BlockPos, BlockState> {
        private final LevelReader level;

        public MultiblockCache(LevelReader level) {
            this.level = level;
        }

        @Override
        public @NotNull BlockState load(@NotNull BlockPos key) {
            return this.level.getBlockState(key);
        }
    }

    public static class MultiblockBuilder {
        List<String[]> pattern = Lists.newArrayList();
        private final Map<Character, Predicate<BlockState>> key = Maps.newHashMap();
        private int width;
        private int depth;

        private MultiblockBuilder() {
            this.key.put(' ', blockInWorld -> true);
        }

        public MultiblockBuilder pattern(String... pattern) {
            if (!ArrayUtils.isEmpty(pattern) && !StringUtils.isEmpty(pattern[0])) {
                if (this.pattern.isEmpty()) {
                    this.width = pattern[0].length();
                    this.depth = pattern.length;
                }

                if (pattern.length != this.depth) {
                    throw new IllegalArgumentException(
                            "Expected pattern with depth of " + this.depth + ", but was given one with a depth of " + pattern.length + ")"
                    );
                } else {
                    for (String s : pattern) {
                        if (s.length() != this.width) {
                            throw new IllegalArgumentException(
                                    "Not all rows in the given pattern are the correct width (expected " + this.width + ", found one with " + s.length() + ")"
                            );
                        }

                        for (char c0 : s.toCharArray()) {
                            if (!this.key.containsKey(c0)) {
                                this.key.put(c0, null);
                            }
                        }
                    }

                    this.pattern.add(pattern);
                    return this;
                }
            } else {
                throw new IllegalArgumentException("Cannot build multiblock with empty pattern");
            }
        }

        public static MultiblockBuilder of() {
            return new MultiblockBuilder();
        }

        public MultiblockBuilder key(char symbol, Predicate<BlockState> condition) {
            this.key.put(symbol, condition);
            return this;
        }

        public MultiblockInfo build() {
            return new MultiblockInfo(this.buildPattern(), this.width, this.pattern.size(), this.depth);
        }

        private Map<MultiblockIndex, Predicate<BlockState>> buildPattern() {
            this.ensureAllCharactersMatched();
            Map<MultiblockIndex, Predicate<BlockState>> map = Maps.newHashMap();
            Predicate<BlockState> predicate;

            for (int i = 0; i < this.width; i++) {
                for (int j = 0; j < this.pattern.size(); j++) {
                    for (int k = 0; k < this.depth; k++) {
                        MultiblockIndex index = new MultiblockIndex(i, j, k);
                        predicate = this.key.get(this.pattern.get(j)[k].charAt(i));
                        map.put(index, predicate);
                    }
                }
            }

            return map;
        }

        private void ensureAllCharactersMatched() {
            List<Character> list = Lists.newArrayList();

            for (Map.Entry<Character, Predicate<BlockState>> entry : this.key.entrySet()) {
                if (entry.getValue() == null) {
                    list.add(entry.getKey());
                }
            }

            if (!list.isEmpty()) {
                throw new IllegalStateException("Predicates for character(s) " + Joiner.on(",").join(list) + " are missing");
            }
        }
    }

    public record MultiblockInfo(Map<MultiblockIndex, Predicate<BlockState>> indices, int width, int height,
                                    int depth) {
    }

    public record MultiblockPattern(LevelAccessor level, BlockPos frontBottomLeft, LoadingCache<BlockPos, BlockState> cache, Direction facing, Direction up, int width, int height,
                                    int depth) {

        public BlockState getBlock(MultiblockIndex index) {
            return this.getBlock(index.x(), index.y(), index.z());
        }

        public BlockState getBlock(int xOffset, int yOffset, int zOffset) {
            return this.cache.getUnchecked(getIndexPos(xOffset, yOffset, zOffset));
        }

        public BlockPos getIndexPos(MultiblockIndex index) {
            return this.frontBottomLeft.relative(facing.getClockWise(), index.x()).relative(up, index.y()).relative(facing, index.z());
        }

        public BlockPos getIndexPos(int xOffset, int yOffset, int zOffset) {
            return this.frontBottomLeft.relative(facing.getClockWise(), xOffset).relative(up, yOffset).relative(facing, zOffset);
        }

        void assignMultiblock() {
            this.forEachBlock((blockState, index) -> {
                if (blockState.getBlock() instanceof MultiblockParts<?> part)
                    part.setIndex(level, getIndexPos(index), index, facing, true);
            });
        }

        public void forEachBlock(BiConsumer<BlockState, MultiblockIndex> consumer) {
            for (int i = 0; i < this.width; i++) {
                for (int j = 0; j < this.height; j++) {
                    for (int k = 0; k < this.depth; k++) {
                        consumer.accept(getBlock(i, j, k), new MultiblockIndex(i, j, k));
                    }
                }
            }
        }
    }
}
