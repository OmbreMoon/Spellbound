package com.ombremoon.spellbound.common.content.spell.ruin.fire;

import com.ombremoon.sentinellib.api.BoxUtil;
import com.ombremoon.sentinellib.api.box.AABBSentinelBox;
import com.ombremoon.sentinellib.api.box.OBBSentinelBox;
import com.ombremoon.sentinellib.api.box.SentinelBox;
import com.ombremoon.sentinellib.common.ISentinel;
import com.ombremoon.spellbound.common.content.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import com.ombremoon.spellbound.common.magic.api.buff.BuffCategory;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.common.magic.api.buff.SpellModifier;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.util.RandomUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

//TODO: CHANGE MODEL BASED ON SKILL
//TODO: ADD SOLAR BURST ANIMATIONS
//TODO: FIX ATTACK CONDITION

public class SolarRaySpell extends ChanneledSpell {
    protected static final SpellDataKey<Integer> SOLAR_RAY_ID = SyncedSpellData.registerDataKey(SolarRaySpell.class, SBDataTypes.INT.get());
    private static final List<SentinelBox> BOXES = new ObjectArrayList<>();
    private static final BiFunction<Entity, LivingEntity, Float> POTENCY = (entity, livingEntity) -> {
        float damage = 5F;
        if (entity instanceof LivingEntity living) {
            var handler = SpellUtil.getSpellHandler(living);
            SolarRaySpell spell = handler.getSpell(SBSpells.SOLAR_RAY.get());
            damage = spell.potency(damage);
            if (spell.checkForCounterMagic(livingEntity))
                damage = 0;
        }
        return damage;
    };
    public static final OBBSentinelBox SOLAR_RAY = createSolarRay(false);
    public static final OBBSentinelBox SOLAR_RAY_EXTENDED = createSolarRay(true);
    public static final AABBSentinelBox OVERHEAT = AABBSentinelBox.Builder.of("overheat")
            .sizeAndOffset(3, 0, 1.5F, 0)
            .noDuration(entity -> false)
            .onBoxTick((entity, boxInstance) -> {
                Vec3 vec3 = boxInstance.getWorldCenter();
                if (entity.level().isClientSide) {
                    double x = vec3.x;
                    double y = vec3.y;
                    double z = vec3.z;
                    for (int i = 0; i < 15; i++) {
                        entity.level().addParticle(ParticleTypes.FLAME,
                                x + RandomUtil.randomValueBetween(-3, 3),
                                y + RandomUtil.randomValueBetween(-3, 3),
                                z + RandomUtil.randomValueBetween(-3, 3),
                                entity.getRandom().nextDouble() * 0.05,
                                entity.getRandom().nextDouble() * 0.05,
                                entity.getRandom().nextDouble() * 0.05);
                    }
                }
            })
            .activeTicks((entity, integer) -> integer == 1 || integer % 20 == 0)
            .attackCondition((entity, livingEntity) -> !entity.isAlliedTo(livingEntity) || !livingEntity.hasEffect(SBEffects.COUNTER_MAGIC) || (livingEntity instanceof OwnableEntity ownable && ownable.getOwner() != entity))
            .typeDamage(SBDamageTypes.RUIN_FIRE, POTENCY).build();
    public static final AABBSentinelBox SOLAR_BURST_FRONT = AABBSentinelBox.Builder.of("solar_burst_front")
            .sizeAndOffset(2, 0, 2, 0)
            .noDuration(entity -> false)
            .activeTicks((entity, integer) -> integer % 60 == 0)
            .attackCondition((entity, livingEntity) -> entity instanceof LivingEntity living && !living.isAlliedTo(livingEntity))
            .typeDamage(SBDamageTypes.RUIN_FIRE, POTENCY).build();
    public static final OBBSentinelBox SOLAR_BURST_END = createSolarBurstEnd(false);
    public static final OBBSentinelBox SOLAR_BURST_END_EXTENDED = createSolarBurstEnd(true);
    private final Set<LivingEntity> concentratedHeatSet = new ObjectOpenHashSet<>();
    private final Map<LivingEntity, Integer> heatTracker = new Object2IntOpenHashMap<>();

    public static Builder<SolarRaySpell> createSolarRayBuilder() {
        return createChannelledSpellBuilder(SolarRaySpell.class).castTime(18).castAnimation(context -> "solar_ray1");
    }

    public SolarRaySpell() {
        super(SBSpells.SOLAR_RAY.get(), createSolarRayBuilder());
    }

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        builder.define(SOLAR_RAY_ID, 0);
    }

    @Override
    public void onCastStart(SpellContext context) {
        super.onCastStart(context);
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        if (!level.isClientSide) {
            SolarRay solarRay = SBEntities.SOLAR_RAY.get().create(level);
            if (solarRay != null) {
                this.setSolarRay(solarRay.getId());
                solarRay.setOwner(caster);
                solarRay.setPos(caster.position());
                solarRay.setYRot(caster.getYRot());
                solarRay.setStartTick(18);
                level.addFreshEntity(solarRay);
            }
        }
    }

    @Override
    public void whenCasting(SpellContext context, int castTime) {
        super.whenCasting(context, castTime);
        LivingEntity caster = context.getCaster();
        var handler = context.getSpellHandler();
        handler.setStationaryTicks(1);

        SolarRay solarRay = getSolarRay(context);
        if (solarRay != null) {
            solarRay.setPos(caster.position());
            solarRay.setYRot(caster.getYRot());
        }
    }

    @Override
    public void onCastReset(SpellContext context) {
        super.onCastReset(context);
        SolarRay solarRay = getSolarRay(context);
        if (solarRay != null) solarRay.discard();
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        LivingEntity caster = context.getCaster();
        var skills = context.getSkills();
        if (!context.getLevel().isClientSide) {
            boolean flag = skills.hasSkill(SBSkills.SUNSHINE.value());
            ((ISentinel)caster).triggerSentinelBox(flag ? SOLAR_RAY_EXTENDED : SOLAR_RAY);

            if (skills.hasSkill(SBSkills.SOLAR_BURST.value())) {
                ((ISentinel)caster).triggerSentinelBox(SOLAR_BURST_FRONT);
                ((ISentinel)caster).triggerSentinelBox(flag ? SOLAR_BURST_END_EXTENDED : SOLAR_BURST_END);
            }
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity caster = context.getCaster();
        var handler = context.getSpellHandler();
        var skills = context.getSkills();
        if (skills.hasSkill(SBSkills.OVERHEAT.value()) && this.ticks >= 100)
            ((ISentinel)caster).triggerSentinelBox(OVERHEAT);


        SolarRay solarRay = getSolarRay(context);
        if (solarRay != null) {
            solarRay.setYRot(caster.getYRot());
            solarRay.setPos(caster.position());
        }

        if (context.getLevel().isClientSide && caster instanceof Player player) {
            shakeScreen(player, 10, 5);
            handler.setStationaryTicks(1);
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        var handler = context.getSpellHandler();
        handler.setStationaryTicks(16);
        for (SentinelBox box : BOXES) {
            ((ISentinel) context.getCaster()).removeSentinelInstance(box);
        }

        SolarRay solarRay = getSolarRay(context);
        if (solarRay != null)
            solarRay.setEndTick(15);

    }

    private void setSolarRay(int solarRay) {
        this.spellData.set(SOLAR_RAY_ID, solarRay);
    }

    private SolarRay getSolarRay(SpellContext context) {
        Entity entity = context.getLevel().getEntity(this.spellData.get(SOLAR_RAY_ID));
        return entity instanceof SolarRay solarRay ? solarRay : null;
    }

    private static OBBSentinelBox createSolarRay(boolean isExtended) {
        String name = "solar_ray";
        float range = isExtended ? 7.7F : 3.85F;
        return OBBSentinelBox.Builder.of(isExtended ? name + "_extended" : name)
                .sizeAndOffset(0.75F, 0.75F, range, 0.0F, 1.7F, range)
                .noDuration(entity -> false)
                .activeTicks((entity, integer) -> integer % 10 == 1)
                .attackCondition((entity, livingEntity) -> !entity.isAlliedTo(livingEntity) || !livingEntity.hasEffect(SBEffects.COUNTER_MAGIC) || (livingEntity instanceof OwnableEntity ownable && ownable.getOwner() != entity))
                .onCollisionTick((entity, living) -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        var skills = SpellUtil.getSkillHolder(livingEntity);
                        if (skills.hasSkill(SBSkills.HEALING_LIGHT.value()) && living.isAlliedTo(livingEntity))
                            living.heal(2);
                    }
                })
                .onHurtTick((entity, livingEntity) -> {
                    if (entity instanceof LivingEntity caster) {
                        var skills = SpellUtil.getSkillHolder(caster);
                        var handler = SpellUtil.getSpellHandler(caster);
                        SolarRaySpell spell = handler.getSpell(SBSpells.SOLAR_RAY.get());
                        if (entity.isAlliedTo(livingEntity) || (livingEntity instanceof OwnableEntity ownable && ownable.getOwner() == entity)) {
                            if (skills.hasSkill(SBSkills.HEALING_LIGHT.value()))
                                livingEntity.heal(2);

                            return;
                        }

                        if (skills.hasSkill(SBSkills.RADIANCE.value())) {
                            var targetHandler = SpellUtil.getSpellHandler(livingEntity);
                            targetHandler.consumeMana(5, true);
                        }

                        if (skills.hasSkill(SBSkills.CONCENTRATED_HEAT.value())) {
                            if (!spell.concentratedHeatSet.contains(livingEntity)) {
                                spell.concentratedHeatSet.add(livingEntity);
                                livingEntity.setData(SBData.HEAT_TICK, livingEntity.tickCount);
                                spell.heatTracker.put(livingEntity, livingEntity.tickCount);
                            } else {
                                int heatTick = livingEntity.getData(SBData.HEAT_TICK);
                                if (livingEntity.tickCount == heatTick + 20) {
                                    livingEntity.setData(SBData.HEAT_TICK, livingEntity.tickCount);
                                } else {
                                    livingEntity.setData(SBData.HEAT_TICK, 0);
                                    spell.concentratedHeatSet.remove(livingEntity);
                                    spell.heatTracker.remove(livingEntity);
                                }
                            }
                        }

                        if (skills.hasSkill(SBSkills.BLINDING_LIGHT.value()))
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));

                        if (skills.hasSkill(SBSkills.AFTERGLOW.value())) {
                            livingEntity.addEffect(new SBEffectInstance(caster, SBEffects.AFTERGLOW, 100, true, 0, false, false));
                            spell.addSkillBuff(
                                    caster,
                                    SBSkills.AFTERGLOW.value(),
                                    BuffCategory.HARMFUL,
                                    SkillBuff.SPELL_MODIFIER,
                                    SpellModifier.AFTERGLOW,
                                    100
                            );
                        }
                    }
                })
                .typeDamage(SBDamageTypes.RUIN_FIRE, (entity, living) -> {
                    float damage = 5F;
                    if (entity instanceof LivingEntity livingEntity) {
                        var handler = SpellUtil.getSpellHandler(livingEntity);
                        var skills = SpellUtil.getSkillHolder(livingEntity);
                        SolarRaySpell spell = handler.getSpell(SBSpells.SOLAR_RAY.get());
                        damage = spell.potency(damage);
                        int startTick = spell.heatTracker.computeIfAbsent(living, target -> 0);
                        int bonus = startTick > 0 && living.tickCount >= startTick + 60 ? 2 : 1;
                        damage *= bonus;
                        if (skills.hasSkill(SBSkills.POWER_OF_THE_SUN.value()) && livingEntity.level().isDay())
                            damage *= 1.5F;

                        if (spell.checkForCounterMagic(living))
                            damage = 0;
                    }
                    return damage;
                }).build();
    }

    private static OBBSentinelBox createSolarBurstEnd(boolean isExtended) {
        String name = "solar_burst_end";
        String newName = isExtended ? name + "_extended" : name;
        return OBBSentinelBox.Builder.of(newName)
                .sizeAndOffset(2, 0, 2, 9.2F)
                .noDuration(entity -> false)
                .activeTicks((entity, integer) -> integer % 60 == 0)
                .attackCondition((entity, livingEntity) -> !entity.isAlliedTo(livingEntity) || !livingEntity.hasEffect(SBEffects.COUNTER_MAGIC) || (livingEntity instanceof OwnableEntity ownable && ownable.getOwner() != entity))
                .onBoxTick((entity, instance) -> {
                    Level level = entity.level();
                    if (!(entity instanceof LivingEntity livingEntity)) return;

                    var skills = SpellUtil.getSkillHolder(livingEntity);
                    if (!level.isClientSide) {
                        if (skills.hasSkill(SBSkills.SOLAR_BORE.value())) {
                            Vec3 vec3 = instance.getWorldCenter();
                            if (instance.tickCount % 20 == 0)
                                level.explode(livingEntity, Explosion.getDefaultDamageSource(level, livingEntity), null, vec3.x(), vec3.y(), vec3.z(), 4.0F, true, Level.ExplosionInteraction.TNT);
                        }
                    }
                })
                .typeDamage(SBDamageTypes.RUIN_FIRE, POTENCY).build();
    }

    static {
        BOXES.add(SOLAR_RAY);
        BOXES.add(SOLAR_RAY_EXTENDED);
        BOXES.add(OVERHEAT);
        BOXES.add(SOLAR_BURST_FRONT);
        BOXES.add(SOLAR_BURST_END);
        BOXES.add(SOLAR_BURST_END_EXTENDED);
    }
}
