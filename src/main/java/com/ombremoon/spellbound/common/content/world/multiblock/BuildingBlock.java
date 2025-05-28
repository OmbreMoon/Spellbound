package com.ombremoon.spellbound.common.content.world.multiblock;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BuildingBlock implements Predicate<BlockState> {
    public static final BuildingBlock EMPTY = new BuildingBlock(Stream.empty());
    private final Value[] values;
    @Nullable
    private BlockState[] blockStates;

    public static final Codec<BuildingBlock> CODEC = codec(true);
    public static final Codec<BuildingBlock> CODEC_NON_EMPTY = codec(false);

    private BuildingBlock(Stream<? extends Value> values) {
        this.values = values.toArray(Value[]::new);
    }

    private BuildingBlock(Value[] values) {
        this.values = values;
    }

    public BlockState[] getBlocks() {
        if (this.blockStates == null) {
            final Stream<BlockState> stream = Arrays.stream(this.values).flatMap(value -> value.getBlocks().stream());
            this.blockStates = stream.collect(Collectors.toCollection(BlockStateLinkedSet::createTypeAndComponentsSet)).toArray(BlockState[]::new);
        }

        return this.blockStates;
    }

    @Override
    public boolean test(BlockState blockState) {
        if (blockState == null) {
            return false;
        } else if (this.isEmpty()) {
            return blockState.isEmpty();
        } else {
            for (BlockState blockState1 : this.getBlocks()) {
                if (blockState == blockState1) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isEmpty() {
        return this.values.length == 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BuildingBlock buildingBlock && Arrays.equals(this.values, buildingBlock.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    public Value[] getValues() {
        return this.values;
    }

    public static BuildingBlock fromValues(Stream<? extends Value> stream) {
        BuildingBlock buildingBlock = new BuildingBlock(stream);
        return buildingBlock.isEmpty() ? EMPTY : buildingBlock;
    }

    public static BuildingBlock of() {
        return EMPTY;
    }

    public static BuildingBlock of(Block... blocks) {
        return of(Arrays.stream(blocks).map(Block::defaultBlockState));
    }

    public static BuildingBlock of(BlockState... blocks) {
        return of(Arrays.stream(blocks));
    }

    public static BuildingBlock of(Stream<BlockState> blockStates) {
        return fromValues(blockStates.filter(blockState -> !blockState.isEmpty()).map(BlockValue::new));
    }

    public static BuildingBlock of(TagKey<Block> tag) {
        return fromValues(Stream.of(new TagValue(tag)));
    }

    private static Codec<BuildingBlock> codec(boolean allowEmpty) {
        Codec<Value[]> codec = Codec.list(Value.CODEC)
                .comapFlatMap(
                        values -> !allowEmpty && values.isEmpty()
                                ? DataResult.error(() -> "Block array cannot be empty, at least one block must be defined")
                                : DataResult.success(values.toArray(new Value[0])),
                        List::of
                );
        return Codec.either(codec, Value.CODEC)
                .flatComapMap(
                        either -> either.map(BuildingBlock::new, value -> new BuildingBlock(new Value[]{value})),
                        buildingBlock -> {
                            if (buildingBlock.values.length == 1) {
                                return DataResult.success(Either.right(buildingBlock.values[0]));
                            } else {
                                return buildingBlock.values.length == 0 && !allowEmpty
                                        ? DataResult.error(() -> "Block array cannot be empty, at least one block must be defined")
                                        : DataResult.success(Either.left(buildingBlock.values));
                            }
                        }
                );
    }

    public record BlockValue(BlockState block) implements Value {
        static final MapCodec<BlockValue> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        BlockState.CODEC.fieldOf("block").forGetter(blockValue -> blockValue.block)
                ).apply(instance, BlockValue::new)
        );
        static final Codec<BlockValue> CODEC = MAP_CODEC.codec();

        @Override
        public boolean equals(Object obj) {
            return obj instanceof BlockValue blockValue && blockValue.block.getBlock().equals(this.block.getBlock()) && blockValue.block == this.block;
        }

        @Override
        public int hashCode() {
            int i = 31 * block.getBlock().hashCode();
            return 31 * i + block.getValues().hashCode();
        }

        @Override
        public Collection<BlockState> getBlocks() {
            return Collections.singleton(this.block);
        }
    }

    public record TagValue(TagKey<Block> tag) implements Value {
        static final MapCodec<TagValue> MAP_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter(tagValue -> tagValue.tag)
                ).apply(instance, TagValue::new)
        );
        static final Codec<TagValue> CODEC = MAP_CODEC.codec();

        @Override
        public boolean equals(Object obj) {
            return obj instanceof TagValue tagValue && tagValue.tag.location().equals(this.tag.location());
        }

        @Override
        public Collection<BlockState> getBlocks() {
            List<BlockState> list = Lists.newArrayList();

            for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(this.tag)) {
                list.add(holder.value().defaultBlockState());
            }

            return list;
        }
    }

    public interface Value {
        MapCodec<Value> MAP_CODEC = NeoForgeExtraCodecs.xor(BlockValue.MAP_CODEC, TagValue.MAP_CODEC)
                .xmap(either -> either.map(blockValue -> blockValue, tagValue -> tagValue), value -> {
                    if (value instanceof TagValue tagValue) {
                        return Either.right(tagValue);
                    } else if (value instanceof BlockValue blockValue) {
                        return Either.left(blockValue);
                    } else {
                        throw new UnsupportedOperationException("This is neither a block nor a tag value.");
                    }
                });
        Codec<Value> CODEC = MAP_CODEC.codec();

        Collection<BlockState> getBlocks();
    }
}
