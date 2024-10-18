package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiPredicate;

/**
 * The main class most spell will extend from. Main utility is to handle spell casting animations.
 */
public abstract class AnimatedSpell extends AbstractSpell {
    private final String castAnimation;

    public static <T extends AnimatedSpell> Builder<T> createSimpleSpellBuilder(Class<T> spellClass) {
        return new Builder<>();
    }

    public AnimatedSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, builder);
        this.castAnimation = builder.castAnimation;
    }

    @Override
    public void onCastStart(SpellContext context) {
        super.onCastStart(context);
        //WILL THIS BE AN ISSUE DOWN THE LINE??
        if (context.getLevel().isClientSide && !this.castAnimation.isEmpty() && context.getSpellHandler().castTick == 1 && context.getCaster() instanceof Player player)
            playAnimation(player, this.castAnimation);

    }

    @Override
    public void onCastReset(SpellContext context) {
        super.onCastReset(context);
        if (context.getLevel().isClientSide && !this.castAnimation.isEmpty() && context.getCaster() instanceof Player player)
            stopAnimation(player);
    }

    public static class Builder<T extends AnimatedSpell> extends AbstractSpell.Builder<T> {
        protected String castAnimation = "";

        public Builder<T> manaCost(int manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        public Builder<T> castTime(int castTime) {
            this.castTime = castTime;
            return this;
        }

        public Builder<T> castAnimation(String castAnimationName) {
            this.castAnimation = castAnimationName;
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
