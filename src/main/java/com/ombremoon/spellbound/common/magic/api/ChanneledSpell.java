package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

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
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Player player = context.getPlayer();
        var handler = player.getData(DataInit.SPELL_HANDLER.get());
        handler.setChannelling(true);
        PayloadHandler.syncSpellsToClient(context.getPlayer());
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Player player = context.getPlayer();
        var handler = SpellUtil.getSpellHandler(player);
        if (!handler.consumeMana(this.manaTickCost, true) || !handler.isChannelling()) {
            this.endSpell();
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        Player player = context.getPlayer();
        var handler = SpellUtil.getSpellHandler(player);
        handler.setChannelling(false);
        PayloadHandler.syncSpellsToClient(context.getPlayer());
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
