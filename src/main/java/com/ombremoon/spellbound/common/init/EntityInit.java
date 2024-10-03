package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.entity.MushroomEntity;
import com.ombremoon.spellbound.common.content.entity.ShadowGate;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister
            .create(Registries.ENTITY_TYPE, Constants.MOD_ID);

    public static final Supplier<EntityType<MushroomEntity>> MUSHROOM = ENTITIES.register("wild_mushroom",
            () -> EntityType.Builder.of(MushroomEntity::new, MobCategory.MISC).sized(0.9f, 0.9f).build("wild_mushroom"));
    public static final Supplier<EntityType<ShadowGate>> SHADOW_GATE = registerEntity("shadow_gate", ShadowGate::new, 0.9F, 0.9F);


    protected static <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, EntityType.EntityFactory<T> factory, float width, float height) {
        EntityType.Builder<T> builder = EntityType.Builder.of(factory, MobCategory.MISC).sized(width, height).clientTrackingRange(4);

        return EntityInit.ENTITIES.register(name, () -> {
            return builder.build(name);
        });
    }

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
