package com.ombremoon.spellbound.common.magic.api.buff.events;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class ChangeTargetEvent extends SpellEvent {
    public ChangeTargetEvent(LivingEntity caster, LivingEvent event) {
        super(caster, event);
    }

    public LivingChangeTargetEvent getTargetEvent() {
        return (LivingChangeTargetEvent) getEvent();
    }
}
