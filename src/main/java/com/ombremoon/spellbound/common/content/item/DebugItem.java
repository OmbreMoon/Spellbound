package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.client.gui.SpellSelectScreen;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.util.Loggable;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DebugItem extends Item implements Loggable {
    public DebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var handler = SpellUtil.getSpellHandler(player);
        var skillHandler = SpellUtil.getSkillHolder(player);
        ombreDebug(level, player, usedHand, handler, skillHandler);
        if (!level.isClientSide && !player.isCrouching()) {
//            skillHandler.unlockSkill(SpellInit.WILD_MUSHROOM_SPELL.get(), SkillInit.VILE_INFLUENCE.value());
            handler.sync();
            skillHandler.sync(player);
//            player.sendSystemMessage(Component.literal("Has vile influence: " + skillHandler.hasSkill(SpellInit.WILD_MUSHROOM_SPELL.get(), SkillInit.VILE_INFLUENCE.value())));
        } else if (!level.isClientSide && player.isCrouching()) {
            skillHandler.resetSkills(SBSpells.WILD_MUSHROOM.get());
            player.sendSystemMessage(Component.literal("Wild mushrooms has: " + skillHandler.getSpellXp(SBSpells.WILD_MUSHROOM.get()) + " XP"));
//            player.sendSystemMessage(Component.literal("Has vile influence: " + skillHandler.hasSkill(SpellInit.WILD_MUSHROOM_SPELL.get(), SkillInit.VILE_INFLUENCE.value())));
            handler.sync();
            skillHandler.sync(player);
        }
        return super.use(level, player, usedHand);
    }

    private void ombreDebug(Level level, Player player, InteractionHand usedHand, SpellHandler spellHandler, SkillHolder skillHolder) {
//        spellHandler.getActiveSpells(SpellInit.WILD_MUSHROOM_SPELL.get()).forEach(spell -> Constants.LOG.info("{}", spell.getId()));
        if (!level.isClientSide) {
//            spellHandler.clearList();
//            spellHandler.clearList();
//            spellHandler.setSelectedSpell(SpellInit.WILD_MUSHROOM_SPELL.get());
//            spellHandler.setSelectedSpell(SpellInit.TEST_SPELL.get());
//            Constants.LOG.info("{}", tree.nodes());
//            Constants.LOG.info("{}", tree.children());
//            Constants.LOG.info("{}", tree.roots());
            spellHandler.clearList();
        } else {
//            log(spellHandler.getActiveSpells());
//            Minecraft.getInstance().setScreen(new SpellSelectScreen());
//            Constants.LOG.info("{}", spellHandler.getActiveSpells());
        }
//        skillHolder.clearModifiers();
    }
}
