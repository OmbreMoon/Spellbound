package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.events.SpellEvent;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.BiConsumer;

public class SkillBuff {
    public static final BuffObject<MobEffectInstance> MOB_EFFECT = new BuffObject<>((livingEntity, mobEffectInstance, duration) -> livingEntity.addEffect(mobEffectInstance), (livingEntity, mobEffectInstance) -> livingEntity.removeEffect(mobEffectInstance.getEffect()));
    public static final BuffObject<SpellHandler.ModifierData> ATTRIBUTE_MODIFIER = new BuffObject<>((livingEntity, modifierData, integer) -> {
        var instance = livingEntity.getAttribute(modifierData.attribute());
        if (instance != null)
            instance.addTransientModifier(modifierData.attributeModifier());
    }, (livingEntity, modifierData) -> {
        var instance = livingEntity.getAttribute(modifierData.attribute());
        if (instance != null && instance.hasModifier(modifierData.attributeModifier().id()))
            instance.removeModifier(modifierData.attributeModifier());
    });
    public static final BuffObject<SpellModifier> SPELL_MODIFIER = new BuffObject<>((livingEntity, spellModifier, integer) -> {
        var skills = SpellUtil.getSkillHolder(livingEntity);
        skills.addModifierWithExpiry1(spellModifier);
    }, (livingEntity, spellModifier) -> {
        var skills = SpellUtil.getSkillHolder(livingEntity);
        skills.removeModifier(spellModifier);
    });
    public static final BuffObject<SpellEventListener.EventInstance<SpellEvent>> EVENT = new BuffObject<>((livingEntity, eventInstance, integer) -> {
        var handler = SpellUtil.getSpellHandler(livingEntity);
        handler.getListener().addListener(eventInstance.event(), eventInstance.location(), eventInstance.spellConsumer());
    }, (livingEntity, spellEventEventInstance) -> {
        var handler = SpellUtil.getSpellHandler(livingEntity);
        handler.getListener().removeListener(spellEventEventInstance.event(), spellEventEventInstance.location());
    });
    private final Skill skill;
    private final BuffCategory category;
    private final BuffObject<?> buffObject;

    public SkillBuff(Skill skill, BuffCategory category, BuffObject<?> buffObject) {
        this.skill = skill;
        this.category = category;
        this.buffObject = buffObject;
    }

    public <T> void addBuff(LivingEntity livingEntity, BuffCategory category, BuffObject<T> buffObject, Object object, int duration) {
//        this.buffObject.addObject.accept(livingEntity, object, duration);
    }

    public void removeBuff() {

    }

    public record BuffObject<T>(TriConsumer<LivingEntity, T, Integer> addObject, BiConsumer<LivingEntity, T> removeObject) {}
}
