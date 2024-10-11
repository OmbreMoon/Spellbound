package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.sentinellib.api.BoxUtil;
import com.ombremoon.spellbound.common.content.entity.SpellProjectile;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.AbstractSpell;
import com.ombremoon.spellbound.common.magic.api.ModifierType;
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

public class StormstrikeBolt extends SpellProjectile {
    public StormstrikeBolt(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public StormstrikeBolt(Level level, LivingEntity shooter, AbstractSpell spell) {
        super(level, EntityInit.STORMSTRIKE_BOLT.get(), shooter, spell);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            Entity entity = result.getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                if (owner instanceof LivingEntity living) {
                    livingEntity.setData(DataInit.STORMSTRIKE_OWNER.get(), living.getId());
                    livingEntity.addEffect(new MobEffectInstance(EffectInit.STORMSTRIKE, 120, 0, false, false));
                } else {
                    livingEntity.hurt(BoxUtil.sentinelDamageSource(level(), DamageTypeInit.RUIN_SHOCK, this), 5);
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
                var skills = SpellUtil.getSkillHandler(livingEntity);
                if (skills.hasSkill(SkillInit.STATIC_SHOCK.value())) {
                    var entities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity target && !target.is(owner) && !target.isAlliedTo(owner)) {
                            AbstractSpell spell = SpellInit.STORMSTRIKE.get().createSpell();
                            target.hurt(BoxUtil.sentinelDamageSource(level(), DamageTypeInit.RUIN_SHOCK, owner), spell.getModifier(ModifierType.POTENCY, livingEntity) * 5);
                        }
                    }
                }
            }
        }
        super.onHitBlock(result);
    }
}
