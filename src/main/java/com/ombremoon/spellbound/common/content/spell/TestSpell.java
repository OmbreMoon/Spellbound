package com.ombremoon.spellbound.common.content.spell;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;

public class TestSpell extends AnimatedSpell {

    public static Builder<AnimatedSpell> createTestBuilder() {
        return createSimpleSpellBuilder();
    }

    public TestSpell() {
        super(SpellInit.TEST_SPELL.get(), createTestBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Constants.LOG.info("Working");
    }
}
