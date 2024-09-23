package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.ItemInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.init.StatInit;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

public class SpellTomeItem extends Item {

    public SpellTomeItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        String resLocString = itemStack.get(DataInit.SPELL);
        if (resLocString == null) return fail(player, itemStack);
        ResourceLocation resLoc = ResourceLocation.tryParse(resLocString);
        if (resLoc == null) return fail(player, itemStack);

        SpellType<?> spell = SpellInit.REGISTRY.get(resLoc);
        if (!level.isClientSide) {
            var handler = SpellUtil.getSpellHandler(player);
            if (handler.getSpellList().contains(spell)) {
                player.displayClientMessage(Component.translatable("chat.spelltome.awardxp"), true);
                player.getData(DataInit.SKILL_HANDLER.get()).awardSpellXp(spell, 10);
                itemStack.shrink(1);
                return InteractionResultHolder.fail(itemStack);
            }

            player.displayClientMessage(Component.translatable("chat.spelltome.spellunlocked", I18n.get(spell.)));
            handler.learnSpell(spell);
            itemStack.shrink(1);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.awardStat(StatInit.SPELLS_LEARNED.get());
        player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    public static ItemStack createWithSpell(SpellType<?> spellType) {
        ItemStack tome = new ItemStack(ItemInit.TEST_SPELL_TOME.get());
        tome.set(DataInit.SPELL, spellType.location().toString());

        return tome;
    }

    private InteractionResultHolder<ItemStack> fail(Player player, ItemStack stack) {
        player.displayClientMessage(Component.translatable("chat.spelltome.nospell"), true);
        return InteractionResultHolder.fail(stack);
    }
}
