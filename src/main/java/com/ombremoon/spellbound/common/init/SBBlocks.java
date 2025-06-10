package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.common.content.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SBBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);

    public static final Supplier<UnnamedWorkbenchBlock> UNNAMED = registerBlock("unnamed_workbench", () -> new UnnamedWorkbenchBlock(BlockBehaviour.Properties.of().noOcclusion()));
    public static final Supplier<ArcanthusCropBlock> ARCANTHUS = registerBlock("arcanthus", () -> new ArcanthusCropBlock(BlockBehaviour.Properties.of().noOcclusion()));
    public static final Supplier<DivineShrineBlock> DIVINE_SHRINE = registerBlock("divine_shrine", () -> new DivineShrineBlock(BlockBehaviour.Properties.of()));
    public static final Supplier<TransfigurationPedestalBlock> TRANSFIGURATION_PEDESTAL = registerBlock("transfiguration_pedestal", () -> new TransfigurationPedestalBlock(BlockBehaviour.Properties.of()));
    public static final Supplier<TransfigurationDisplayBlock> TRANSFIGURATION_DISPLAY = registerBlock("transfiguration_display", () -> new TransfigurationDisplayBlock(BlockBehaviour.Properties.of()));
    public static final Supplier<RuneBlock> RUNE = registerBlock("rune", () -> new RuneBlock(BlockBehaviour.Properties.of()
            .strength(0.1F)
            .noCollission()
            .pushReaction(PushReaction.DESTROY)
            .noLootTable()
            .noOcclusion()));
    public static final Supplier<SummonStoneBlock> SUMMON_STONE = registerBlock("summon_stone", () -> new SummonStoneBlock(BlockBehaviour.Properties.of()));
    public static final Supplier<SummonPortalBlock> SUMMON_PORTAL = registerBlock("summon_portal", () ->
            new SummonPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .noCollission()
                    .lightLevel(state -> 15)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK)), false);

    public static void registerSummonStone(String name, String spell) {
        registerBlock(name, () -> new SummonStoneBlock(CommonClass.customLocation(spell), BlockBehaviour.Properties.of()));
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