package com.ombremoon.spellbound.common.content.spell;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.content.entity.living.LivingShadow;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.init.SBEffects;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class TestSpell extends ChanneledSpell {
    private static final UUID JUMP_EVENT = UUID.fromString("7859afd8-40a9-41c4-a531-674f1f0fdb1b");

    public static Builder<TestSpell> createTestBuilder() {
        return createChannelledSpellBuilder(TestSpell.class).castTime(20);
    }

    public TestSpell() {
        super(SBSpells.TEST_SPELL.get(), createTestBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.JUMP, JUMP_EVENT, playerJumpEvent -> {
            Constants.LOG.info("Jumped");
        });
        Constants.LOG.info("Working");
        if (!context.getLevel().isClientSide) {
            LivingShadow livingShadow = SBEntities.LIVING_SHADOW.get().create(context.getLevel());
            livingShadow.setData(SBData.OWNER_ID, context.getPlayer().getId());
            livingShadow.setPos(context.getPlayer().position());
            context.getLevel().addFreshEntity(livingShadow);
        }
    }

    @Override
    public void whenCasting(SpellContext context, int castTime) {
        super.whenCasting(context, castTime);
        Constants.LOG.info("{}", castTime);
        if (context.getLevel().isClientSide)
            shakeScreen(context.getPlayer(), 10, 5);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity livingEntity = this.getTargetEntity(10);
        if (livingEntity != null) {
            if (!context.getLevel().isClientSide) {
                livingEntity.addEffect(new SBEffectInstance(context.getPlayer(), SBEffects.AFTERGLOW, 40, true, 0, false, false));
            } else {
//                context.getSpellHandler().addGlowEffect(livingEntity);
            }
        }

        if (context.getLevel().isClientSide)
            shakeScreen(context.getPlayer(), 10, 5);
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.JUMP, JUMP_EVENT);
    }
}
