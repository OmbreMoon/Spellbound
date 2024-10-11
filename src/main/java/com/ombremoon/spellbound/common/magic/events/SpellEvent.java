package com.ombremoon.spellbound.common.magic.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class SpellEvent {
    private final Player player;
    private final Event event;
    protected boolean cancelled;

    public SpellEvent(Player player, Event event) {
        this.player = player;
        this.event = event;
    }

    public Player getPlayer() {
        return this.player;
    }

    protected Event getEvent() {
        return this.event;
    }

    private boolean canCancel() {
        return this.event instanceof ICancellableEvent;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void cancelEvent() {
        if (!canCancel()) return;
        ((ICancellableEvent)this.event).setCanceled(true);
        this.cancelled = true;
    }
}
