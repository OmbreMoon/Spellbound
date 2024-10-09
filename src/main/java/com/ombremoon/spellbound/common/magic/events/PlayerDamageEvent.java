package com.ombremoon.spellbound.common.magic.events;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class PlayerDamageEvent extends SpellEvent {

    public PlayerDamageEvent(Player player, LivingEvent event) {
        super(player, event);
    }

    public static class Pre extends PlayerDamageEvent {
        private final LivingDamageEvent.Pre event;
        private final DamageContainer container;

        public Pre(Player player, LivingDamageEvent.Pre event) {
            super(player, event);
            this.event = event;
            this.container = this.event.getContainer();
        }

        public LivingEntity getEntity() {
            return this.event.getEntity();
        }

        public DamageContainer getContainer() {
            return this.container;
        }

        public DamageSource getSource() {
            return this.container.getSource();
        }

        public float getNewDamage() {
            return this.container.getNewDamage();
        }

        public float getOriginalDamage() {
            return this.container.getOriginalDamage();
        }

        public void setNewDamage(float newDamage) {
            this.container.setNewDamage(newDamage);
        }
    }

    public static class Post extends PlayerDamageEvent {
        private final LivingDamageEvent.Post event;

        public Post(Player player, LivingDamageEvent.Post event) {
            super(player, event);
            this.event = event;
        }

        public LivingEntity getEntity() {
            return this.event.getEntity();
        }

        public float getOriginalDamage() {
            return this.event.getOriginalDamage();
        }

        public DamageSource getSource() {
            return this.event.getSource();
        }

        public float getNewDamage() {
            return this.event.getNewDamage();
        }

        public float getBlockedDamage() {
            return this.event.getBlockedDamage();
        }

        public float getShieldDamage() {
            return this.event.getShieldDamage();
        }

        public int getInvulnerabilityTicks() {
            return this.event.getPostAttackInvulnerabilityTicks();
        }

        public float getReduction(DamageContainer.Reduction reduction) {
            return this.event.getReduction(reduction);
        }
    }
}
