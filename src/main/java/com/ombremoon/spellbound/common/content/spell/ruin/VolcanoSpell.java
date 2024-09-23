package com.ombremoon.spellbound.common.content.spell.ruin;

import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;

public class VolcanoSpell extends AnimatedSpell {

    private static Builder<AnimatedSpell> createVolcanoBuilder() {
        return createSimpleSpellBuilder().setCastTime(20);
    }

    public VolcanoSpell() {
        super(SpellInit.VOLCANO.get(), createVolcanoBuilder());
    }
}
