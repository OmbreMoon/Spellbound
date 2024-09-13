package com.ombremoon.spellbound.common.content.spell;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class SecondTestSpell extends ChanneledSpell {

    public static Builder<ChanneledSpell> createTestBuilder() {
        return createChannelledSpellBuilder().setCastTime(20);
    }

    public SecondTestSpell() {
        super(SpellInit.SECOND_TEST_SPELL.get(), createTestBuilder());
    }

    @Override
    protected void onSpellStart(LivingEntity livingEntityPatch, Level level, BlockPos blockPos) {
        super.onSpellStart(livingEntityPatch, level, blockPos);
        Constants.LOG.info("Second Working");
    }

    @Override
    protected void onSpellTick(LivingEntity caster, Level level, BlockPos blockPos) {
        super.onSpellTick(caster, level, blockPos);
        Constants.LOG.info("BEAMING");
    }
}
