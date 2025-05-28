package com.ombremoon.spellbound.datagen;

import com.ombremoon.spellbound.common.content.world.multiblock.BuildingBlock;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockOutput;
import com.ombremoon.spellbound.common.content.world.multiblock.MultiblockProvider;
import com.ombremoon.spellbound.common.content.world.multiblock.type.StandardMultiblock;
import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class ModMultiblockProvider extends MultiblockProvider {
    public ModMultiblockProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildMultiblocks(MultiblockOutput multiblockOutput) {
        StandardMultiblock.Builder.of()
                .pattern("^ ^")
                .pattern(" $ ")
                .pattern("^$^")
                .key('^', BuildingBlock.of(Blocks.ACACIA_DOOR))
                .key('$', BuildingBlock.of(Blocks.GOLD_BLOCK))
                .build(multiblockOutput, CommonClass.customLocation("test"));
    }
}
