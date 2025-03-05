package com.ombremoon.spellbound.common.events.custom;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;

public abstract class MobEffectEvent extends LivingEvent {
    @Nullable
    protected final MobEffectInstance effectInstance;

    public MobEffectEvent(LivingEntity living, @Nullable MobEffectInstance effectInstance) {
        super(living);
        this.effectInstance = effectInstance;
    }

    public @Nullable MobEffectInstance getEffectInstance() {
        return this.effectInstance;
    }

    public static class Remove extends MobEffectEvent implements ICancellableEvent {
        private final Holder<MobEffect> effect;

        public Remove(LivingEntity living, MobEffectInstance effectInstance) {
            super(living, effectInstance);
            this.effect = effectInstance.getEffect();
        }

        public Holder<MobEffect> getEffect() {
            return this.effect;
        }
    }

    public static class Added extends net.neoforged.neoforge.event.entity.living.MobEffectEvent {
        private final MobEffectInstance oldEffectInstance;
        private final Entity source;

        public Added(LivingEntity living, MobEffectInstance oldEffectInstance, MobEffectInstance newEffectInstance, Entity source) {
            super(living, newEffectInstance);
            this.oldEffectInstance = oldEffectInstance;
            this.source = source;
        }

        /**
         * @return the added {@link MobEffectInstance}. This is the unmerged MobEffectInstance if the old MobEffectInstance is not null.
         */
        @Override
        public MobEffectInstance getEffectInstance() {
            return super.getEffectInstance();
        }

        /**
         * @return the old {@link MobEffectInstance}. This can be null if the entity did not have an effect of this kind before.
         */
        @Nullable
        public MobEffectInstance getOldEffectInstance() {
            return oldEffectInstance;
        }

        /**
         * @return the entity source of the effect, or {@code null} if none exists
         */
        @Nullable
        public Entity getEffectSource() {
            return source;
        }
    }
}
