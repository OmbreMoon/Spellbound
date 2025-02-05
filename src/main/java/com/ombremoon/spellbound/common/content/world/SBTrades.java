package com.ombremoon.spellbound.common.content.world;

import com.ombremoon.spellbound.common.content.item.SpellTomeItem;
import com.ombremoon.spellbound.common.init.SBBlocks;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.DOMErrorHandler;

import java.util.List;
import java.util.Optional;

public class SBTrades {
    public static final MerchantOffers SPELL_BROKER = new MerchantOffers();

    public static void initialiseTrades() {
        SPELL_BROKER.addAll(List.of(
                spellTrade(10, SBSpells.HEALING_TOUCH.get()),
                spellTrade(20, SBSpells.SHADOW_GATE.get())
        ));
    }

    private static MerchantOffer spellTrade(int arcanthusCost, SpellType<?> spell) {
        return makeOffer(SBBlocks.ARCANTHUS.get().asItem(), arcanthusCost, SpellTomeItem.createWithSpell(spell), 999, 0, 0f);
    }

    private static MerchantOffer makeOffer(@NotNull ItemLike item, int count, @NotNull ItemStack result, int maxUses, int xp, float multiplier) {
        return makeOffer(item, count, null, null, result, maxUses, xp, multiplier);
    }

    private static MerchantOffer makeOffer(@NotNull ItemLike item1, int count1, @Nullable ItemLike item2, @Nullable Integer count2, @NotNull ItemStack result, int maxUses, int xp, float multiplier) {
        ItemCost costA = new ItemCost(item1, count1);
        Optional<ItemCost> costB = Optional.ofNullable(item2 == null || count2 == null ? null : new ItemCost(item2, count2));
        return new MerchantOffer(
                costA,
                costB,
                result,
                maxUses,
                xp,
                multiplier
        );
    }
}
