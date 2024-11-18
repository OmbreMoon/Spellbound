package com.ombremoon.spellbound.common.magic.api.buff.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.InputEvent;

public abstract class MouseInputEvent extends SpellEvent {
    private final InputEvent.MouseButton event;

    public MouseInputEvent(Player player, InputEvent.MouseButton event) {
        super(player, event);
        this.event = event;
    }

    public int getButton() {
        return this.event.getButton();
    }

    public int getAction() {
        return this.event.getAction();
    }

    public int getModifiers() {
        return this.event.getModifiers();
    }

    public static class Pre extends MouseInputEvent {
        public Pre(Player player, InputEvent.MouseButton.Pre event) {
            super(player, event);
        }
    }

    public static class Post extends MouseInputEvent {
        public Post(Player player, InputEvent.MouseButton.Post event) {
            super(player, event);
        }
    }
}
