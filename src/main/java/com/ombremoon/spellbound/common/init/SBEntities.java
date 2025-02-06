package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.entity.living.LivingShadow;
import com.ombremoon.spellbound.common.content.entity.living.SpellBrokerEntity;
import com.ombremoon.spellbound.common.content.entity.living.Valkyr;
import com.ombremoon.spellbound.common.content.entity.spell.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SBEntities {
    public static final List<AttributesRegister<?>> SUPPLIERS = new ArrayList<>();
    public static final List<Supplier<? extends EntityType<?>>> MOBS = new ArrayList<>();
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister
            .create(Registries.ENTITY_TYPE, Constants.MOD_ID);

    public static final Supplier<EntityType<WildMushroom>> MUSHROOM = ENTITIES.register("wild_mushroom",
            () -> EntityType.Builder.of(WildMushroom::new, MobCategory.MISC).sized(0.9f, 0.9f).build("wild_mushroom"));
    public static final Supplier<EntityType<ShadowGate>> SHADOW_GATE = registerEntity("shadow_gate", ShadowGate::new, 1.2F, 2.5F);
    public static final Supplier<EntityType<SolarRay>> SOLAR_RAY = registerEntity("solar_ray", SolarRay::new, 1.8F, 3.6F);
    public static final Supplier<EntityType<StormstrikeBolt>> STORMSTRIKE_BOLT = registerEntity("stormstrike_bolt", StormstrikeBolt::new, 0.5F, 0.5F);
    public static final Supplier<EntityType<StormRift>> STORM_RIFT = registerEntity("storm_rift", StormRift::new, 1.9F, 2.9F);
    public static final Supplier<EntityType<Cyclone>> CYCLONE = registerEntity("cyclone", Cyclone::new, 3.0F, 3.0F);
    public static final Supplier<EntityType<Hail>> HAIL = registerEntity("hail", Hail::new, 3.0F, 3.0F);

    public static final Supplier<EntityType<LivingShadow>> LIVING_SHADOW = registerMob("living_shadow", LivingShadow::new, MobCategory.CREATURE,0.6F, 1.95F, 8, LivingShadow::createLivingShadowAttributes, false);
    public static final Supplier<EntityType<Valkyr>> VALKYR = registerMob("valkyr", Valkyr::new, MobCategory.CREATURE,0.6F, 1.95F, 8, Valkyr::createValkyrAttributes, false);
    //public static final Supplier<EntityType<TotemSpiritEntity>> TOTEM_SPIRIT = registerMob("totem_spirit", TotemSpiritEntity::new, MobCategory.CREATURE, 1f, 1f, 8, LivingShadow::createLivingShadowAttributes, false);
    public static final Supplier<EntityType<SpellBrokerEntity>> SPELL_BROKER = registerMob("spell_broker", SpellBrokerEntity::new, MobCategory.CREATURE, 1f, 1f, 8, SpellBrokerEntity::createAttributes);

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

        var entitySupplier = SBEntities.ENTITIES.register(name, () -> {
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
        EntityType.Builder<T> builder = EntityType.Builder.of(factory, MobCategory.MISC).sized(width, height).fireImmune().clientTrackingRange(4);

        return SBEntities.ENTITIES.register(name, () -> {
            return builder.build(name);
        });
    }

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public record AttributesRegister<E extends LivingEntity>(Supplier<EntityType<E>> entityTypeSupplier, Supplier<AttributeSupplier.Builder> attributeSupplier) {}
}
