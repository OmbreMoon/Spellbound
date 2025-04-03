package com.ombremoon.spellbound.common.magic.api.buff;

import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class SkillBuff<T> {
    public static final BuffObject<MobEffectInstance> MOB_EFFECT = new BuffObject<>(
            LivingEntity::addEffect,
            (livingEntity, mobEffectInstance) -> livingEntity.removeEffect(mobEffectInstance.getEffect()),
            (o, o1) -> o instanceof MobEffectInstance instance && o1 instanceof MobEffectInstance instance1 && instance.is(instance1.getEffect()));

    public static final BuffObject<ModifierData> ATTRIBUTE_MODIFIER = new BuffObject<>(
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
            (o, o1) -> o instanceof ModifierData data && o1 instanceof ModifierData data1 && data.attributeModifier().is(data1.attributeModifier().id()));

    public static final BuffObject<SpellModifier> SPELL_MODIFIER = new BuffObject<>(
            (livingEntity, spellModifier) -> {
                var skills = SpellUtil.getSkillHolder(livingEntity);
                skills.addModifierWithExpiry(spellModifier);
            },
            (livingEntity, spellModifier) -> {
                var skills = SpellUtil.getSkillHolder(livingEntity);
                skills.removeModifier(spellModifier);
            },
            (o, o1) -> o instanceof SpellModifier modifier && o1 instanceof SpellModifier modifier1 && modifier.equals(modifier1));

    public static final BuffObject<ResourceLocation> EVENT = new BuffObject<>(
            (livingEntity, resourceLocation) -> {},
            (livingEntity, resourceLocation) -> {
                var handler = SpellUtil.getSpellHandler(livingEntity);
                handler.getListener().removeListener(resourceLocation);
            },
            (o, o1) -> o instanceof ResourceLocation location && o1 instanceof ResourceLocation location1 && location.equals(location1));

    private final Skill skill;
    private final BuffCategory category;
    private final BuffObject<T> buffObject;
    private final T object;

    public SkillBuff(Skill skill, BuffCategory category, BuffObject<T> buffObject, T object) {
        this.skill = skill;
        this.category = category;
        this.buffObject = buffObject;
        this.object = object;
    }

    public void addBuff(LivingEntity livingEntity) {
        if (this.buffObject != null)
            this.buffObject.addObject().accept(livingEntity, this.object);
    }

    public void removeBuff(LivingEntity livingEntity) {
        if (this.buffObject != null)
            this.buffObject.removeObject().accept(livingEntity, this.object);
    }

    public Skill getSkill() {
        return this.skill;
    }

    public BuffCategory category() {
        return this.category;
    }

    public BuffObject<T> getBuffObject() {
        return this.buffObject;
    }

    public boolean isSkill(Skill skill) {
        return this.skill.equals(skill);
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

    public record BuffObject<T>(BiConsumer<LivingEntity, T> addObject, BiConsumer<LivingEntity, T> removeObject, BiPredicate<Object, Object> equalCondition) {}
}