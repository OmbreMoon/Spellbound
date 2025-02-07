package com.ombremoon.spellbound.common.magic.api.buff;

import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class SkillBuff<T> {
    public static final BuffObject<MobEffectInstance> MOB_EFFECT = new BuffObject<>(LivingEntity::addEffect, (livingEntity, mobEffectInstance) -> livingEntity.removeEffect(mobEffectInstance.getEffect()));
    public static final BuffObject<ModifierData> ATTRIBUTE_MODIFIER = new BuffObject<>((livingEntity, modifierData) -> {
        var instance = livingEntity.getAttribute(modifierData.attribute());
        if (instance != null && !instance.hasModifier(modifierData.attributeModifier().id()))
            instance.addTransientModifier(modifierData.attributeModifier());
    }, (livingEntity, modifierData) -> {
        var instance = livingEntity.getAttribute(modifierData.attribute());
        if (instance != null && instance.hasModifier(modifierData.attributeModifier().id()))
            instance.removeModifier(modifierData.attributeModifier());
    });
    public static final BuffObject<SpellModifier> SPELL_MODIFIER = new BuffObject<>((livingEntity, spellModifier) -> {
        var skills = SpellUtil.getSkillHolder(livingEntity);
        skills.addModifierWithExpiry(spellModifier);
    }, (livingEntity, spellModifier) -> {
        var skills = SpellUtil.getSkillHolder(livingEntity);
        skills.removeModifier(spellModifier);
    });
    public static final BuffObject<ResourceLocation> EVENT = new BuffObject<>((livingEntity, resourceLocation) -> {}, (livingEntity, resourceLocation) -> {
        var handler = SpellUtil.getSpellHandler(livingEntity);
        handler.getListener().removeListener(resourceLocation);
    });

    private final Skill skill;
    private final BuffCategory category;
    private final BuffObject<T> buffObject;
    private final T object;

    public SkillBuff(Skill skill, BuffCategory category, @Nullable BuffObject<T> buffObject, @Nullable T object) {
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

    public boolean is(SkillBuff<?> skillBuff) {
        return this.skill.equals(skillBuff.skill) && this.object.equals(skillBuff.object);
    }

    public boolean isSkill(Skill skill) {
        return this.skill.equals(skill);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            return obj instanceof SkillBuff<?> skillBuff && this.skill == skillBuff.getSkill()
                    && this.category == skillBuff.category
                    && this.buffObject == skillBuff.buffObject
                    && this.object == skillBuff.object;
        }
    }

    @Override
    public String toString() {
        return "SkillBuff: [" + "Skill: " + this.skill + ", Category: " + this.category + ", Buff: " + this.object + "]";
    }

    public record BuffObject<T>(BiConsumer<LivingEntity, T> addObject, BiConsumer<LivingEntity, T> removeObject) {}
}