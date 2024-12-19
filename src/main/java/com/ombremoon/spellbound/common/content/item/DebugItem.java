package com.ombremoon.spellbound.common.content.item;

import com.ombremoon.spellbound.common.init.SBTriggers;
import com.ombremoon.spellbound.common.magic.acquisition.divine.DivineAction;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.util.Loggable;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
            skillHandler.sync();
//            player.sendSystemMessage(Component.literal("Has vile influence: " + skillHandler.hasSkill(SpellInit.WILD_MUSHROOM_SPELL.get(), SkillInit.VILE_INFLUENCE.value())));
        } else if (!level.isClientSide && player.isCrouching()) {
            skillHandler.resetSkills(SBSpells.WILD_MUSHROOM.get());
            player.sendSystemMessage(Component.literal("Wild mushrooms has: " + skillHandler.getSpellXp(SBSpells.WILD_MUSHROOM.get()) + " XP"));
//            player.sendSystemMessage(Component.literal("Has vile influence: " + skillHandler.hasSkill(SpellInit.WILD_MUSHROOM_SPELL.get(), SkillInit.VILE_INFLUENCE.value())));
            handler.sync();
            skillHandler.sync();
        }
        return super.use(level, player, usedHand);
    }

    private void ombreDebug(Level level, Player player, InteractionHand usedHand, SpellHandler spellHandler, SkillHolder skillHolder) {
//        spellHandler.setSelectedSpell(SBSpells.TEST_SPELL.get());
//        log(spellHandler.getActiveSpells());
        if (!level.isClientSide) {
//            spellHandler.removeSpell(SBSpells.VOLCANO.get());
//            log(Registries.elementsDirPath(DivineAction.REGISTRY));
            SBTriggers.TEST_TRIGGER.get().trigger((ServerPlayer) player);
//            spellHandler.setSelectedSpell(SBSpells.TEST_SPELL.get());
//            ((HailstormSavedData)HailstormSavedData.get(level)).toggleHailing((ServerLevel) level, 600);
        } else {
//            log(ClientStuff.getInstance().getExamples().exampleBufferSource());
//            Minecraft.getInstance().setScreen(new SpellSelectScreen());
//            SBShaders.EXAMPLE_SHADER_2.toggleShader();
//            SBShaders.EXAMPLE_SHADER.toggleShader();
        }
    }
}
