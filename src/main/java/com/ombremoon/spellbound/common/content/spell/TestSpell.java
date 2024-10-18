package com.ombremoon.spellbound.common.content.spell;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.content.entity.living.LivingShadow;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.init.SBEffects;
import com.ombremoon.spellbound.common.init.SBEntities;
import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class TestSpell extends ChanneledSpell {
    private static final ResourceLocation JUMP_EVENT = CommonClass.customLocation("jumpies");

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
            livingShadow.setData(SBData.OWNER_ID, context.getCaster().getId());
            livingShadow.setPos(context.getCaster().position());
            context.getLevel().addFreshEntity(livingShadow);
        }
    }

    @Override
    public void whenCasting(SpellContext context, int castTime) {
        super.whenCasting(context, castTime);
        Constants.LOG.info("{}", castTime);
        if (context.getLevel().isClientSide && context.getCaster() instanceof Player player)
            shakeScreen(player, 10, 5);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity livingEntity = this.getTargetEntity(10);
        if (livingEntity != null) {
            if (!context.getLevel().isClientSide) {
                livingEntity.addEffect(new SBEffectInstance(context.getCaster(), SBEffects.AFTERGLOW, 40, true, 0, false, false));
            } else {
//                context.getSpellHandler().addGlowEffect(livingEntity);
            }
        }

        if (context.getLevel().isClientSide && context.getCaster() instanceof Player player)
            shakeScreen(player, 10, 5);
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.JUMP, JUMP_EVENT);
    }
}
