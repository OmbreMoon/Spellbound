package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DebugItem extends Item {
    public DebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var handler = player.getData(DataInit.SPELL_HANDLER.get());
        var skillHandler = player.getData(DataInit.SKILL_HANDLER.get());
        if (!level.isClientSide && !player.isCrouching()) {
            handler.learnSpell(SpellInit.WILD_MUSHROOM_SPELL.get());
            handler.learnSpell(SpellInit.SUMMON_UNDEAD_SPELL.get());
            handler.learnSpell(SpellInit.TEST_SPELL.get());
            skillHandler.resetSpellXP(SpellInit.WILD_MUSHROOM_SPELL.get());
            player.sendSystemMessage(Component.literal("Rest Wild Mushroom XP"));
            Constants.LOG.info("{}", handler.getActiveSpells().stream().map(spell -> spell.getSpellName().getString()).toList());
            handler.sync(player);
        } else if (!level.isClientSide && player.isCrouching()) {
            player.sendSystemMessage(Component.literal("Wild mushrooms has: " + skillHandler.getSpellXp(SpellInit.WILD_MUSHROOM_SPELL) + " XP"));
        }
//        Constants.LOG.info("{}", handler.getSpellList().stream().map(SpellType::getResourceLocation).toList());
        return super.use(level, player, usedHand);
    }
}
