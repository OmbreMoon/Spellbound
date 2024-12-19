package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.block.DivineShrineBlock;
import com.ombremoon.spellbound.common.content.block.SummonPortalBlock;
import com.ombremoon.spellbound.common.content.block.SummonStoneBlock;
import com.ombremoon.spellbound.common.content.block.UnnamedWorkbenchBlock;
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
    public static final Supplier<DivineShrineBlock> DIVINE_SHRINE = registerBlock("divine_shrine", () -> new DivineShrineBlock(BlockBehaviour.Properties.of()));
    public static final Supplier<SummonStoneBlock> SUMMON_STONE = registerBlock("summon_stone", () -> new SummonStoneBlock(BlockBehaviour.Properties.of()));
    public static final Supplier<SummonPortalBlock> SUMMON_PORTAL = registerBlock("summon_portal", () ->
            new SummonPortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .noCollission()
                    .lightLevel(state -> 15)
                    .strength(-1.0F, 3600000.0F)
                    .noLootTable()
                    .pushReaction(PushReaction.BLOCK)));

    public static void registerSummonStone(String name, String spell) {
        registerBlock(name, () -> new SummonStoneBlock(CommonClass.customLocation(spell), BlockBehaviour.Properties.of()));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> deferredBlock = BLOCKS.register(name, block);
        SBItems.registerBlockItem(name, deferredBlock);
        return deferredBlock;
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
