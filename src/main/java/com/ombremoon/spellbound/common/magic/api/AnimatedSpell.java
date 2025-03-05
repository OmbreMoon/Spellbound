package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.events.EventFactory;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellMastery;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The main class most spells will extend from. Primary utility is to handle spells casting animations.
 */
public abstract class AnimatedSpell extends AbstractSpell {
    private final Function<SpellContext, String> castAnimation;

    public static <T extends AnimatedSpell> Builder<T> createSimpleSpellBuilder(Class<T> spellClass) {
        return new Builder<>();
    }

    public AnimatedSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, EventFactory.getAnimatedBuilder(spellType, builder));
        this.castAnimation = builder.castAnimation;
    }

    @Override
    public void onCastStart(SpellContext context) {
        super.onCastStart(context);
        String animation = this.castAnimation.apply(context);
        if (!context.getLevel().isClientSide && !animation.isEmpty() && context.getCaster() instanceof Player player)
            playAnimation(player, animation);

    }

    @Override
    public void onCastReset(SpellContext context) {
        super.onCastReset(context);
        String animation = this.castAnimation.apply(context);
        if (context.getLevel().isClientSide && !animation.isEmpty() && context.getCaster() instanceof Player player)
            //Fade to fail animation
            stopAnimation(player);
    }

    public static class Builder<T extends AnimatedSpell> extends AbstractSpell.Builder<T> {
        protected Function<SpellContext, String> castAnimation = context -> "";
        protected Function<SpellContext, String> failAnimation = context -> "";

        public Builder<T> mastery(SpellMastery mastery) {
            this.spellMastery = mastery;
            return this;
        }

        public Builder<T> manaCost(int manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        public Builder<T> duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder<T> baseDamage(int baseDamage) {
            this.baseDamage = baseDamage;
            return this;
        }

        public Builder<T> xpModifier(float modifier) {
            this.xpModifier = modifier;
            return this;
        }

        public Builder<T> castTime(int castTime) {
            this.castTime = castTime;
            return this;
        }

        public Builder<T> castAnimation(Function<SpellContext, String> castAnimationName) {
            this.castAnimation = castAnimationName;
            return this;
        }

        public Builder<T> failAnimation(Function<SpellContext, String> failAnimationName) {
            this.failAnimation = failAnimationName;
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

        public Builder<T> castType(CastType castType) {
            this.castType = castType;
            return this;
        }

        public Builder<T> castSound(SoundEvent castSound) {
            this.castSound = castSound;
            return this;
        }

        public Builder<T> fullRecast() {
            this.fullRecast = true;
            return this;
        }
        public Builder<T> skipEndOnRecast(Predicate<SpellContext> skipIf) {
            this.skipEndOnRecast = skipIf;
            return this;
        }

        public Builder<T> skipEndOnRecast() {
            this.skipEndOnRecast = context -> true;
            return this;
        }

        public Builder<T> updateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }
        public Builder<T> hasLayer() {
            this.hasLayer = true;
            return this;
        }
    }
}
