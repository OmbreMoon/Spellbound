package com.ombremoon.spellbound.common.magic.api.buff.events;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class JumpEvent extends SpellEvent {
    public JumpEvent(LivingEntity caster, LivingEvent.LivingJumpEvent event) {
        super(caster, event);
    }

    public LivingEvent.LivingJumpEvent getJumpEvent() {
        return (LivingEvent.LivingJumpEvent) this.getEvent();
    }
}
