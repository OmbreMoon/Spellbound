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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

@SuppressWarnings("unchecked")
public record SkillBuff<T>(Skill skill, BuffCategory category, BuffObject<T> buffObject, T object) {
    private static final Map<String, BuffObject<?>> REGISTERED_OBJECTS = Maps.newHashMap();
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillBuff<?>> STREAM_CODEC = StreamCodec.ofMember(
            SkillBuff::toNetwork, SkillBuff::fromNetwork
    );

    public static final BuffObject<MobEffectInstance> MOB_EFFECT = registerBuffObject(
            "mob_effect",
            (source, livingEntity, effectInstance) -> livingEntity.addEffect(effectInstance, source),
            (livingEntity, mobEffectInstance) -> livingEntity.removeEffect(mobEffectInstance.getEffect()),
            (effectInstance, effectInstance2) -> effectInstance.is(effectInstance2.getEffect()),
            MobEffectInstance.STREAM_CODEC);

    public static final BuffObject<ModifierData> ATTRIBUTE_MODIFIER = registerBuffObject(
            "attribute_modifier",
            (source, livingEntity, modifierData) -> {
                var instance = livingEntity.getAttribute(modifierData.attribute());
                if (instance != null && !instance.hasModifier(modifierData.attributeModifier().id()))
                    instance.addTransientModifier(modifierData.attributeModifier());
            },
            (livingEntity, modifierData) -> {
                var instance = livingEntity.getAttribute(modifierData.attribute());
                if (instance != null && instance.hasModifier(modifierData.attributeModifier().id()))
                    instance.removeModifier(modifierData.attributeModifier());
            },
            (data, data1) -> data.attributeModifier().is(data1.attributeModifier().id()),
            ModifierData.STREAM_CODEC);

    public static final BuffObject<SpellModifier> SPELL_MODIFIER = registerBuffObject(
            "spell_modifier",
            (source, livingEntity, spellModifier) -> {
                var skills = SpellUtil.getSkills(livingEntity);
                skills.addModifier(spellModifier);
            },
            (livingEntity, spellModifier) -> {
                var skills = SpellUtil.getSkills(livingEntity);
                skills.removeModifier(spellModifier);
            },
            SpellModifier::equals,
            SpellModifier.STREAM_CODEC);

    public static final BuffObject<ResourceLocation> EVENT = registerBuffObject(
            "event",
            (source, livingEntity, resourceLocation) -> {
            },
            (livingEntity, resourceLocation) -> {
                var handler = SpellUtil.getSpellHandler(livingEntity);
                handler.getListener().removeListener(resourceLocation);
            },
            ResourceLocation::equals,
            ResourceLocation.STREAM_CODEC);

    private void toNetwork(RegistryFriendlyByteBuf buf) {
        ByteBufCodecs.registry(SBSkills.SKILL_REGISTRY_KEY).encode(buf, this.skill);
        NeoForgeStreamCodecs.enumCodec(BuffCategory.class).encode(buf, this.category);
        BuffObject.STREAM_CODEC.encode(buf, this.buffObject);
        this.buffObject.objectStreamCodec.encode(buf, this.object);
    }

    private static <T> SkillBuff<T> fromNetwork(RegistryFriendlyByteBuf buf) {
        Skill skill = ByteBufCodecs.registry(SBSkills.SKILL_REGISTRY_KEY).decode(buf);
        BuffCategory category = NeoForgeStreamCodecs.enumCodec(BuffCategory.class).decode(buf);
        BuffObject<T> buffObject = (BuffObject<T>) BuffObject.STREAM_CODEC.decode(buf);
        T object = buffObject.objectStreamCodec.decode(buf);
        return new SkillBuff<>(skill, category, buffObject, object);
    }

    private static <T> BuffObject<T> registerBuffObject(String name,
                                                        TriConsumer<Entity, LivingEntity, T> addObject,
                                                        BiConsumer<LivingEntity, T> removeObject,
                                                        BiPredicate<T, T> equalCondition,
                                                        StreamCodec<? super RegistryFriendlyByteBuf, T> objectStreamCodec) {
        BuffObject<T> object = new BuffObject<>(name, addObject, removeObject, equalCondition, objectStreamCodec);
        REGISTERED_OBJECTS.put(name, object);
        return object;
    }

    public void addBuff(Entity source, LivingEntity livingEntity) {
        if (this.buffObject != null)
            this.buffObject.addObject().accept(source, livingEntity, this.object);
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
        } else if (!(obj instanceof SkillBuff<?> skillBuff)) {
            return false;
        } else {
            return this.isSkill(skillBuff.skill) && this.isOfType(skillBuff) && this.buffObject.equalCondition.test(this.object, (T) skillBuff.object);
        }
    }

    private boolean isOfType(SkillBuff<?> buff) {
        return this.object.getClass().equals(buff.object.getClass());
    }

    @Override
    public String toString() {
        return "SkillBuff: [" + "Skill: " + this.skill + ", Type: " + this.buffObject.name + ", Category: " + this.category + ", Buff: " + this.object + "]";
    }

    public record BuffObject<T>(
            String name,
            TriConsumer<Entity, LivingEntity, T> addObject,
            BiConsumer<LivingEntity, T> removeObject,
            BiPredicate<T, T> equalCondition,
            StreamCodec<? super RegistryFriendlyByteBuf, T> objectStreamCodec) {

        public static final StreamCodec<ByteBuf, BuffObject<?>> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
                .map(BuffObject::parse, BuffObject::name);

        @SuppressWarnings("unchecked")
        private static <T> BuffObject<T> parse(String name) {
            return (BuffObject<T>) REGISTERED_OBJECTS.get(name);
        }
    }
}