package com.ombremoon.spellbound.common.magic.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
