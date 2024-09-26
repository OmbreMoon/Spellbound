package com.ombremoon.spellbound.common.magic.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class PlayerKillEvent extends SpellEvent {
    public PlayerKillEvent(Player player, LivingEvent event) {
        super(player, event);
    }

    public LivingDeathEvent getDeathEvent() {
        return (LivingDeathEvent) getEvent();
    }
}
