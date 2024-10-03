package com.ombremoon.spellbound.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellType;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;

public class LearnSkillsCommand {
    public LearnSkillsCommand(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("learnskills")
                .then(Commands.argument("spell", ResourceArgument.resource(context, SpellInit.SPELL_TYPE_REGISTRY_KEY))
                        .executes(cmdContext -> learnSpells(cmdContext.getSource(),
                                ResourceArgument.getResource(cmdContext, "spell", SpellInit.SPELL_TYPE_REGISTRY_KEY))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("spell", ResourceArgument.resource(context, SpellInit.SPELL_TYPE_REGISTRY_KEY))
                                .executes(cmdContext -> resetSpells(cmdContext.getSource(),
                                        ResourceArgument.getResource(cmdContext, "spell", SpellInit.SPELL_TYPE_REGISTRY_KEY))))));
    }

    private int learnSpells(CommandSourceStack context, Holder.Reference<SpellType<?>> spell) {
        if (!context.isPlayer()) return 0;
        SpellHandler handler = SpellUtil.getSpellHandler(context.getPlayer());
        SkillHandler skillHandler = SpellUtil.getSkillHandler(context.getPlayer());

        SpellType<?> spellType = SpellInit.REGISTRY.get(spell.key());
        if (!handler.getSpellList().contains(spellType)) {
            context.getPlayer().sendSystemMessage(Component.translatable("command.spellbound.nospellknown"));
            return 0;
        };

        for (Skill skill : spellType.getSkills()) {
            skillHandler.unlockSkill(skill);
        }

        skillHandler.sync(context.getPlayer());
        context.getPlayer().sendSystemMessage(Component.translatable("command.spellbound.learntspells"));

        return 1;
    }

    private int resetSpells(CommandSourceStack context, Holder.Reference<SpellType<?>> spell) {
        if (!context.isPlayer()) return 0;
        SpellHandler handler = SpellUtil.getSpellHandler(context.getPlayer());
        SkillHandler skillHandler = SpellUtil.getSkillHandler(context.getPlayer());

        SpellType<?> spellType = SpellInit.REGISTRY.get(spell.key());
        if (!handler.getSpellList().contains(spellType)) return 0;

        skillHandler.resetSkills(spellType);
        return 1;
    }
}
