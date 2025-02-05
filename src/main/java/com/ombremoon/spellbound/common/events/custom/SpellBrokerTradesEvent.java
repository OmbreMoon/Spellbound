package com.ombremoon.spellbound.common.events.custom;

import com.ombremoon.spellbound.common.content.entity.living.SBMerchant;
import com.ombremoon.spellbound.common.content.world.SBTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.Event;

import java.util.Collection;

public class SpellBrokerTradesEvent extends Event {

    public MerchantOffers getTrades() {
        return SBTrades.SPELL_BROKER;
    }

    public void addTrade(MerchantOffer trade) {
        SBTrades.SPELL_BROKER.add(trade);
    }

    public void addTrades(Collection<? extends MerchantOffer> trades) {
        SBTrades.SPELL_BROKER.addAll(trades);
    }
}
