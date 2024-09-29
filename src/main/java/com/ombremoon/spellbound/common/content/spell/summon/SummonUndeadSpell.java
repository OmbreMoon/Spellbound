package com.ombremoon.spellbound.common.content.spell.summon;

import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;

public class SummonUndeadSpell extends SummonSpell {
    public static AnimatedSpell.Builder<AnimatedSpell> createSummonBuilder() {
        return createSimpleSpellBuilder()
                .setManaCost(10)
                .setDuration(180)
                .castCondition((player, spell) -> player.level().getDifficulty() != Difficulty.PEACEFUL);
    }

    public SummonUndeadSpell() {
        super(SpellInit.SUMMON_UNDEAD_SPELL.get(), createSummonBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        var success = addMobs(context, EntityType.ZOMBIE, 1);
        if (success == null) {
            endSpell();
            context.getSpellHandler().awardMana(this.getManaCost(context.getSkillHandler()));
            context.getSpellHandler().sync();
        }
    }
}
