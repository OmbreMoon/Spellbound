package com.ombremoon.spellbound.common.events;

import com.ombremoon.spellbound.common.events.custom.BuildSpellEvent;
import com.ombremoon.spellbound.common.events.custom.MobEffectEvent;
import com.ombremoon.spellbound.common.events.custom.SpellCastEvent;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;

public class EventFactory {

    public static boolean onEffectRemoved(LivingEntity entity, MobEffectInstance effectInstance) {
        return NeoForge.EVENT_BUS.post(new MobEffectEvent.Remove(entity, effectInstance)).isCanceled();
    }

    public static AnimatedSpell.Builder<?> getAnimatedBuilder(SpellType<?> spellType, AnimatedSpell.Builder<?> builder) {
        BuildSpellEvent.Animated event = new BuildSpellEvent.Animated(spellType, builder);
        NeoForge.EVENT_BUS.post(event);
        return event.getBuilder();
    }

    public static ChanneledSpell.Builder<?> getChanneledBuilder(SpellType<?> spellType, ChanneledSpell.Builder<?> builder) {
        BuildSpellEvent.Channeled event = new BuildSpellEvent.Channeled(spellType, builder);
        NeoForge.EVENT_BUS.post(event);
        return event.getBuilder();
    }

    public static void onSpellCast(LivingEntity caster, AbstractSpell spell, SpellContext context) {
        NeoForge.EVENT_BUS.post(new SpellCastEvent(caster, spell, context));
    }
}
