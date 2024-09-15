package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public abstract class ChanneledSpell extends AnimatedSpell {
    protected int manaTickCost;

    public static Builder<ChanneledSpell> createChannelledSpellBuilder() {
        return new Builder<>();
    }

    public ChanneledSpell(SpellType<?> spellType, Builder<ChanneledSpell> builder) {
        super(spellType, builder);
        this.manaTickCost = builder.manaTickCost;
    }

    public int getManaTickCost() {
        return this.manaTickCost;
    }

    @Override
    protected void onSpellStart(LivingEntity caster, Level level, BlockPos blockPos) {
        super.onSpellStart(caster, level, blockPos);
        var handler = caster.getData(DataInit.SPELL_HANDLER.get());
        handler.setChannelling(true);
        PayloadHandler.syncSpellsToClient((Player) caster);
    }

    @Override
    protected void onSpellTick(LivingEntity caster, Level level, BlockPos blockPos) {
        super.onSpellTick(caster, level, blockPos);
        var handler = SpellUtil.getSpellHandler(caster);
        if (!handler.consumeMana(this.manaTickCost, true) || !handler.isChannelling()) {
            this.endSpell();
        }
    }

    @Override
    protected void onSpellStop(LivingEntity caster, Level level, BlockPos blockPos) {
        super.onSpellStop(caster, level, blockPos);
        var handler = SpellUtil.getSpellHandler(caster);
        handler.setChannelling(false);
        PayloadHandler.syncSpellsToClient((Player) caster);
    }

    public static class Builder<T extends ChanneledSpell> extends AnimatedSpell.Builder<T> {
        protected int manaTickCost;

        public Builder() {
            this.castType = CastType.CHANNEL;
        }

        public Builder<T> setManaCost(int fpCost) {
            this.manaCost = fpCost;
            return this;
        }

        public Builder<T> setManaTickCost(int fpTickCost) {
            this.manaTickCost = fpTickCost;
            return this;
        }

        public Builder<T> setCastTime(int castTime) {
            this.castTime = castTime;
            return this;
        }

        public Builder<T> setCastSound(SoundEvent castSound) {
            this.castSound = castSound;
            return this;
        }
    }
}
