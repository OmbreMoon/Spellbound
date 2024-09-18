package com.ombremoon.spellbound.common.magic.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class ChangeTargetEvent extends SpellEvent {
    public ChangeTargetEvent(Player player, LivingEvent event) {
        super(player, event);
    }

    public LivingChangeTargetEvent getTargetEvent() {
        return (LivingChangeTargetEvent) getEvent();
    }
}
