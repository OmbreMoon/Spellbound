package com.ombremoon.spellbound.common.magic.events;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class PlayerDamageEvent {

    public static class Pre extends SpellEvent {
        public Pre(Player player, LivingEvent event) {
            super(player, event);
        }

        public LivingDamageEvent.Pre getDamageEvent() {
            return (LivingDamageEvent.Pre) getEvent();
        }
    }

    public static class Post extends SpellEvent {
        public Post(Player player, LivingEvent event) {
            super(player, event);
        }

        public LivingDamageEvent.Post getDamageEvent() {
            return (LivingDamageEvent.Post) getEvent();
        }
    }

}
