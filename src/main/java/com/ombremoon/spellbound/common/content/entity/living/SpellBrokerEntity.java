package com.ombremoon.spellbound.common.content.entity.living;

import com.ombremoon.spellbound.common.content.world.SBTrades;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SpellBrokerEntity extends SBMerchant{
    protected SpellBrokerEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public @NotNull MerchantOffers getOffers() {
        if (this.trades == null || this.trades.isEmpty()) {
            this.trades = SBTrades.SPELL_BROKER;
        }
        return this.trades;
    }

    @Override
    public void notifyTrade(@NotNull MerchantOffer merchantOffer) {

    }

    @Override
    public void notifyTradeUpdated(@NotNull ItemStack itemStack) {

    }

    @Override
    public @NotNull SoundEvent getNotifyTradeSound() {
        return SoundEvents.AMETHYST_CLUSTER_PLACE;
    }
}
