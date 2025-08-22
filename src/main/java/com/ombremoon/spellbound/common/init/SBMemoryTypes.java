package com.ombremoon.spellbound.common.init;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.ombremoon.spellbound.main.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SBMemoryTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_TYPES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, Constants.MOD_ID);

    public static final Supplier<MemoryModuleType<List<Pair<BlockPos, BlockState>>>> NEARBY_SHRINES = register("nearby_shrines");

    private static <T> Supplier<MemoryModuleType<T>> register(String id) {
        return register(id, Optional.empty());
    }

    private static <T> Supplier<MemoryModuleType<T>> register(String id, Optional<Codec<T>> codec) {
        return MEMORY_TYPES.register(id, () -> new MemoryModuleType<>(codec));
    }

    public static void register(IEventBus modEventBus) {
        MEMORY_TYPES.register(modEventBus);
    }

}
