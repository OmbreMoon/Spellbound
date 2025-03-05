package com.ombremoon.spellbound.common.content.spell.summon;

import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.common.content.entity.spell.WildMushroom;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.api.SummonSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;

public class WildMushroomSpell extends SummonSpell {
    private static final ResourceLocation SYNTHESIS = CommonClass.customLocation("synthesis");
    private static final ResourceLocation RECYCLED_LOCATION = CommonClass.customLocation("recycled_regen");
    private static final int MAX_XP = 50;
    private static final int XP_PER_HIT = 5;

    private final Set<LivingEntity> targetsHit = new HashSet<>();
    private WildMushroom mushroom;
    private AABB damageZone;
    private int poisonEssenceExpiry = 0;
    private int awardedXp = 0;
    private int explosionInterval;

    public static Builder<WildMushroomSpell> createMushroomBuilder() {
        return createSummonBuilder(WildMushroomSpell.class)
                .mastery(SpellMastery.ADEPT)
                .duration(180)
                .manaCost(15);
    }

    public WildMushroomSpell() {
        super(SBSpells.WILD_MUSHROOM.get(), createMushroomBuilder());
    }

/*    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        context.getSpellHandler().getListener().addListener(SpellEventListener.Events.PLAYER_KILL, SYNTHESIS, this::playerKill);
        if (!context.getLevel().isClientSide) {
            WildMushroom mushroom = summonEntity(context, SBEntities.MUSHROOM.get(), 5);
            this.mushroom = mobs.iterator().next();
            SkillHolder skillHolder = context.getSkills();
            double radius = skillHolder.hasSkill(SBSkills.VILE_INFLUENCE.value()) ? 3D : 2D;
            this.damageZone = mushroom.getBoundingBox().inflate(radius, 0, radius);
            this.explosionInterval = skillHolder.hasSkill(SBSkills.HASTENED_GROWTH.value()) ? 40 : 60;


            boolean recycledFlag = context.getSpellHandler().getActiveSpells(getSpellType()).size() >= 3;
            boolean recycledFlag2 = skillHolder.hasSkill(SBSkills.RECYCLED.value());
            if (recycledFlag && recycledFlag2)
                this.addAttributeModifier(context.getCaster(), SBAttributes.MANA_REGEN, new AttributeModifier(RECYCLED_LOCATION,
                        1.1d,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        if (context.getLevel().isClientSide) return;

        if (poisonEssenceExpiry > 0) poisonEssenceExpiry--;
        this.mushroom.explode(); //Visual stuff
        LivingEntity caster = context.getCaster();
        SkillHolder skills = context.getSkills();

        List<LivingEntity> entities = caster.level().getEntitiesOfClass(
                LivingEntity.class,
                this.damageZone,
                entity -> !entity.is(caster) && !entity.isInvulnerable());

        for (LivingEntity entity : entities) {
            if (skills.hasSkillReady(SBSkills.CATALEPSY.value())) {
                entity.addEffect(new MobEffectInstance(SBEffects.CATALEPSY, 100), caster);
            }

            if (skills.hasSkill(SBSkills.ENVENOM.value())) {
                entity.getData(SBData.STATUS_EFFECTS).increment(EffectManager.Effect.POISON, 100);
            } else {
                entity.getData(SBData.STATUS_EFFECTS).increment(EffectManager.Effect.POISON, 33);
            }

            entity.hurt(entity.damageSources().explosion(caster, null), calculateDamage(context, entity));
            targetsHit.add(entity);

            if (awardedXp < MAX_XP) {
                awardedXp++;
                context.getSkills().awardSpellXp(getSpellType(), XP_PER_HIT);
//                context.getSkills().sync(caster);
            }
        }

        if (!entities.isEmpty() && skills.hasSkillReady(SBSkills.CATALEPSY.value()))
            this.addCooldown(SBSkills.CATALEPSY.value(), 200);

        if (context.getSpellHandler().getActiveSpells(getSpellType()).size() <= 2
                && this.hasAttributeModifier(context.getCaster(), SBAttributes.MANA_REGEN, RECYCLED_LOCATION)) {
            this.removeAttributeModifier(context.getCaster(), SBAttributes.MANA_REGEN, RECYCLED_LOCATION);
        }
    }

    @Override
    protected boolean shouldTickSpellEffect(SpellContext context) {
        return explosionInterval <= 0 || ticks % explosionInterval == 0;
    }

    private float calculateDamage(SpellContext context, LivingEntity target) {
        float damage = 2f;
        SkillHolder handler = context.getSkills();

        if (context.getSkills().hasSkill(SBSkills.DECOMPOSE.value())
                && target.hasEffect(SBEffects.POISON)) damage += (float) (context.getCaster().getData(SBData.MANA)/100f);

        if (handler.hasSkill(SBSkills.NATURES_DOMINANCE.value())) damage *= 1f + (0.1f * context.getSpellHandler().getActiveSpells(getSpellType()).size());
        if (handler.hasSkill(SBSkills.POISON_ESSENCE.value()) && poisonEssenceExpiry > ticks) damage *= 1.25f;

        return damage;
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        context.getSpellHandler().getListener().removeListener(SpellEventListener.Events.PLAYER_KILL, SYNTHESIS);
        if (!context.getLevel().isClientSide) {
            if (context.getSkills().hasSkill(SBSkills.CIRCLE_OF_LIFE.value())) {
                SpellHandler handler = context.getSpellHandler();
                int level = context.getSkills().getSpellLevel(getSpellType());
                handler.awardMana(52 + (2 * (level - 1)));
            }
        }
    }

    private void playerKill(DeathEvent event) {
        for (LivingEntity entity : targetsHit) {
            if (entity.is(event.getDeathEvent().getEntity())) {
                this.poisonEssenceExpiry = ticks + 200;
                if (SpellUtil.getSkillHolder(event.getCaster()).hasSkill(SBSkills.SYNTHESIS.value())) {
                    this.addTimedModifier(event.getCaster(), SpellModifier.SYNTHESIS, 120);
                }
                return;
            }
        }
    }*/
}
