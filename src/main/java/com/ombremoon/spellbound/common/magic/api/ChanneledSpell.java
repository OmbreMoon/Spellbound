package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.client.KeyBinds;
import com.ombremoon.spellbound.common.events.EventFactory;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class ChanneledSpell extends AnimatedSpell {
    protected int manaTickCost;
    protected Function<SpellContext, String> channelAnimation;
    protected String channelStopAnimation;

    public static <T extends ChanneledSpell> Builder<T> createChannelledSpellBuilder(Class<T> spellClass) {
        return new Builder<>();
    }

    public ChanneledSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, EventFactory.getChanneledBuilder(spellType, builder));
        this.manaTickCost = builder.manaTickCost;
        this.channelAnimation = builder.channelAnimation;
        this.channelStopAnimation = builder.channelStopAnimation;
    }

    public int getManaTickCost() {
        return this.manaTickCost;
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        LivingEntity caster = context.getCaster();
        var handler = SpellUtil.getSpellCaster(caster);
        handler.setChargingOrChannelling(true);

        String animation = this.channelAnimation.apply(context);
        if (!animation.isEmpty() && context.getCaster() instanceof Player player)
            playAnimation(player, animation);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity caster = context.getCaster();
        var handler = SpellUtil.getSpellCaster(caster);
        if (!caster.level().isClientSide) {
            if ((this.tickCount > 0 && this.tickCount % 20 == 0 && !handler.consumeMana(this.manaTickCost, true)) || !handler.isChargingOrChannelling()) {
                this.endSpell();
            }
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        LivingEntity caster = context.getCaster();
        var handler = SpellUtil.getSpellCaster(caster);
        handler.setChargingOrChannelling(false);
        if (!caster.level().isClientSide) {
            if (context.getCaster() instanceof Player player) {
                if (!this.channelStopAnimation.isEmpty()) {
                    playAnimation(player, this.channelStopAnimation);
                } else {
//                    stopAnimation(player);
                }
            }
        } else {
            KeyBinds.getSpellCastMapping().setDown(false);
        }
    }

    public static class Builder<T extends ChanneledSpell> extends AnimatedSpell.Builder<T> {
        protected int manaTickCost;
        protected Function<SpellContext, String> channelAnimation;
        protected String channelStopAnimation;

        public Builder() {
            this.castType = CastType.CHANNEL;
        }

        public Builder<T> mastery(SpellMastery mastery) {
            this.spellMastery = mastery;
            return this;
        }

        public Builder<T> manaCost(int manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        public Builder<T> manaTickCost(int fpTickCost) {
            this.manaTickCost = fpTickCost;
            return this;
        }

        public Builder<T> baseDamage(float baseDamage) {
            this.baseDamage = baseDamage;
            return this;
        }

        public Builder<T> xpModifier(float modifier) {
            this.xpModifier = modifier;
            return this;
        }

        public Builder<T> castTime(int castTime, int stationaryTicks) {
            this.castTime = castTime;
            this.stationaryTicks = stationaryTicks;
            return this;
        }

        public Builder<T> castTime(int castTime) {
            this.castTime = castTime;
            this.stationaryTicks = castTime;
            return this;
        }

        public Builder<T> castAnimation(Function<SpellContext, String> castAnimation) {
            this.castAnimation = castAnimation;
            return this;
        }

        public Builder<T> channelAnimation(Function<SpellContext, String> channelAnimation) {
            this.channelAnimation = channelAnimation;
            return this;
        }

        public Builder<T> stopChannelAnimation(String channelStopAnimation) {
            this.channelStopAnimation = channelStopAnimation;
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

        public Builder<T> castSound(SoundEvent castSound) {
            this.castSound = castSound;
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

        public Builder<T> negativeScaling(Predicate<SpellContext> negativeScaling) {
            this.negativeScaling = negativeScaling;
            return this;
        }

        public Builder<T> negativeScaling() {
            this.negativeScaling = context -> true;
            return this;
        }
    }
}
