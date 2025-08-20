package com.ombremoon.spellbound.common.events.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class MobInteractEvent extends PlayerEvent {
    private final InteractionHand hand;
    private final Mob mob;

    public MobInteractEvent(Player player, InteractionHand hand, Mob mob) {
        super(player);
        this.hand = hand;
        this.mob = mob;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public Mob getMob() {
        return this.mob;
    }

    public static class Pre extends MobInteractEvent implements ICancellableEvent {
        private InteractionResult result = InteractionResult.PASS;

        public Pre(Player player, InteractionHand hand, Mob mob) {
            super(player, hand, mob);
        }

        public InteractionResult getResult() {
            return this.result;
        }

        public void setResult(InteractionResult result) {
            this.result = result;
        }
    }

    public static class Post extends MobInteractEvent implements ICancellableEvent {
        private InteractionResult result = InteractionResult.PASS;

        public Post(Player player, InteractionHand hand, Mob mob) {
            super(player, hand, mob);
        }

        public InteractionResult getResult() {
            return this.result;
        }

        public void setResult(InteractionResult result) {
            this.result = result;
        }
    }
}
