package com.ombremoon.spellbound.common.magic.events;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class DeathEvent extends SpellEvent {
    public DeathEvent(LivingEntity caster, LivingEvent event) {
        super(caster, event);
    }

    public LivingDeathEvent getDeathEvent() {
        return (LivingDeathEvent) getEvent();
    }
}
