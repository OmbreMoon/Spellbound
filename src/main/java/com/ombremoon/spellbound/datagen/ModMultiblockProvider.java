package com.ombremoon.spellbound.datagen;

import com.mojang.datafixers.util.Pair;
import com.ombremoon.spellbound.common.content.world.multiblock.BuildingBlock;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockOutput;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockProvider;
import com.ombremoon.spellbound.common.content.world.multiblock.type.StandardMultiblock;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
public class ModMultiblockProvider extends MultiblockProvider {
    public ModMultiblockProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildMultiblocks(MultiblockOutput multiblockOutput) {
        StandardMultiblock.Builder.of()
                .pattern("^ ^",
                         " $ ",
                         "^$^")
                .pattern("$ $",
                         "   ",
                         "$ $")
                .pattern("^^^",
                         "^^^",
                         "^^^")
                .key('^', BuildingBlock.of(Blocks.GOLD_BLOCK))
                .key('$', BuildingBlock.of(Blocks.DIAMOND_BLOCK))
                .build(multiblockOutput, CommonClass.customLocation("building_block_test"));
    }

    protected static class BlockBuilder {
        private final BlockPredicate.Builder blockBuilder;
        private StatePropertiesPredicate.Builder stateBuilder;

        public BlockBuilder() {
            this.blockBuilder = BlockPredicate.Builder.block();
        }

        public static BlockBuilder of(Block block) {
            return new BlockBuilder().block(block);
        }

        private BlockBuilder block(Block block) {
            this.blockBuilder.of(block);
            return this;
        }

        public <T extends Comparable<T>> BlockBuilder withProperties(Property<T> property, T value) {
            if (this.stateBuilder == null)
                this.stateBuilder = StatePropertiesPredicate.Builder.properties();

            if (value instanceof Integer intValue) {
                this.stateBuilder.hasProperty((Property<Integer>) property, intValue);
            } else if (value instanceof Boolean booleanValue) {
                this.stateBuilder.hasProperty((Property<Boolean>) property, booleanValue);
            } else if (value instanceof StringRepresentable stringValue) {
                this.stateBuilder.hasProperty(property, stringValue.getSerializedName());
            }

            return this;
        }

        public BlockPredicate build() {
            if (this.stateBuilder != null)
                this.blockBuilder.setProperties(this.stateBuilder);

            return this.blockBuilder.build();
        }
    }
}
