package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.common.content.block.entity.*;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SBBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public static final Supplier<BlockEntityType<SummonBlockEntity>> SUMMON_PORTAL = BLOCK_ENTITY_TYPE.register("summon_portal", () -> BlockEntityType.Builder.of(SummonBlockEntity::new, SBBlocks.SUMMON_PORTAL.get()).build(null));
    public static final Supplier<BlockEntityType<RuneBlockEntity>> RUNE = BLOCK_ENTITY_TYPE.register("rune", () -> BlockEntityType.Builder.of(RuneBlockEntity::new, SBBlocks.RUNE.get()).build(null));
    public static final Supplier<BlockEntityType<TransfigurationDisplayBlockEntity>> TRANSFIGURATION_DISPLAY = BLOCK_ENTITY_TYPE.register("transfiguration_display", () -> BlockEntityType.Builder.of(TransfigurationDisplayBlockEntity::new, SBBlocks.TRANSFIGURATION_DISPLAY.get()).build(null));
    public static final Supplier<BlockEntityType<PedestalBlockEntity>> PEDESTAL = BLOCK_ENTITY_TYPE.register("pedestal", () -> BlockEntityType.Builder.of(PedestalBlockEntity::new, SBBlocks.TRANSFIGURATION_PEDESTAL.get()).build(null));
    public static final Supplier<BlockEntityType<SimpleExtendedBlockEntity>> SIMPLE_EXTENDED_BLOCK = BLOCK_ENTITY_TYPE.register("simple_multiblock", () -> BlockEntityType.Builder.of(SimpleExtendedBlockEntity::new, SBBlocks.ARCANE_WORKBENCH.get(), SBBlocks.SANDSTONE_DIVINE_SHRINE.get(), SBBlocks.JUNGLE_DIVINE_SHRINE.get(), SBBlocks.PLAINS_DIVINE_SHRINE.get()).build(null));
    public static final Supplier<BlockEntityType<ValkyrBlockEntity>> VALKY_STATUE = BLOCK_ENTITY_TYPE.register("valkyr_statue", () -> BlockEntityType.Builder.of(ValkyrBlockEntity::new, SBBlocks.VALKYR_STATUE.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPE.register(modEventBus);
    }
}
