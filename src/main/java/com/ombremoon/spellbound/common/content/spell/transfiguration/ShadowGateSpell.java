package com.ombremoon.spellbound.common.content.spell.transfiguration;

import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;

public class ShadowGateSpell extends AnimatedSpell {
    private static Builder<AnimatedSpell> createShadowGateBuilder() {
        return createSimpleSpellBuilder().castTime(20).castCondition(context -> {
            if (context.getSkillHandler().hasSkill(SkillInit.DUAL_DESTINATION.value()) && !context.hasActiveSpells(SpellInit.SHADOW_GATE.get(), 3)) {
                return true;
            } else return !context.hasActiveSpells(SpellInit.SHADOW_GATE.get(), 2);
        });
    }

    public ShadowGateSpell() {
        super(SpellInit.SHADOW_GATE.get(), createShadowGateBuilder());
    }
}
