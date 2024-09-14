package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public abstract class AnimatedSpell extends AbstractSpell {

    public static Builder<AnimatedSpell> createSimpleSpellBuilder() {
        return new Builder<>();
    }

    public AnimatedSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, builder);
    }

    @Override
    protected void onSpellStart(SpellContext context) {

    }

    public static class Builder<T extends AnimatedSpell> extends AbstractSpell.Builder<T> {

        public Builder() {
        }

        public Builder<T> setManaCost(int fpCost) {
            this.manaCost = fpCost;
            return this;
        }

        public Builder<T> setCastTime(int castTime) {
            this.castTime = castTime;
            return this;
        }

        public Builder<T> setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder<T> setCastType(CastType castType) {
            this.castType = castType;
            return this;
        }

        public Builder<T> setCastSound(SoundEvent castSound) {
            this.castSound = castSound;
            return this;
        }
    }
}
