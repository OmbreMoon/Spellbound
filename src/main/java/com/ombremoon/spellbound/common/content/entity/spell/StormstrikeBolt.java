package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.sentinellib.api.BoxUtil;
import com.ombremoon.spellbound.common.content.entity.SpellProjectile;
import com.ombremoon.spellbound.common.content.spell.ruin.shock.StormstrikeSpell;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.buff.ModifierType;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class StormstrikeBolt extends SpellProjectile <StormstrikeSpell> {
    public StormstrikeBolt(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInWaterOrBubble()) {
            hurtSurroundingEnemies(this, 5, true);
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            Entity entity = result.getEntity();

            if (entity.is(owner)) return;

            if (entity instanceof LivingEntity livingEntity) {
                if (owner instanceof LivingEntity living) {
                    livingEntity.setData(SBData.STORMSTRIKE_OWNER.get(), living.getId());
                    livingEntity.addEffect(new MobEffectInstance(SBEffects.STORMSTRIKE, 120, 0, false, false));
                } else {
                    livingEntity.hurt(BoxUtil.sentinelDamageSource(level(), SBDamageTypes.RUIN_SHOCK, this), 5);
                }
            }

            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingEntity) {
                var skills = SpellUtil.getSkillHolder(livingEntity);
                if (skills.hasSkill(SBSkills.STATIC_SHOCK.value())) {
                    hurtSurroundingEnemies(livingEntity, 3, false);
                }
            }
        }
        super.onHitBlock(result);
    }

    private void hurtSurroundingEnemies(Entity owner, int range, boolean inWater) {
        StormstrikeSpell spell = SBSpells.STORMSTRIKE.get().createSpell();
        if (!owner.level().isClientSide) {
            var entities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(range), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity target && !target.is(owner) && !target.isAlliedTo(owner) && !spell.checkForCounterMagic(target)) {
                    float damage = 5F;
                    if (owner instanceof LivingEntity livingEntity) {
                        damage *= spell.getModifier(ModifierType.POTENCY, livingEntity);

                        if (inWater && target.isInWaterOrBubble()) {
                            target.setData(SBData.STORMSTRIKE_OWNER.get(), livingEntity.getId());
                            target.addEffect(new MobEffectInstance(SBEffects.STORMSTRIKE, 120, 0, false, false));
                        }
                    }

                    if (inWater && !target.isInWaterOrBubble()) return;

                    target.hurt(BoxUtil.sentinelDamageSource(level(), SBDamageTypes.RUIN_SHOCK, owner), damage);
                }
            }
        }
    }
}
