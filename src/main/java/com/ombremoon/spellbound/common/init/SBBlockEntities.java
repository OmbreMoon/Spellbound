package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.block.entity.SummonBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SBBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPE = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    public static final Supplier<BlockEntityType<SummonBlockEntity>> SUMMON_PORTAL = BLOCK_ENTITY_TYPE.register("summon_portal", () -> BlockEntityType.Builder.of(SummonBlockEntity::new, SBBlocks.SUMMON_PORTAL.get()).build(null));

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPE.register(modEventBus);
    }
}