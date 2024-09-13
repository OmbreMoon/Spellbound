package com.ombremoon.spellbound.common.content.spell;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class TestSpell extends AnimatedSpell {

    public static Builder<AnimatedSpell> createTestBuilder() {
        return createSimpleSpellBuilder();
    }

    public TestSpell() {
        super(SpellInit.TEST_SPELL.get(), createTestBuilder());
    }

    @Override
    protected void onSpellStart(LivingEntity livingEntityPatch, Level level, BlockPos blockPos) {
        super.onSpellStart(livingEntityPatch, level, blockPos);
        Constants.LOG.info("Working");
    }
}
