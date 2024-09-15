package com.ombremoon.spellbound.common.content.spell;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
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
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Constants.LOG.info("Second Working");
    }

    @Override
    public void whenCasting(SpellContext context, int castTime) {
        super.whenCasting(context, castTime);
        Constants.LOG.info("Casting");
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Constants.LOG.info(String.valueOf(this.getTargetEntity(8)));
    }
}
