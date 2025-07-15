package com.ombremoon.spellbound.common.magic.api.buff;

import com.google.common.collect.Maps;
import com.ombremoon.spellbound.common.init.SBSkills;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.util.SpellUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public record SkillBuff<T>(Skill skill, BuffCategory category, BuffObject<T> buffObject, T object) {
    private static final Map<String, BuffObject<?>> REGISTERED_OBJECTS = Maps.newHashMap();
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillBuff<?>> STREAM_CODEC = StreamCodec.ofMember(
            SkillBuff::toNetwork, SkillBuff::fromNetwork
    );

    public static final BuffObject<MobEffectInstance> MOB_EFFECT = registerBuffObject(
            "mob_effect",
            LivingEntity::addEffect,
            (livingEntity, mobEffectInstance) -> livingEntity.removeEffect(mobEffectInstance.getEffect()),
            (o, o1) -> o instanceof MobEffectInstance instance && o1 instanceof MobEffectInstance instance1 && instance.is(instance1.getEffect()),
            MobEffectInstance.STREAM_CODEC);

    public static final BuffObject<ModifierData> ATTRIBUTE_MODIFIER = registerBuffObject(
            "attribute_modifier",
            (livingEntity, modifierData) -> {
                var instance = livingEntity.getAttribute(modifierData.attribute());
                if (instance != null && !instance.hasModifier(modifierData.attributeModifier().id()))
                    instance.addTransientModifier(modifierData.attributeModifier());
            },
            (livingEntity, modifierData) -> {
                var instance = livingEntity.getAttribute(modifierData.attribute());
                if (instance != null && instance.hasModifier(modifierData.attributeModifier().id()))
                    instance.removeModifier(modifierData.attributeModifier());
            },
            (o, o1) -> o instanceof ModifierData data && o1 instanceof ModifierData data1 && data.attributeModifier().is(data1.attributeModifier().id()),
            ModifierData.STREAM_CODEC);

    public static final BuffObject<SpellModifier> SPELL_MODIFIER = registerBuffObject(
            "spell_modifier",
            (livingEntity, spellModifier) -> {
                var skills = SpellUtil.getSkills(livingEntity);
                skills.addModifierWithExpiry(spellModifier);
            },
            (livingEntity, spellModifier) -> {
                var skills = SpellUtil.getSkills(livingEntity);
                skills.removeModifier(spellModifier);
            },
            (o, o1) -> o instanceof SpellModifier modifier && o1 instanceof SpellModifier modifier1 && modifier.equals(modifier1),
            SpellModifier.STREAM_CODEC);

    public static final BuffObject<ResourceLocation> EVENT = registerBuffObject(
            "event",
            (livingEntity, resourceLocation) -> {
            },
            (livingEntity, resourceLocation) -> {
                var handler = SpellUtil.getSpellCaster(livingEntity);
                handler.getListener().removeListener(resourceLocation);
            },
            (o, o1) -> o instanceof ResourceLocation location && o1 instanceof ResourceLocation location1 && location.equals(location1),
            ResourceLocation.STREAM_CODEC);

    private void toNetwork(RegistryFriendlyByteBuf buf) {
        ByteBufCodecs.registry(SBSkills.SKILL_REGISTRY_KEY).encode(buf, this.skill);
        NeoForgeStreamCodecs.enumCodec(BuffCategory.class).encode(buf, this.category);
        BuffObject.STREAM_CODEC.encode(buf, this.buffObject);
        this.buffObject.objectStreamCodec.encode(buf, this.object);
    }

    @SuppressWarnings("unchecked")
    private static <T> SkillBuff<T> fromNetwork(RegistryFriendlyByteBuf buf) {
        Skill skill = ByteBufCodecs.registry(SBSkills.SKILL_REGISTRY_KEY).decode(buf);
        BuffCategory category = NeoForgeStreamCodecs.enumCodec(BuffCategory.class).decode(buf);
        BuffObject<T> buffObject = (BuffObject<T>) BuffObject.STREAM_CODEC.decode(buf);
        T object = buffObject.objectStreamCodec.decode(buf);
        return new SkillBuff<>(skill, category, buffObject, object);
    }

    private static <T> BuffObject<T> registerBuffObject(String name,
                                                        BiConsumer<LivingEntity,
                                                                T> addObject,
                                                        BiConsumer<LivingEntity, T> removeObject,
                                                        BiPredicate<Object, Object> equalCondition,
                                                        StreamCodec<? super RegistryFriendlyByteBuf, T> objectStreamCodec) {
        BuffObject<T> object = new BuffObject<>(name, addObject, removeObject, equalCondition, objectStreamCodec);
        REGISTERED_OBJECTS.put(name, object);
        return object;
    }

    public void addBuff(LivingEntity livingEntity) {
        if (this.buffObject != null)
            this.buffObject.addObject().accept(livingEntity, this.object);
    }

    public void removeBuff(LivingEntity livingEntity) {
        if (this.buffObject != null)
            this.buffObject.removeObject().accept(livingEntity, this.object);
    }

    public boolean isSkill(Skill skill) {
        return this.skill.equals(skill);
    }

    public boolean isBeneficial() {
        return this.category == BuffCategory.BENEFICIAL;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            return obj instanceof SkillBuff<?> skillBuff && skillBuff.buffObject.equalCondition.test(skillBuff.object, this.object);
        }
    }

    @Override
    public String toString() {
        return "SkillBuff: [" + "Skill: " + this.skill + ", Category: " + this.category + ", Buff: " + this.object + "]";
    }

    public record BuffObject<T>(
            String name,
            BiConsumer<LivingEntity, T> addObject,
            BiConsumer<LivingEntity, T> removeObject,
            BiPredicate<Object, Object> equalCondition,
            StreamCodec<? super RegistryFriendlyByteBuf, T> objectStreamCodec) {

        public static final StreamCodec<ByteBuf, BuffObject<?>> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
                .map(BuffObject::parse, BuffObject::name);

        @SuppressWarnings("unchecked")
        private static <T> BuffObject<T> parse(String name) {
            return (BuffObject<T>) REGISTERED_OBJECTS.get(name);
        }
    }
}