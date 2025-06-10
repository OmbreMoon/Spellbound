package com.ombremoon.spellbound.datagen;

import com.mojang.datafixers.util.Pair;
import com.ombremoon.spellbound.common.content.block.RuneBlock;
import com.ombremoon.spellbound.common.content.world.multiblock.BuildingBlock;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockOutput;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockProvider;
import com.ombremoon.spellbound.common.content.world.multiblock.type.StandardMultiblock;
import com.ombremoon.spellbound.common.init.SBBlocks;
import com.ombremoon.spellbound.common.init.SBTags;
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
                .key('^', BuildingBlock.ANY)
                .key('$', BuildingBlock.of(Blocks.DIAMOND_BLOCK))
                .build(multiblockOutput, CommonClass.customLocation("building_block_test"));

        StandardMultiblock.Builder.of()
                .pattern("  ^^^  ",
                         " $   $ ",
                         "^     ^",
                         "^  #  ^",
                         "^     ^",
                         " $   $ ",
                         "  ^^^  ")
                .key('^', BuildingBlock.of(SBBlocks.RUNE.get()))
                .key('$', BuildingBlock.of(SBBlocks.TRANSFIGURATION_DISPLAY.get()))
                .key('#', BuildingBlock.of(SBBlocks.TRANSFIGURATION_PEDESTAL.get()))
                .index(3, 0, 3)
                .build(multiblockOutput, CommonClass.customLocation("one_ring"));
    }
}
