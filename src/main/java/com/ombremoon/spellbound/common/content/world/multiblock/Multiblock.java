package com.ombremoon.spellbound.common.content.world.multiblock;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.main.Constants;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class Multiblock {
    private final Map<MultiblockIndex, BuildingBlock> indices;
    private final int width;
    private final int height;
    private final int depth;

    protected Multiblock(MultiblockInfo info) {
        this.indices = info.indices;
        this.width = info.width;
        this.height = info.height;
        this.depth = info.depth;
    }

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
        return new MultiblockPattern(this, origin, cache, facing, up);
    }

    public boolean tryCreateMultiblock(Level level, Player player, BlockPos blockPos, Direction facing) {
        Multiblock.MultiblockPattern pattern = this.findPattern(level, blockPos, facing);
        if (pattern != null) {
            pattern.assignMultiblock(level, blockPos);
            this.onActivate(player, level, pattern);
            return true;
        }
        return false;
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
                    BlockEntity blockEntity = level.getBlockEntity(currentPos);
                    if (blockEntity instanceof MultiblockPart part && part.getMultiblock() == this && part.isAssigned())
                        part.setIndex(null, MultiblockIndex.ORIGIN, facing);
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
        private final Map<Character, BuildingBlock> key = Maps.newHashMap();
        private int width;
        private int depth;
        private Optional<MultiblockPattern.Data> data;

        private MultiblockBuilder() {
            this.key.put(' ', BuildingBlock.EMPTY);
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

        public MultiblockBuilder key(char symbol, BuildingBlock block) {
            this.key.put(symbol, block);
            return this;
        }

        public MultiblockInfo build() {
            return MultiblockInfo.of(this.key, this.pattern);
        }
    }

    public record MultiblockInfo(Map<MultiblockIndex, BuildingBlock> indices, int width, int height,
                                 int depth, Optional<MultiblockPattern.Data> data) {

        public static MultiblockInfo of(Map<Character, BuildingBlock> key, List<String[]> pattern) {
            MultiblockPattern.Data data = new MultiblockPattern.Data(key, pattern);
            return unpack(data).getOrThrow();
        }

        private static DataResult<MultiblockInfo> unpack(MultiblockPattern.Data data) {
            List<String[]> pattern = data.pattern;
            int i = pattern.size();
            int j = pattern.getFirst().length;
            CharSet charSet = new CharArraySet(data.key.keySet());

            for (int i1 = 0; i1 < i; i1++) {
                for (int j1 = 0; j1 < j; j1++) {
                    String s = pattern.getFirst()[i1];

                    for (int k = 0; k < s.length(); k++) {
                        char c0 = s.charAt(k);
                        BuildingBlock block = c0 == ' ' ? BuildingBlock.EMPTY : data.key.get(c0);
                        if (block == null) {
                            return DataResult.error(() -> "Pattern references symbol '" + c0 + "' but it's not defined in the key");
                        }

                        charSet.remove(c0);
                    }
                }
            }

            return !charSet.isEmpty()
                    ? DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + charSet)
                    : DataResult.success(new MultiblockInfo(buildPattern(data.key, data.pattern), i, pattern.size(), j, Optional.of(data)));
        }

        private static Map<MultiblockIndex, BuildingBlock> buildPattern(MultiblockBuilder builder) {
            return buildPattern(builder.key, builder.pattern);
        }

        private static Map<MultiblockIndex, BuildingBlock> buildPattern(Map<Character, BuildingBlock> key, List<String[]> pattern) {
            ensureAllCharactersMatched(key);
            Map<MultiblockIndex, BuildingBlock> map = Maps.newHashMap();
            BuildingBlock buildingBlock;
            int width = pattern.getFirst()[0].length();
            int depth = pattern.getFirst().length;

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < pattern.size(); j++) {
                    for (int k = 0; k < depth; k++) {
                        MultiblockIndex index = new MultiblockIndex(i, j, k);
                        buildingBlock = key.get(pattern.get(j)[k].charAt(i));
                        map.put(index, buildingBlock);
                    }
                }
            }

            return map;
        }

        private static void ensureAllCharactersMatched(Map<Character, BuildingBlock> key) {
            List<Character> list = Lists.newArrayList();

            for (Map.Entry<Character, BuildingBlock> entry : key.entrySet()) {
                if (entry.getValue() == null) {
                    list.add(entry.getKey());
                }
            }

            if (!list.isEmpty()) {
                throw new IllegalStateException("Predicates for character(s) " + Joiner.on(",").join(list) + " are missing");
            }
        }
    }

    public record MultiblockPattern(Multiblock multiblock, BlockPos frontBottomLeft, LoadingCache<BlockPos, BlockState> cache, Direction facing, Direction up) {

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

        void assignMultiblock(LevelAccessor level, BlockPos blockPos) {
            this.forEachBlock((blockState, index) -> {
                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                if (blockEntity instanceof MultiblockPart part)
                    part.setIndex(multiblock, index, facing);
            });
        }

        public void forEachBlock(BiConsumer<BlockState, MultiblockIndex> consumer) {
            for (int i = 0; i < multiblock.width; i++) {
                for (int j = 0; j < multiblock.height; j++) {
                    for (int k = 0; k < multiblock.depth; k++) {
                        consumer.accept(getBlock(i, j, k), new MultiblockIndex(i, j, k));
                    }
                }
            }
        }

        public record Data(Map<Character, BuildingBlock> key, List<String[]> pattern) {
            private static final Codec<List<String[]>> PATTERN_CODEC = Codec.STRING.listOf().listOf().comapFlatMap(stringList -> {
                int i = stringList.getFirst().getFirst().length();

                for (String s : stringList.getFirst()) {
                    if (i != s.length()) {
                        return DataResult.error(() -> "Invalid pattern: each row must be the same width");
                    }
                }

                return DataResult.success(stringList.stream()
                        .map(list -> list.toArray(new String[0]))
                        .toList());
            }, stringArray -> stringArray.stream()
                        .map(Arrays::asList)
                        .toList());
            private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap(symbol -> {
                if (symbol.length() != 1) {
                    return DataResult.error(() -> "Invalid key entry: '" + symbol + "' is an invalid symbol (must be 1 character only).");
                } else {
                    return " ".equals(symbol) ? DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.") : DataResult.success(symbol.charAt(0));
                }
            }, String::valueOf);
            public static final MapCodec<Data> MAP_CODEC = RecordCodecBuilder.mapCodec(
                    instance -> instance.group(
                            ExtraCodecs.strictUnboundedMap(SYMBOL_CODEC, BuildingBlock.CODEC_NON_EMPTY).fieldOf("key").forGetter(data -> data.key),
                            PATTERN_CODEC.fieldOf("pattern").forGetter(data -> data.pattern)
                    ).apply(instance, Data::new)
            );
        }
    }
}
