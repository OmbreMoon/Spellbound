package com.ombremoon.spellbound.common.events.custom;

import com.ombremoon.spellbound.common.content.entity.SBMerchantType;
import com.ombremoon.spellbound.common.content.world.SBTrades;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.Event;

import java.util.Collection;
import java.util.List;

public class SpellboundTradesEvent extends Event {
    protected final SBMerchantType type;
    protected final Int2ObjectMap<List<MerchantOffer>> trades;

    public SpellboundTradesEvent(SBMerchantType type, Int2ObjectMap<List<MerchantOffer>> trades) {
        this.type = type;
        this.trades = trades;
    }

    public SBMerchantType getType() {
        return type;
    }

    public Int2ObjectMap<List<MerchantOffer>> getTrades() {
        return trades;
    }
}
