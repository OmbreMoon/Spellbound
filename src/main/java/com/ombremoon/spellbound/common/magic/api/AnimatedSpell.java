package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.sounds.SoundEvent;

import java.util.function.BiPredicate;

/**
 * The main class most spell will extend from. Main utility is to handle spell casting animations.
 */
public abstract class AnimatedSpell extends AbstractSpell {

    public static <T extends AnimatedSpell> Builder<T> createSimpleSpellBuilder(Class<T> spellClass) {
        return new Builder<>();
    }

    public AnimatedSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, builder);
    }

    @Override
    protected void onSpellStart(SpellContext context) {

    }

    public static class Builder<T extends AnimatedSpell> extends AbstractSpell.Builder<T> {
        public Builder<T> manaCost(int manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        public Builder<T> castTime(int castTime) {
            this.castTime = castTime;
            return this;
        }

        public Builder<T> castCondition(BiPredicate<SpellContext, T> castCondition) {
            this.castPredicate = castCondition;
            return this;
        }

        public Builder<T> additionalCondition(BiPredicate<SpellContext, T> castCondition) {
            this.castPredicate = this.castPredicate.and(castCondition);
            return this;
        }

        public Builder<T> duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder<T> castType(CastType castType) {
            this.castType = castType;
            return this;
        }

        public Builder<T> castSound(SoundEvent castSound) {
            this.castSound = castSound;
            return this;
        }

        public Builder<T> partialRecast() {
            this.partialRecast = true;
            this.fullRecast = false;
            return this;
        }

        public Builder<T> fullRecast() {
            this.fullRecast = true;
            this.partialRecast = false;
            return this;
        }

        public Builder<T> skipEndOnRecast() {
            this.skipEndOnRecast = true;
            return this;
        }

        public Builder<T> updateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }
    }
}
