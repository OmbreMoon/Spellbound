package com.ombremoon.spellbound.common.init;

import com.mojang.datafixers.kinds.Const;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.entity.custom.MushroomEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister
            .create(Registries.ENTITY_TYPE, Constants.MOD_ID);

    public static final Supplier<EntityType<MushroomEntity>> MUSHROOM = ENTITIES.register("mushroom",
            () -> EntityType.Builder.of(MushroomEntity::new, MobCategory.MISC).sized(0.9f, 0.9f).build("mushroom"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
