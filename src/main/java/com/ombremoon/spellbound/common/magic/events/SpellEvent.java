package com.ombremoon.spellbound.common.magic.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class SpellEvent {
    private final LivingEntity caster;
    private final Event event;
    protected boolean cancelled;

    public SpellEvent(LivingEntity caster, Event event) {
        this.caster = caster;
        this.event = event;
    }

    public LivingEntity getCaster() {
        return this.caster;
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
