package com.ombremoon.spellbound.common.events.custom;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class UnregisterDimensionEvent extends Event implements ICancellableEvent {
    private final ServerLevel level;

    public UnregisterDimensionEvent(ServerLevel level) {
        this.level = level;
    }

    public ServerLevel getLevel() {
        return this.level;
    }
}