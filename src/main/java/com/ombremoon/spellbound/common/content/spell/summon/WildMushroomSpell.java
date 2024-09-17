package com.ombremoon.spellbound.common.content.spell.summon;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WildMushroomSpell extends SummonSpell {
    private AABB aoeZone;

    public static Builder<AnimatedSpell> createMushroomBuilder() {
        return createSimpleSpellBuilder().setDuration(180);
    }

    public WildMushroomSpell() {
        super(SpellInit.WILD_MUSHROOM_SPELL.get(), createMushroomBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        int mushroomId = addMobs(context, EntityInit.MUSHROOM.get(), 1).iterator().next();
        aoeZone = context.getLevel().getEntity(mushroomId).getBoundingBox().inflate(3d, 0, 3d);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Constants.LOG.debug("{}", context.getLevel().isClientSide);
        if (ticks % 60 == 0) {
            Player caster = context.getPlayer();
            List<LivingEntity> entities = caster.level().getEntitiesOfClass(
                    LivingEntity.class, aoeZone, entity -> true);

            for (LivingEntity entity : entities) {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1, true, true), caster);
            }

            Vec3 minPos = aoeZone.getMinPosition();
            Vec3 maxPos = aoeZone.getMaxPosition();
            for (double i = minPos.x; i < maxPos.x; i++) {
                for (double j = minPos.z; j < maxPos.z; j++) {
                    ((ServerLevel) context.getLevel()).sendParticles(
                            ParticleTypes.EFFECT,
                            i, aoeZone.maxY, j,
                            1, 0, 0, 0, 0);
                }
            }
        }
    }
}
