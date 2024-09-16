package com.ombremoon.spellbound.common.magic.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class PlayerJumpEvent extends SpellEvent {
    public PlayerJumpEvent(Player player, LivingEvent.LivingJumpEvent event) {
        super(player, event);
    }

    public LivingEvent.LivingJumpEvent getJumpEvent() {
        return (LivingEvent.LivingJumpEvent) this.getEvent();
    }
}
