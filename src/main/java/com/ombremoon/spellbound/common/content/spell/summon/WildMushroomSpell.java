package com.ombremoon.spellbound.common.content.spell.summon;

import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.data.SpellHandler;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.util.valueproviders.UniformFloat;
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
        double radius = skillHandler.hasSkill(SkillInit.VILE_INFLUENCE.value()) ? 3D : 2D;
        this.explosionInterval = skillHandler.hasSkill(SkillInit.HASTENED_GROWTH.value()) ? 40 : 60;
        this.poisonDuration = skillHandler.hasSkill(SkillInit.ENVENOM.value()) ? 80 : 60;

        this.aoeZone = mushroom.getBoundingBox().inflate(radius, 0, radius);
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Constants.LOG.debug("{}", context.getLevel().isClientSide);
        float intervalProgress = ticks % explosionInterval;
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


            //rot should be i/j based, offset should be tick baed
        }
        if (intervalProgress <= 12 && intervalProgress % 6 == 0) {
            Vec3 center = aoeZone.getCenter();
            for (double i = 1; i <= 20; i++) {
                for (double j = 1; j <= 5; j++) {
                    double rot = Math.toRadians(i*18);
                    Vec3 pos = new Vec3(center.x + ((intervalProgress/6)+2-(j/2.5)) * Math.cos(rot),
                            aoeZone.minY + (j/4),
                            center.z + ((intervalProgress/6)+2-(j/2.5)) * Math.sin(rot));

                    ((ServerLevel) context.getLevel()).sendParticles(
                            ParticleTypes.FLAME,
                            pos.x, pos.y, pos.z,
                            1, 0, 0, 0, 0);
                }
            }
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        if (context.getSkillHandler().hasSkill(SkillInit.CIRCLE_OF_LIFE.value())) {
            SpellHandler handler = context.getSpellHandler();
            int level = context.getSkillHandler().getSpellLevel(getSpellType());
            handler.awardMana(52 + (2 * (level-1)));
            handler.sync();
        }
    }
}
