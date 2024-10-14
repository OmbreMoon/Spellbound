package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiPredicate;

public abstract class ChanneledSpell extends AnimatedSpell {
    protected int manaTickCost;

    public static <T extends ChanneledSpell> Builder<T> createChannelledSpellBuilder(Class<T> spellClass) {
        return new Builder<>();
    }

    public ChanneledSpell(SpellType<?> spellType, Builder<?> builder) {
        super(spellType, builder);
        this.manaTickCost = builder.manaTickCost;
    }

    public int getManaTickCost() {
        return this.manaTickCost;
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Player player = context.getPlayer();
        var handler = SpellUtil.getSpellHandler(player);
        handler.setChannelling(true);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Player player = context.getPlayer();
        var handler = SpellUtil.getSpellHandler(player);
        if ((this.ticks % 20 == 0 && !handler.consumeMana(this.manaTickCost, true)) || !handler.isChannelling()) {
            this.endSpell();
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        Player player = context.getPlayer();
        var handler = SpellUtil.getSpellHandler(player);
        handler.setChannelling(false);
    }

    public static class Builder<T extends ChanneledSpell> extends AnimatedSpell.Builder<T> {
        protected int manaTickCost;

        public Builder() {
            this.castType = CastType.CHANNEL;
        }

        public Builder<T> manaCost(int manaCost) {
            this.manaCost = manaCost;
            return this;
        }

        public Builder<T> setManaTickCost(int fpTickCost) {
            this.manaTickCost = fpTickCost;
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

        public Builder<T> castSound(SoundEvent castSound) {
            this.castSound = castSound;
            return this;
        }

        public Builder<T> updateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }
    }
}
