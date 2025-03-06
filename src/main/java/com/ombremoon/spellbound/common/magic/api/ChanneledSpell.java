package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.events.EventFactory;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiPredicate;
import java.util.function.Function;

public abstract class ChanneledSpell extends AnimatedSpell {
    protected int manaTickCost;

    public static <T extends ChanneledSpell> Builder<T> createChannelledSpellBuilder(Class<T> spellClass) {
        return new Builder<>();
    }

    public ChanneledSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, EventFactory.getChanneledBuilder(spellType, builder));
        this.manaTickCost = builder.manaTickCost;
    }

    public int getManaTickCost() {
        return this.manaTickCost;
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        LivingEntity caster = context.getCaster();
        var handler = SpellUtil.getSpellHandler(caster);
        handler.setChargingOrChannelling(true);

        if (context.getLevel().isClientSide) {
            //Play Channel Anim
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity caster = context.getCaster();
        var handler = SpellUtil.getSpellHandler(caster);
        if ((this.ticks % 20 == 0 && !handler.consumeMana(this.manaTickCost, true)) || !handler.isChargingOrChannelling()) {
            this.endSpell();
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        LivingEntity caster = context.getCaster();
        var handler = SpellUtil.getSpellHandler(caster);
        handler.setChargingOrChannelling(false);
        //Stop Channel Anim
    }

    public static class Builder<T extends ChanneledSpell> extends AnimatedSpell.Builder<T> {
        protected int manaTickCost;
        protected String channelAnimation;

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

        public Builder<T> castAnimation(Function<SpellContext, String> castAnimation) {
            this.castAnimation = castAnimation;
            return this;
        }

        public Builder<T> channelAnimation(String channelAnimation) {
            this.channelAnimation = channelAnimation;
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
    }
}
