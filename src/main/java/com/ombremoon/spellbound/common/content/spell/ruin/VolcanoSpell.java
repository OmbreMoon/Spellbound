package com.ombremoon.spellbound.common.content.spell.ruin;

import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class VolcanoSpell extends AnimatedSpell {

    private static Builder<AnimatedSpell> createVolcanoBuilder() {
        return createSimpleSpellBuilder().setCastTime(20);
    }

    public VolcanoSpell() {
        super(SpellInit.VOLCANO.get(), createVolcanoBuilder());
    }
}
