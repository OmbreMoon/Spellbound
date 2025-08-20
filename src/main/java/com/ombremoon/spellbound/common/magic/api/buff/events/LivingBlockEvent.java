package com.ombremoon.spellbound.common.magic.api.buff.events;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

public class LivingBlockEvent extends SpellEvent {
    private final LivingShieldBlockEvent shieldBlockEvent;
    private final DamageContainer container;

    public LivingBlockEvent(LivingEntity caster, LivingShieldBlockEvent shieldBlockEvent) {
        super(caster, shieldBlockEvent);
        this.shieldBlockEvent = shieldBlockEvent;
        this.container = this.shieldBlockEvent.getDamageContainer();
    }

    public DamageContainer getContainer() {
        return this.container;
    }

    public DamageSource getSource() {
        return this.container.getSource();
    }

    public float getOriginalDamage() {
        return this.container.getOriginalDamage();
    }

    public float getBlockedDamage() {
        return this.container.getBlockedDamage();
    }

    public float shieldDamage() {
        return this.shieldBlockEvent.shieldDamage();
    }

    public void setBlockedDamage(float blocked) {
        this.shieldBlockEvent.setBlockedDamage(blocked);
    }

    public void setShieldDamage(float damage) {
        this.shieldBlockEvent.setShieldDamage(damage);
    }

    public boolean wasOriginallyBlocked() {
        return this.shieldBlockEvent.getOriginalBlock();
    }

    public boolean getBlocked() {
        return this.shieldBlockEvent.getBlocked();
    }

    public void setBlocked(boolean isBlocked) {
        this.shieldBlockEvent.setBlocked(isBlocked);
    }
}
