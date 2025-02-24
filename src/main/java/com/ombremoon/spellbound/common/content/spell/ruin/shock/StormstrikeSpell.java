package com.ombremoon.spellbound.common.content.spell.ruin.shock;

import com.ombremoon.spellbound.common.content.entity.ISpellEntity;
import com.ombremoon.spellbound.common.content.entity.spell.StormstrikeBolt;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class StormstrikeSpell extends AnimatedSpell {
    public static Builder<StormstrikeSpell> createStormstrikeBuilder() {
        return createSimpleSpellBuilder(StormstrikeSpell.class)
                .castTime(20).manaCost(60);
    }

    public StormstrikeSpell() {
        super(SBSpells.STORMSTRIKE.get(), createStormstrikeBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            this.shootProjectile(context, SBEntities.STORMSTRIKE_BOLT.get(), 2.5F, 1.0F);
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {

    }

    @Override
    public void onEntityTick(ISpellEntity<?> spellEntity, SpellContext context) {
        if (spellEntity instanceof StormstrikeBolt bolt) {
            Level level = context.getLevel();
            LivingEntity caster = context.getCaster();
            if (bolt.isInWaterOrBubble()) {
                hurtSurroundingEnemies(level, caster, bolt, 5, true);
                bolt.discard();
            }
        }
    }

    @Override
    public void onProjectileHitEntity(ISpellEntity<?> spellEntity, SpellContext context, EntityHitResult result) {
        if (spellEntity instanceof StormstrikeBolt bolt) {
            Level level = context.getLevel();
            LivingEntity caster = context.getCaster();
            if (!level.isClientSide) {
                Entity entity = result.getEntity();

                if (entity.is(caster)) return;

                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.setData(SBData.STORMSTRIKE_OWNER.get(), caster.getId());
                    livingEntity.addEffect(new MobEffectInstance(SBEffects.STORMSTRIKE, 60, 0, false, false));
                }

                bolt.discard();
            }
        }
    }

    @Override
    public void onProjectileHitBlock(ISpellEntity<?> spellEntity, SpellContext context, BlockHitResult result) {
        if (spellEntity instanceof StormstrikeBolt bolt) {
            Level level = context.getLevel();
            LivingEntity caster = context.getCaster();
            var skills = context.getSkills();
            if (!level.isClientSide) {
                if (skills.hasSkill(SBSkills.STATIC_SHOCK.value()))
                    hurtSurroundingEnemies(level, caster, bolt, 3, false);
            }

            if (!level.isClientSide) {
                bolt.discard();
            }
        }
    }

    private void hurtSurroundingEnemies(Level level, LivingEntity caster, StormstrikeBolt bolt, int range, boolean inWater) {
        if (!level.isClientSide) {
            var entities = level.getEntitiesOfClass(LivingEntity.class, bolt.getBoundingBox().inflate(range), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity target && !target.is(caster) && !target.isAlliedTo(caster) && !checkForCounterMagic(target)) {
                    if (inWater && target.isInWaterOrBubble()) {
                        target.setData(SBData.STORMSTRIKE_OWNER.get(), caster.getId());
                        target.addEffect(new MobEffectInstance(SBEffects.STORMSTRIKE, 60, 0, false, false));
                    }

                    if (inWater && !target.isInWaterOrBubble()) return;

                    this.hurt(target, SBDamageTypes.RUIN_SHOCK, 5.0F);
                }
            }
        }
    }
}
