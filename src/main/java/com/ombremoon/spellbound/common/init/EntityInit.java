package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.entity.spell.WildMushroom;
import com.ombremoon.spellbound.common.content.entity.spell.ShadowGate;
import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister
            .create(Registries.ENTITY_TYPE, Constants.MOD_ID);

    public static final Supplier<EntityType<WildMushroom>> MUSHROOM = ENTITIES.register("wild_mushroom",
            () -> EntityType.Builder.of(WildMushroom::new, MobCategory.MISC).sized(0.9f, 0.9f).build("wild_mushroom"));
    public static final Supplier<EntityType<ShadowGate>> SHADOW_GATE = registerEntity("shadow_gate", ShadowGate::new, 1.2F, 2.5F);
    public static final Supplier<EntityType<SolarRay>> SOLAR_RAY = registerEntity("solar_ray", SolarRay::new, 1.8F, 3.6F);


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
