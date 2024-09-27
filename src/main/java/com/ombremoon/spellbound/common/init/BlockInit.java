package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.block.UnnamedWorkbenchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockInit {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);

    public static final Supplier<UnnamedWorkbenchBlock> UNNAMED = registerBlock("unnamed_workbench", () -> new UnnamedWorkbenchBlock(BlockBehaviour.Properties.of().noCollission()));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> deferredBlock = BLOCKS.register(name, block);
        ItemInit.registerBlockItem(name, deferredBlock);
        return deferredBlock;
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
