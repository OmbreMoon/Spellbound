package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.common.content.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SBBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);

    public static final Supplier<UnnamedWorkbenchBlock> UNNAMED = registerBlock("unnamed_workbench", () -> new UnnamedWorkbenchBlock(blockProperties()
            .noOcclusion()));
    public static final Supplier<ArcanthusCropBlock> ARCANTHUS = registerBlock("arcanthus", () -> new ArcanthusCropBlock(blockProperties().
            noOcclusion()));
    public static final Supplier<DivineShrineBlock> DIVINE_SHRINE = registerBlock("divine_shrine", () -> new DivineShrineBlock(blockProperties()));
    public static final Supplier<TransfigurationPedestalBlock> TRANSFIGURATION_PEDESTAL = registerBlock("transfiguration_pedestal", () -> new TransfigurationPedestalBlock(blockProperties()
            .noOcclusion()));
    public static final Supplier<TransfigurationDisplayBlock> TRANSFIGURATION_DISPLAY = registerBlock("transfiguration_display", () -> new TransfigurationDisplayBlock(blockProperties()
            .noOcclusion()));
    public static final Supplier<RuneBlock> RUNE = registerBlock("rune", () -> new RuneBlock(blockProperties()
            .strength(0.1F)
            .noCollission()
            .pushReaction(PushReaction.DESTROY)
            .noLootTable()
            .noOcclusion()),
            false);
    public static final Supplier<SummonStoneBlock> SUMMON_STONE = registerBlock("summon_stone", () -> new SummonStoneBlock(blockProperties()
            .lightLevel(state -> state.getValue(SummonStoneBlock.POWERED) ? 13 : 0)));
    public static final Supplier<Block> CRACKED_SUMMON_STONE = registerSimpleBlock("cracked_summon_stone");
    public static final Supplier<SummonPortalBlock> SUMMON_PORTAL = registerBlock("summon_portal", () -> new SummonPortalBlock(blockProperties()
            .mapColor(MapColor.COLOR_BLACK)
            .noCollission()
            .lightLevel(state -> 11)
            .strength(-1.0F)
            .sound(SoundType.GLASS)
            .noLootTable()
            .pushReaction(PushReaction.BLOCK)),
            false);
    public static final Supplier<Block> UMBRAL_SLUDGE = registerSludge("umbral_sludge", true);
    public static final Supplier<Block> PENUMBRAL_SLUDGE = registerSludge("penumbral_sludge", false);
//    public static final Supplier<Block> WOVEN_SHADE = registerSimpleBlock("woven_shade");

    public static final Supplier<Block> GREEN_SPORE_BLOCK = registerBlock("green_spore_block", () -> new SporeBlock(blockProperties()));
    public static final Supplier<Block> GREEN_SPORE_SLAB = registerBlock("green_spore_slab", () -> new SporeSlabBlock(blockProperties()));
    public static final Supplier<Block> PURPLE_SPORE_BLOCK = registerBlock("purple_spore_block", () -> new SporeBlock(blockProperties()));
    public static final Supplier<Block> PURPLE_SPORE_SLAB = registerBlock("purple_spore_slab", () -> new SporeSlabBlock(blockProperties()));
    public static final Supplier<Block> PINK_SPORE_BLOCK = registerBlock("pink_spore_block", () -> new SporeBlock(blockProperties()));
    public static final Supplier<Block> PINK_SPORE_SLAB = registerBlock("pink_spore_slab", () -> new SporeSlabBlock(blockProperties()));
    public static final Supplier<Block> RED_SPORE_BLOCK = registerBlock("red_spore_block", () -> new SporeBlock(blockProperties()));
    public static final Supplier<Block> RED_SPORE_SLAB = registerBlock("red_spore_slab", () -> new SporeSlabBlock(blockProperties()));
    public static final Supplier<Block> MYCELIUM_CARPET = registerBlock("mycelium_carpet", () -> new MyceliumCarpetBlock(blockProperties()
            .mapColor(MapColor.COLOR_PURPLE)
            .strength(0.1F)
            .sound(SoundType.MOSS_CARPET)
            .pushReaction(PushReaction.DESTROY)
            .randomTicks()));

    public static void registerSummonStone(String name, String spell) {
        registerBlock(name, () -> new SummonStoneBlock(CommonClass.customLocation(spell), blockProperties()));
    }

    public static DeferredBlock<Block> registerSludge(String name, boolean causeHarm) {
        return registerBlock(name, () -> new SludgeBlock(causeHarm, blockProperties()
                .mapColor(MapColor.COLOR_BLACK)
                .forceSolidOn()
                .noCollission()
                .strength(50.0F, 1200.0F)));
    }

    private static DeferredBlock<Block> registerSimpleBlock(String name) {
        return registerBlock(name, () -> new Block(blockProperties()), true);
    }

    private static BlockBehaviour.Properties blockProperties() {
        return BlockBehaviour.Properties.of();
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        return registerBlock(name, block, true);
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block, boolean registerItem) {
        DeferredBlock<T> deferredBlock = BLOCKS.register(name, block);
        if (registerItem)
            SBItems.registerBlockItem(name, deferredBlock);

        return deferredBlock;
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}