package com.ombremoon.spellbound.common.magic.api.buff.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public class PlayerAttackEvent extends SpellEvent {
    private final Player player;
    private final AttackEntityEvent event;

    public PlayerAttackEvent(Player player, AttackEntityEvent event) {
        super(player, event);
        this.player = player;
        this.event = event;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Entity getTarget() {
        return this.event.getTarget();
    }
}
