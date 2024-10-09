package com.ombremoon.spellbound.common.magic.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class SpellEvent {
    private final Player player;
    private final LivingEvent livingEvent;
    protected boolean cancelled;

    public SpellEvent(Player player, LivingEvent event) {
        this.player = player;
        this.livingEvent = event;
    }

    public Player getPlayer() {
        return this.player;
    }

    protected LivingEvent getEvent() {
        return this.livingEvent;
    }

    private boolean canCancel() {
        return this.livingEvent instanceof ICancellableEvent;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void cancelEvent() {
        if (!canCancel()) return;
        ((ICancellableEvent)this.livingEvent).setCanceled(true);
        this.cancelled = true;
    }
}
