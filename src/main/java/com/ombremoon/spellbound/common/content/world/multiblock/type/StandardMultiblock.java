package com.ombremoon.spellbound.common.content.world.multiblock.type;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ombremoon.spellbound.common.content.world.multiblock.BuildingBlock;
import com.ombremoon.spellbound.common.content.world.multiblock.Multiblock;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockOutput;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockSerializer;
import com.ombremoon.spellbound.common.init.SBMultiblockSerializers;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class StandardMultiblock extends Multiblock {
    public StandardMultiblock(MultiblockStructure structure) {
        super(structure);
    }

    @Override
    public MultiblockSerializer<?> getSerializer() {
        return SBMultiblockSerializers.STANDARD_MULTIBLOCK.get();
    }

    public static class Builder extends MultiblockBuilder {

       public static Builder of() {
           return new Builder();
       }

        public Builder pattern(String... pattern) {
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

        public Builder key(char symbol, BuildingBlock block) {
            this.key.put(symbol, block);
            return this;
        }

        @Override
        public void build(MultiblockOutput output, ResourceLocation location) {
            MultiblockStructure structure = MultiblockStructure.of(this.key, this.pattern, this.activeIndex);
            StandardMultiblock multiblock = new StandardMultiblock(structure);
            output.accept(location, multiblock);
        }
    }

    public static class Serializer implements MultiblockSerializer<StandardMultiblock> {
        public static final MapCodec<StandardMultiblock> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        MultiblockStructure.MAP_CODEC.forGetter(standardMultiblock -> standardMultiblock.info)
                ).apply(instance, StandardMultiblock::new)
        );

        @Override
        public MapCodec<StandardMultiblock> codec() {
            return CODEC;
        }
    }
}
