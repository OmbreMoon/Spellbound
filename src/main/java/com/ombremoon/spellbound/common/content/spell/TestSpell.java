package com.ombremoon.spellbound.common.content.spell;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.content.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.content.entity.living.LivingShadow;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.JUMP, JUMP_EVENT, playerJumpEvent -> {
            Constants.LOG.info("Jumped");
        });
        if (!context.getLevel().isClientSide) {
            LivingShadow livingShadow = SBEntities.LIVING_SHADOW.get().create(context.getLevel());
            livingShadow.setData(SBData.OWNER_ID, context.getCaster().getId());
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
        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.POST_DAMAGE, DAMAGE_EVENT);
    }
}
