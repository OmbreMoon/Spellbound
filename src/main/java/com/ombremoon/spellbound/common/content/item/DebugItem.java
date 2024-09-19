package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellPath;
import com.ombremoon.spellbound.common.magic.SpellType;
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
            skillHandler.unlockSkill(SpellInit.WILD_MUSHROOM_SPELL, SkillInit.VILE_INFLUENCE.get());
//            skillHandler.resetSpellXP(SpellInit.TEST_SPELL.get());
            skillHandler.awardSpellXp(SpellInit.TEST_SPELL.get(), 500);
            handler.sync();
            skillHandler.sync(player);
            player.sendSystemMessage(Component.literal("Has vile influence: " + skillHandler.hasSkill(SpellInit.WILD_MUSHROOM_SPELL.get(), SkillInit.VILE_INFLUENCE)));
        } else if (!level.isClientSide && player.isCrouching()) {
            skillHandler.resetSkills(SpellInit.WILD_MUSHROOM_SPELL);
            skillHandler.resetSpellXP(SpellInit.TEST_SPELL.get());
            player.sendSystemMessage(Component.literal("Ruin path has: " + skillHandler.getPathXp(SpellPath.RUIN) + " XP"));
            player.sendSystemMessage(Component.literal("Wild mushrooms has: " + skillHandler.getSpellXp(SpellInit.WILD_MUSHROOM_SPELL.get()) + " XP"));
            player.sendSystemMessage(Component.literal("Test Spell has: " + skillHandler.getSpellXp(SpellInit.TEST_SPELL.get()) + " XP"));
            player.sendSystemMessage(Component.literal("Has vile influence: " + skillHandler.hasSkill(SpellInit.WILD_MUSHROOM_SPELL.get(), SkillInit.VILE_INFLUENCE)));
            handler.sync();
            skillHandler.sync(player);
        }
//        Constants.LOG.info("{}", handler.getSpellList().stream().map(SpellType::getResourceLocation).toList());
        return super.use(level, player, usedHand);
    }
}
