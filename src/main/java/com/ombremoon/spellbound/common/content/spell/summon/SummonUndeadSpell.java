package com.ombremoon.spellbound.common.content.spell.summon;

import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import net.minecraft.world.entity.EntityType;

public class SummonUndeadSpell extends SummonSpell {
    public static AnimatedSpell.Builder<AnimatedSpell> createSummonBuilder() {
        return createSimpleSpellBuilder().setDuration(20*60);
    }

    public SummonUndeadSpell() {
        super(SpellInit.SUMMON_UNDEAD_SPELL.get(), createSummonBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        var success = addMobs(context, EntityType.ZOMBIE, 1);
        if (success == null) endSpell(); //TODO: refund mana
    }
}
