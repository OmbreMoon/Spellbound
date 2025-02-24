package com.ombremoon.spellbound.common.content.world;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.ombremoon.spellbound.common.content.entity.SBMerchantType;
import com.ombremoon.spellbound.common.content.item.SpellTomeItem;
import com.ombremoon.spellbound.common.init.SBBlocks;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class SBTrades {
    public static final Map<SBMerchantType, Int2ObjectMap<MerchantOffer[]>> TRADES = Util.make(Maps.newHashMap(), map -> {
        map.put(SBMerchantType.SPELL_BROKER, toIntMap(ImmutableMap.of(1, new MerchantOffer[]{
                spellTrade(20, Items.GOLDEN_APPLE, 10, SBSpells.HEALING_TOUCH.get()),
                spellTrade(20, Items.ECHO_SHARD, 5, SBSpells.SHADOW_GATE.get()),
                spellTrade(20, Items.BOOK, 20, SBSpells.PURGE_MAGIC.get()),
                spellTrade(20, Items.LAVA_BUCKET, 1, SBSpells.SOLAR_RAY.get()),
                spellTrade(20, Items.SADDLE, 1, SBSpells.STRIDE.get()),
                spellTrade(15, Items.GOLD_BLOCK, 32, SBSpells.SPIRIT_TOTEM.get())
        }, 2, new MerchantOffer[]{
                spellTrade(20, Items.GOLDEN_APPLE, 10, SBSpells.HEALING_TOUCH.get()),
                spellTrade(20, Items.GOLDEN_APPLE, 10, SBSpells.HEALING_TOUCH.get()),
                spellTrade(20, Items.GOLDEN_APPLE, 10, SBSpells.HEALING_TOUCH.get()),
                spellTrade(20, Items.GOLDEN_APPLE, 10, SBSpells.HEALING_TOUCH.get()),
                spellTrade(20, Items.GOLDEN_APPLE, 10, SBSpells.HEALING_TOUCH.get()),
        })));
    });

    private static MerchantOffer spellTrade(int arcanthusCost, ItemLike item, int count, SpellType<?> spell) {
        return makeOffer(SBBlocks.ARCANTHUS.get().asItem(), arcanthusCost, item, count, SpellTomeItem.createWithSpell(spell), 999, 0, 0f);
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

    private static Int2ObjectMap<MerchantOffer[]> toIntMap(ImmutableMap<Integer, MerchantOffer[]> map) {
        return new Int2ObjectOpenHashMap(map);
    }
}
