package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.entity.living.LivingShadow;
import com.ombremoon.spellbound.common.content.entity.living.TotemSpiritEntity;
import com.ombremoon.spellbound.common.content.entity.spell.WildMushroom;
import com.ombremoon.spellbound.common.content.entity.spell.ShadowGate;
import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EntityInit {
    public static final List<AttributesRegister<?>> SUPPLIERS = new ArrayList<>();
    public static final List<Supplier<? extends EntityType<?>>> MOBS = new ArrayList<>();
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister
            .create(Registries.ENTITY_TYPE, Constants.MOD_ID);

    public static final Supplier<EntityType<WildMushroom>> MUSHROOM = ENTITIES.register("wild_mushroom",
            () -> EntityType.Builder.of(WildMushroom::new, MobCategory.MISC).sized(0.9f, 0.9f).build("wild_mushroom"));
    public static final Supplier<EntityType<ShadowGate>> SHADOW_GATE = registerEntity("shadow_gate", ShadowGate::new, 1.2F, 2.5F);
    public static final Supplier<EntityType<SolarRay>> SOLAR_RAY = registerEntity("solar_ray", SolarRay::new, 1.8F, 3.6F);
    public static final Supplier<EntityType<LivingShadow>> LIVING_SHADOW = registerMob("living_shadow", LivingShadow::new, MobCategory.CREATURE,0.6F, 1.95F, 8, LivingShadow::createLivingShadowAttributes, false);
    //public static final Supplier<EntityType<TotemSpiritEntity>> TOTEM_SPIRIT = registerMob("totem_spirit", TotemSpiritEntity::new, MobCategory.CREATURE, 1f, 1f, 8, LivingShadow::createLivingShadowAttributes, false);


    protected static <T extends Mob> Supplier<EntityType<T>> registerMob(String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height, int clientTrackingRange, Supplier<AttributeSupplier.Builder> attributeSupplier) {
        return registerMob(name, factory, category, true, width, height, clientTrackingRange, attributeSupplier, true);
    }

    protected static <T extends Mob> Supplier<EntityType<T>> registerMob(String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height, int clientTrackingRange, Supplier<AttributeSupplier.Builder> attributeSupplier, boolean hasLoot) {
        return registerMob(name, factory, category, true, width, height, clientTrackingRange, attributeSupplier, hasLoot);
    }

    protected static <T extends Mob> Supplier<EntityType<T>> registerMob(String name, EntityType.EntityFactory<T> factory, MobCategory mobCategory, boolean fireImmune, float width, float height, int clientTrackingRange, Supplier<AttributeSupplier.Builder> attributeSupplier, boolean hasLoot) {
        EntityType.Builder<T> builder = EntityType.Builder.of(factory, mobCategory).sized(width, height).clientTrackingRange(clientTrackingRange);

        if (fireImmune) {
            builder.fireImmune();
        }

        var entitySupplier = EntityInit.ENTITIES.register(name, () -> {
            EntityType<T> entityType = builder.build(name);
            if (attributeSupplier != null) {
                SUPPLIERS.add(new AttributesRegister<>(() -> entityType, attributeSupplier));
            }
            return entityType;
        });
        if (hasLoot) MOBS.add(entitySupplier);
        return entitySupplier;
    }

    protected static <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, EntityType.EntityFactory<T> factory, float width, float height) {
        EntityType.Builder<T> builder = EntityType.Builder.of(factory, MobCategory.MISC).sized(width, height).clientTrackingRange(4);

        return EntityInit.ENTITIES.register(name, () -> {
            return builder.build(name);
        });
    }

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public record AttributesRegister<E extends LivingEntity>(Supplier<EntityType<E>> entityTypeSupplier, Supplier<AttributeSupplier.Builder> attributeSupplier) {}
}
