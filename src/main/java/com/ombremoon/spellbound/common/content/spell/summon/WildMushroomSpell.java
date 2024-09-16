package com.ombremoon.spellbound.common.content.spell.summon;

import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;

public class WildMushroomSpell extends SummonSpell {
    public static Builder<AnimatedSpell> createMushroomBuilder() {
        return createSimpleSpellBuilder().setDuration(200);
    }

    public WildMushroomSpell() {
        super(SpellInit.WILD_MUSHROOM_SPELL.get(), createMushroomBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
    }
}
