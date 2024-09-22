package com.ombremoon.spellbound.common.content.spell.summon;

import com.mojang.datafixers.kinds.Const;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WildMushroomSpell extends SummonSpell {
    private AABB aoeZone;
    private int awardedXp = 0;
    private int explosionInterval;
    private int poisonDuration;
    private static final int MAX_XP = 5;

    public static Builder<AnimatedSpell> createMushroomBuilder() {
        return createSimpleSpellBuilder().setDuration(180);
    }

    public WildMushroomSpell() {
        super(SpellInit.WILD_MUSHROOM_SPELL.get(), createMushroomBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        var mobs = addMobs(context, EntityInit.MUSHROOM.get(), 1);
        if (mobs == null || !mobs.iterator().hasNext()) {
            endSpell();
            return;
            //TODO: refund mana
        }
        Entity mushroom = context.getLevel().getEntity(mobs.iterator().next());
        SkillHandler skillHandler = context.getSkillHandler();
        double radius = skillHandler.hasSkill(getSpellType(), SkillInit.VILE_INFLUENCE) ? 3D : 2D;
        this.explosionInterval = skillHandler.hasSkill(getSpellType(), SkillInit.HASTENED_GROWTH) ? 40 : 60;
        this.poisonDuration = skillHandler.hasSkill(getSpellType(), SkillInit.ENVENOM) ? 80 : 60;

        this.aoeZone = mushroom.getBoundingBox().inflate(radius, 0, radius);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Constants.LOG.debug("{}", context.getLevel().isClientSide);
        if (ticks % explosionInterval == 0) {
            Player caster = context.getPlayer();
            List<LivingEntity> entities = caster.level().getEntitiesOfClass(
                    LivingEntity.class, aoeZone, entity -> !entity.is(caster) && !entity.isInvulnerable());

            for (LivingEntity entity : entities) {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration), caster);
                if (awardedXp < MAX_XP) {
                    awardedXp++;
                    context.getSkillHandler().awardSpellXp(getSpellType(), 1);
                    context.getSkillHandler().sync(caster);
                }
            }

            Vec3 minPos = aoeZone.getMinPosition();
            Vec3 maxPos = aoeZone.getMaxPosition();
            for (double i = minPos.x; i <= maxPos.x; i++) {
                for (double j = minPos.z; j <= maxPos.z; j++) {
                    ((ServerLevel) context.getLevel()).sendParticles(
                            ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(255, 8889187)),
                            i, aoeZone.maxY - 0.5d, j,
                            1, 0, 0, 0, 0);
                }
            }
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
    }
}
