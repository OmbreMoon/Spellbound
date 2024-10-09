package com.ombremoon.spellbound.common.magic.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public class PlayerAttackEvent extends SpellEvent {
    private final AttackEntityEvent event;

    public PlayerAttackEvent(Player player, AttackEntityEvent event) {
        super(player, event);
        this.event = event;
    }

    public Entity getTarget() {
        return this.event.getTarget();
    }
}
