package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.common.magic.tree.UpgradeTree;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Set;

public class DebugItem extends Item {
    public DebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        var handler = SpellUtil.getSpellHandler(player);
        var skillHandler = SpellUtil.getSkillHandler(player);
        ombreDebug(level, player, usedHand, handler, skillHandler);
        if (!level.isClientSide && !player.isCrouching()) {
//            skillHandler.unlockSkill(SpellInit.WILD_MUSHROOM_SPELL.get(), SkillInit.VILE_INFLUENCE.value());
            handler.sync();
            skillHandler.sync(player);
//            player.sendSystemMessage(Component.literal("Has vile influence: " + skillHandler.hasSkill(SpellInit.WILD_MUSHROOM_SPELL.get(), SkillInit.VILE_INFLUENCE.value())));
        } else if (!level.isClientSide && player.isCrouching()) {
            skillHandler.resetSkills(SpellInit.WILD_MUSHROOM_SPELL.get());
            player.sendSystemMessage(Component.literal("Wild mushrooms has: " + skillHandler.getSpellXp(SpellInit.WILD_MUSHROOM_SPELL.get()) + " XP"));
//            player.sendSystemMessage(Component.literal("Has vile influence: " + skillHandler.hasSkill(SpellInit.WILD_MUSHROOM_SPELL.get(), SkillInit.VILE_INFLUENCE.value())));
            handler.sync();
            skillHandler.sync(player);
        }
        return super.use(level, player, usedHand);
    }

    private void ombreDebug(Level level, Player player, InteractionHand usedHand, SpellHandler spellHandler, SkillHandler skillHandler) {
        Constants.LOG.info("{}", spellHandler.getActiveSpells(SpellInit.SHADOW_GATE.get()));
        if (!level.isClientSide) {
//            spellHandler.clearList();
//            spellHandler.setSelectedSpell(SpellInit.WILD_MUSHROOM_SPELL.get());
//            Constants.LOG.info("{}", skillHandler.getModifiers());
//            spellHandler.learnSpell(SpellInit.VOLCANO.get());
//            Constants.LOG.info("{}", spellHandler.getActiveSpells(SpellInit.SHADOW_GATE.get()));
//            Constants.LOG.info("{}", tree.nodes());
//            Constants.LOG.info("{}", tree.children());
//            Constants.LOG.info("{}", tree.roots());
        } else {
        }
    }
}
