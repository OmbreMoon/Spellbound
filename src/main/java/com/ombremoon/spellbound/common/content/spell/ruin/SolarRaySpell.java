package com.ombremoon.spellbound.common.content.spell.ruin;

import com.ombremoon.sentinellib.api.BoxUtil;
import com.ombremoon.sentinellib.api.box.AABBSentinelBox;
import com.ombremoon.sentinellib.api.box.OBBSentinelBox;
import com.ombremoon.sentinellib.api.box.SentinelBox;
import com.ombremoon.sentinellib.common.IPlayerSentinel;
import com.ombremoon.sentinellib.common.ISentinel;
import com.ombremoon.spellbound.common.content.effects.SBEffectInstance;
import com.ombremoon.spellbound.common.content.entity.spell.SolarRay;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellEventListener;
import com.ombremoon.spellbound.common.magic.api.ChanneledSpell;
import com.ombremoon.spellbound.common.magic.events.PlayerAttackEvent;
import com.ombremoon.spellbound.common.magic.events.PlayerDamageEvent;
import com.ombremoon.spellbound.common.magic.sync.SpellDataKey;
import com.ombremoon.spellbound.common.magic.sync.SyncedSpellData;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.tslat.smartbrainlib.util.RandomUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

//TODO: CHANGE MODEL BASED ON SKILL
//TODO: ADD SOLAR BURST ANIMATIONS
//TODO: FIX ATTACK CONDITION

public class SolarRaySpell extends ChanneledSpell {
    protected static final SpellDataKey<Integer> SOLAR_RAY_ID = SyncedSpellData.define(SolarRaySpell.class, DataTypeInit.INT.get());
    private static final List<SentinelBox> BOXES = new ObjectArrayList<>();
    private static final BiFunction<Entity, LivingEntity, Float> POTENCY = (entity, livingEntity) -> {
        float damage = 5F;
        if (entity instanceof LivingEntity living) {
            var handler = SpellUtil.getSpellHandler(living);
            float f = handler.getSpell(SpellInit.SOLAR_RAY.get()).potency();
            damage *= f;
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
            .attackCondition((entity, livingEntity) -> entity instanceof LivingEntity living && !living.isAlliedTo(livingEntity))
            .typeDamage(DamageTypeInit.RUIN_FIRE, POTENCY).build();
    public static final AABBSentinelBox SOLAR_BURST_FRONT = AABBSentinelBox.Builder.of("solar_burst_front")
            .sizeAndOffset(2, 0, 2, 0)
            .noDuration(entity -> false)
            .activeTicks((entity, integer) -> integer % 60 == 0)
            .attackCondition((entity, livingEntity) -> entity instanceof LivingEntity living && !living.isAlliedTo(livingEntity))
            .typeDamage(DamageTypeInit.RUIN_FIRE, POTENCY).build();
    public static final OBBSentinelBox SOLAR_BURST_END = createSolarBurstEnd(false);
    public static final OBBSentinelBox SOLAR_BURST_END_EXTENDED = createSolarBurstEnd(true);
    private final Set<LivingEntity> concentratedHeatSet = new ObjectOpenHashSet<>();
    private final Map<LivingEntity, Integer> heatTracker = new Object2IntOpenHashMap<>();

    public static Builder<ChanneledSpell> createSolarRayBuilder() {
        return createChannelledSpellBuilder().castTime(18);
    }

    public SolarRaySpell() {
        super(SpellInit.SOLAR_RAY.get(), createSolarRayBuilder());
    }

    @Override
    protected void defineSpellData(SyncedSpellData.Builder builder) {
        builder.define(SOLAR_RAY_ID, 0);
    }

    @Override
    public void onCastStart(SpellContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        var handler = context.getSpellHandler();
        handler.setStationary(true);
        if (!level.isClientSide) {
            SolarRay solarRay = EntityInit.SOLAR_RAY.get().create(level);
            if (solarRay != null) {
                this.setSolarRay(solarRay.getId());
                solarRay.setOwner(player);
                solarRay.setPos(player.position());
                solarRay.setYRot(player.getYRot());
                solarRay.setStartTick(18);
                level.addFreshEntity(solarRay);
            }
        }
    }

    @Override
    public void whenCasting(SpellContext context, int castTime) {
        super.whenCasting(context, castTime);
        Player player = context.getPlayer();
        SolarRay solarRay = getSolarRay(context);
        if (solarRay != null) {
            solarRay.setPos(player.position());
            solarRay.setYRot(player.getYRot());
        }
    }

    @Override
    public void onCastReset(SpellContext context) {
        SolarRay solarRay = getSolarRay(context);
        if (solarRay != null) solarRay.discard();
        var handler = context.getSpellHandler();
        handler.setStationary(false);
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        super.onSpellStart(context);
        Player player = context.getPlayer();
        var handler = context.getSpellHandler();
        var skillHandler = context.getSkillHandler();
        if (!context.getLevel().isClientSide){
            boolean flag = skillHandler.hasSkill(SkillInit.SUNSHINE.value());
            BoxUtil.triggerPlayerBox(context.getPlayer(), flag ? SOLAR_RAY_EXTENDED : SOLAR_RAY);

            if (skillHandler.hasSkill(SkillInit.SOLAR_BURST.value())) {
                BoxUtil.triggerPlayerBox(player, SOLAR_BURST_FRONT);
                BoxUtil.triggerPlayerBox(player, flag ? SOLAR_BURST_END_EXTENDED : SOLAR_BURST_END);
            }
        } else {
            handler.setZoom(0.3F);
        }

    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        Player player = context.getPlayer();
        var skillHandler = context.getSkillHandler();
        if (skillHandler.hasSkill(SkillInit.OVERHEAT.value()) && this.ticks >= 100)
            BoxUtil.triggerPlayerBox(player, OVERHEAT);


        SolarRay solarRay = getSolarRay(context);
        if (solarRay != null) {
            solarRay.setYRot(player.getYRot());
            solarRay.setPos(player.position());
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        super.onSpellStop(context);
        var handler = context.getSpellHandler();
        handler.setStationary(false);
        handler.setZoom(1.0F);
        for (SentinelBox box : BOXES) {
            BoxUtil.removePlayerBox(context.getPlayer(), box);
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
                .activeTicks(BoxUtil.PER_SEC)
                .attackCondition((entity, livingEntity) -> !entity.isAlliedTo(livingEntity) || (livingEntity instanceof OwnableEntity ownable && ownable.getOwner() != entity))
                .onCollisionTick((entity, living) -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        var skillHandler = SpellUtil.getSkillHandler(livingEntity);
                        if (skillHandler.hasSkill(SkillInit.HEALING_LIGHT.value()) && living.isAlliedTo(livingEntity))
                            living.heal(2);
                    }
                })
                .onHurtTick((entity, livingEntity) -> {
                    if (entity instanceof Player player) {
                        var skillHandler = SpellUtil.getSkillHandler(player);
                        var handler = SpellUtil.getSpellHandler(player);
                        SolarRaySpell spell = handler.getSpell(SpellInit.SOLAR_RAY.get());
                        if (entity.isAlliedTo(livingEntity) || (livingEntity instanceof OwnableEntity ownable && ownable.getOwner() == entity)) {
                            if (skillHandler.hasSkill(SkillInit.HEALING_LIGHT.value()))
                                livingEntity.heal(2);

                            return;
                        }

                        if (skillHandler.hasSkill(SkillInit.RADIANCE.value())) {
                            var targetHandler = SpellUtil.getSpellHandler(livingEntity);
                            targetHandler.consumeMana(5, true);
                        }

                        if (skillHandler.hasSkill(SkillInit.CONCENTRATED_HEAT.value())) {
                            if (!spell.concentratedHeatSet.contains(livingEntity)) {
                                spell.concentratedHeatSet.add(livingEntity);
                                livingEntity.setData(DataInit.HEAT_TICK, livingEntity.tickCount);
                                spell.heatTracker.put(livingEntity, livingEntity.tickCount);
                            } else {
                                int heatTick = livingEntity.getData(DataInit.HEAT_TICK);
                                if (livingEntity.tickCount == heatTick + 20) {
                                    livingEntity.setData(DataInit.HEAT_TICK, livingEntity.tickCount);
                                } else {
                                    livingEntity.setData(DataInit.HEAT_TICK, 0);
                                    spell.concentratedHeatSet.remove(livingEntity);
                                    spell.heatTracker.remove(livingEntity);
                                }
                            }
                        }

                        if (skillHandler.hasSkill(SkillInit.BLINDING_LIGHT.value()))
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false));

                        if (skillHandler.hasSkill(SkillInit.AFTERGLOW.value()))
                            livingEntity.addEffect(new SBEffectInstance(player, EffectInit.AFTERGLOW, 100, true, 0, false, false));
                    }
                })
                .typeDamage(DamageTypeInit.RUIN_FIRE, (entity, living) -> {
                    float damage = 5F;
                    if (entity instanceof LivingEntity livingEntity) {
                        var handler = SpellUtil.getSpellHandler(livingEntity);
                        var skillHandler = SpellUtil.getSkillHandler(livingEntity);
                        float f = handler.getSpell(SpellInit.SOLAR_RAY.get()).potency();
                        SolarRaySpell spell = handler.getSpell(SpellInit.SOLAR_RAY.get());
                        int startTick = spell.heatTracker.computeIfAbsent(living, target -> 0);
                        int bonus = startTick > 0 && living.tickCount >= startTick + 60 ? 2 : 1;
                        damage *= f * bonus;
                        if (skillHandler.hasSkill(SkillInit.POWER_OF_THE_SUN.value()) && livingEntity.level().isDay())
                            damage *= 1.5F;
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
                .attackCondition(BoxUtil.IS_ALLIED)
                .onBoxTick((entity, instance) -> {
                    Level level = entity.level();
                    if (!(entity instanceof LivingEntity livingEntity)) return;

                    var skillHandler = SpellUtil.getSkillHandler(livingEntity);
                    if (!level.isClientSide) {
                        if (skillHandler.hasSkill(SkillInit.SOLAR_BORE.value())) {
                            Vec3 vec3 = instance.getWorldCenter();
                            if (instance.tickCount % 20 == 0)
                                level.explode(livingEntity, Explosion.getDefaultDamageSource(level, livingEntity), null, vec3.x(), vec3.y(), vec3.z(), 4.0F, true, Level.ExplosionInteraction.TNT);
                        }
                    }
                })
                .typeDamage(DamageTypeInit.RUIN_FIRE, POTENCY).build();
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
