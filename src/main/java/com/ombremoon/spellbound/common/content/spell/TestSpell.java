package com.ombremoon.spellbound.common.content.spell;

import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.main.Constants;
import com.ombremoon.spellbound.common.content.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.content.entity.living.LivingShadow;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.buff.BuffCategory;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.common.magic.api.buff.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class TestSpell extends ChanneledSpell {
    private static final ResourceLocation JUMP_EVENT = CommonClass.customLocation("jumpies");
    private static final ResourceLocation DAMAGE_EVENT = CommonClass.customLocation("hurtsies");

    public static Builder<TestSpell> createTestBuilder() {
        return createChannelledSpellBuilder(TestSpell.class).castTime(20);
    }

    public TestSpell() {
        super(SBSpells.TEST_SPELL.get(), createTestBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        addEventBuff(context.getCaster(), SBSkills.TEST_SKILL.value(), BuffCategory.NEUTRAL, SpellEventListener.Events.JUMP, JUMP_EVENT, jumpEvent -> {
            Constants.LOG.info("Jumped");
        });
        addSkillBuff(context.getCaster(), SBSkills.TEST_SKILL.value(), BuffCategory.BENEFICIAL, SkillBuff.MOB_EFFECT, new MobEffectInstance(MobEffects.INVISIBILITY, -1, 0, false, false), -1);
//        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.JUMP, JUMP_EVENT, playerJumpEvent -> {
//            Constants.LOG.info("Jumped");
//        });
        if (!context.getLevel().isClientSide) {
            LivingShadow livingShadow = SBEntities.LIVING_SHADOW.get().create(context.getLevel());
            livingShadow.setOwner(context.getCaster());
            livingShadow.setPos(context.getCaster().position());
            context.getLevel().addFreshEntity(livingShadow);
            hurt(context.getCaster(), SBDamageTypes.SB_GENERIC, 2.0F);
//            var result = this.getTargetBlock(10);
//            if (result.getType() != HitResult.Type.MISS) {
//                Vec3 pos = result.getLocation();
//                BlockState blockState = Blocks.DIRT.defaultBlockState();
//                FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(EntityType.FALLING_BLOCK, context.getLevel());
//                fallingBlockEntity.blocks
//                fallingBlockEntity.setPos(pos.x + 0.5F, pos.y, pos.z + 0.5F);
//                fallingBlockEntity.setDeltaMovement(Vec3.ZERO);
//                fallingBlockEntity.xo = pos.x;
//                fallingBlockEntity.yo = pos.y;
//                fallingBlockEntity.zo = pos.z;
//                fallingBlockEntity.setStartPos(fallingBlockEntity.blockPosition());
//                context.getLevel().setBlock(pos, )
//            }
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
        Entity targetEntity = this.getTargetEntity(10);
        if (targetEntity instanceof LivingEntity livingEntity) {
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
        removeSkillBuff(context.getCaster(), SBSkills.TEST_SKILL.value());
        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.POST_DAMAGE, DAMAGE_EVENT);
    }
}
