package com.ombremoon.spellbound.common.content.spell.test;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;

import java.util.UUID;

public class TestSpell2 extends ChanneledSpell {
    private static final UUID JUMP_EVENT = UUID.fromString("7859afd8-40a9-41c4-a531-674f1f0fdb1b");

    public static Builder<ChanneledSpell> createTestBuilder() {
        return createChannelledSpellBuilder().setCastTime(20);
    }

    public TestSpell2() {
        super(SpellInit.TEST_SPELL2.get(), createTestBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.JUMP, JUMP_EVENT, playerJumpEvent -> {
            Constants.LOG.info("Jumped");
        });
        Constants.LOG.info("Working");
    }

    @Override
    public void whenCasting(SpellContext context, int castTime) {
        super.whenCasting(context, castTime);
        Constants.LOG.info("{}", castTime);
        addScreenShake(context.getPlayer(), 10, 5);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Constants.LOG.info("{}", this.getTargetEntity(8));
        addScreenShake(context.getPlayer(), 10, 5);
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.JUMP, JUMP_EVENT);
    }
}
