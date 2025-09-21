package com.ombremoon.spellbound.common.content.spell.ruin.shock;

import com.ombremoon.spellbound.common.content.entity.ISpellEntity;
import com.ombremoon.spellbound.common.content.entity.spell.StormBolt;
import com.ombremoon.spellbound.common.content.entity.spell.StormCloud;
import com.ombremoon.spellbound.common.content.entity.spell.StormRift;
import com.ombremoon.spellbound.common.init.*;
import com.ombremoon.spellbound.common.magic.SpellContext;
import com.ombremoon.spellbound.common.magic.SpellMastery;
import com.ombremoon.spellbound.common.magic.api.AnimatedSpell;
import com.ombremoon.spellbound.common.magic.api.buff.BuffCategory;
import com.ombremoon.spellbound.common.magic.api.buff.ModifierData;
import com.ombremoon.spellbound.common.magic.api.buff.SkillBuff;
import com.ombremoon.spellbound.main.CommonClass;
import com.ombremoon.spellbound.util.portal.PortalInfo;
import com.ombremoon.spellbound.util.portal.PortalMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

public class StormRiftSpell extends AnimatedSpell {
    public static Builder<StormRiftSpell> createStormRiftBuilder() {
        return createSimpleSpellBuilder(StormRiftSpell.class)
                .mastery(SpellMastery.MASTER)
                .manaCost(50)
                .duration(400)
                .baseDamage(8)
                .castCondition((context, spell) -> {
                    Entity entity = context.getTarget();
                    if (context.hasSkill(SBSkills.IMPLOSION) && entity instanceof StormRift stormRift && stormRift.tickCount >= 100 && context.hasCatalyst(SBItems.STORM_SHARD.get()) && spell.portalMap.containsKey(stormRift.getId()))
                        return true;

                    if (context.hasSkill(SBSkills.ORBITAL_SHELL) && entity instanceof StormRift stormRift && context.hasCatalyst(SBItems.STORM_SHARD.get()) && spell.portalMap.containsKey(stormRift.getId()))
                        return true;

                    int activePortals = spell.portalMap.size();
                    BlockPos blockPos = spell.getSpawnPos(50);
                    if (blockPos == null) return false;

                    if (!context.getLevel().getBlockState(blockPos).isAir()) return false;
                    if (activePortals > 1) {
                        PortalInfo info = spell.portalMap.get(spell.portalMap.getPreviousPortal());
                        double distance = info.position().distanceToSqr(blockPos.getCenter());
                        return !(distance > 2500);
                    }

                    return true;
                })
                .fullRecast()
                .skipEndOnRecast();
    }

    private static final ResourceLocation MAGNETIC_FIELD = CommonClass.customLocation("magnetic_field");
    private static final ResourceLocation MOTION_SICKNESS = CommonClass.customLocation("motion_sickness");
    private final PortalMap<StormRift> portalMap = new PortalMap<>();
    private final IntOpenHashSet thrownEntities = new IntOpenHashSet();
    private IntOpenHashSet stormClouds = new IntOpenHashSet();
    private int portalCharge;
    private int lightningTimer = 60;

    public StormRiftSpell() {
        super(SBSpells.STORM_RIFT.get(), createStormRiftBuilder());
    }

    @Override
    protected void onSpellStart(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            Entity entity = context.getTarget();
            if (context.hasSkill(SBSkills.IMPLOSION) && entity instanceof StormRift stormRift && stormRift.tickCount >= 100 && context.hasCatalyst(SBItems.STORM_SHARD.get()) && this.portalMap.containsKey(stormRift.getId())) {
                stormRift.implode();
                context.useCatalyst(SBItems.STORM_SHARD.get());
                this.portalMap.remove(stormRift.getId());
                if (portalMap.isEmpty())
                    endSpell();
            } else if (context.hasSkill(SBSkills.ORBITAL_SHELL) && entity instanceof StormRift stormRift && context.hasCatalyst(SBItems.STORM_SHARD.get()) && this.portalMap.containsKey(stormRift.getId())) {
                context.useCatalyst(SBItems.STORM_SHARD.get());
                stormRift.setCenter(stormRift.getOnPos());
                stormRift.allowRotation();
            } else if (!(entity instanceof StormRift)) {
                StormRift stormRift = this.summonEntity(context, SBEntities.STORM_RIFT.get(), 20, rift -> {
                    this.portalMap.createOrShiftPortal(rift, 2, 0, portal -> {
                        if (portal instanceof StormRift shiftedRift) {
                            Entity entity1 = level.getEntity(shiftedRift.getCloudId());
                            if (entity1 instanceof StormCloud cloud)
                                cloud.discard();
                        }
                    });

                    if (context.getSkills().hasSkill(SBSkills.STORM_FURY))
                        rift.allowGrowth();
                });
                if (context.hasSkill(SBSkills.STORM_CALLER) && stormRift != null) {
                    this.summonEntity(context, SBEntities.STORM_CLOUD.get(), stormRift.position().add(0, 7, 0), stormCloud -> {
                        this.stormClouds.add(stormCloud.getId());
                        stormRift.setCloudId(stormCloud.getId());
                    });
                }

            }
        }
    }

    @Override
    protected void onSpellTick(SpellContext context) {
        super.onSpellTick(context);
        LivingEntity caster = context.getCaster();
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            for (var entry : this.portalMap.entrySet()) {
                StormRift stormRift = (StormRift) level.getEntity(entry.getKey());
                if (stormRift != null) {
                    float damage = context.hasSkill(SBSkills.STORM_FURY) && stormRift.tickCount >= 100 ? 10 : 5;
                    damage += Math.min(this.portalCharge, 5);
                    if (this.portalMap.size() > 1 || context.hasSkill(SBSkills.DISPLACEMENT_FIELD))
                        this.pullTargets(context);

                    List<Entity> teleportList = level.getEntities(stormRift, stormRift.getBoundingBox(), EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(entity -> entity.isAlive() && ((entity instanceof LivingEntity livingEntity && !isCaster(livingEntity) && !livingEntity.isAlliedTo(caster)) || entity instanceof Projectile)));
                    for (Entity entity : teleportList) {
                        if (entity instanceof LivingEntity livingEntity && checkForCounterMagic(livingEntity))
                            return;

                        if (this.portalMap.attemptTeleport(entity, stormRift)) {
                            if (context.hasSkill(SBSkills.CHARGED_RIFT))
                                this.portalCharge++;

                            if (entity instanceof LivingEntity livingEntity) {
                                stormRift.addCooldown(livingEntity);
                                this.hurt(livingEntity, damage);
                                this.drainMana(livingEntity, damage);
                                if (context.hasSkill(SBSkills.EVENT_HORIZON))
                                    this.quicklyPullTargets(level, stormRift, caster, livingEntity);

                                if (context.hasSkill(SBSkills.FORCED_WARP))
                                    this.throwTarget(level, livingEntity);

                                if (context.hasSkill(SBSkills.MOTION_SICKNESS)) {
                                    this.addSkillBuff(
                                            livingEntity,
                                            SBSkills.MOTION_SICKNESS,
                                            BuffCategory.HARMFUL,
                                            SkillBuff.ATTRIBUTE_MODIFIER,
                                            new ModifierData(Attributes.MOVEMENT_SPEED, new AttributeModifier(MOTION_SICKNESS, -0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                                            200
                                    );
                                    this.addSkillBuff(
                                            livingEntity,
                                            SBSkills.MOTION_SICKNESS,
                                            BuffCategory.HARMFUL,
                                            SkillBuff.ATTRIBUTE_MODIFIER,
                                            new ModifierData(Attributes.ATTACK_SPEED, new AttributeModifier(MOTION_SICKNESS, -0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                                            200
                                    );
                                    this.addSkillBuff(
                                            livingEntity,
                                            SBSkills.MOTION_SICKNESS,
                                            BuffCategory.HARMFUL,
                                            SkillBuff.ATTRIBUTE_MODIFIER,
                                            new ModifierData(Attributes.BLOCK_BREAK_SPEED, new AttributeModifier(MOTION_SICKNESS, -0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                                            200
                                    );
                                }
                            }
                        } else if (entity instanceof LivingEntity livingEntity && context.hasSkill(SBSkills.DISPLACEMENT_FIELD) && !stormRift.isOnCooldown(livingEntity)) {
                            int i = Mth.floor(livingEntity.getX());
                            int j = Mth.floor(livingEntity.getY());
                            int k = Mth.floor(livingEntity.getZ());
                            for (int l = 0; l < 50; ++l) {
                                int i1 = i + Mth.nextInt(livingEntity.getRandom(), 7, 15) * Mth.nextInt(livingEntity.getRandom(), -1, 1);
                                int j1 = j + Mth.nextInt(livingEntity.getRandom(), 7, 15) * Mth.nextInt(livingEntity.getRandom(), -1, 1);
                                int k1 = k + Mth.nextInt(livingEntity.getRandom(), 7, 15) * Mth.nextInt(livingEntity.getRandom(), -1, 1);
                                BlockPos blockpos = new BlockPos(i1, j1, k1);
                                if (level.getBlockState(blockpos).isAir()) {
                                    livingEntity.teleportTo(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                                    this.hurt(livingEntity, damage);
                                    this.drainMana(livingEntity, 15);
                                    break;
                                }
                            }

                            if (context.hasSkill(SBSkills.EVENT_HORIZON))
                                this.quicklyPullTargets(level, stormRift, caster, livingEntity);

                            if (context.hasSkill(SBSkills.FORCED_WARP) && !stormRift.isOnCooldown(livingEntity))
                                this.throwTarget(level, livingEntity);
                        }
                    }
                }
            }

            for (Integer id : thrownEntities) {
                Entity entity = level.getEntity(id);
                if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                    if (!livingEntity.onGround())
                        livingEntity.setData(SBData.FORCE_WARP, true);

                    if (livingEntity.getData(SBData.FORCE_WARP)) {
                        if (livingEntity.horizontalCollision) {
                            this.hurt(livingEntity, livingEntity.damageSources().flyIntoWall(), 7.0F);
                            this.thrownEntities.remove(id);
                            livingEntity.setData(SBData.FORCE_WARP, false);
                        } else if (livingEntity.onGround()) {
                            this.thrownEntities.remove(id);
                            livingEntity.setData(SBData.FORCE_WARP, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onSpellStop(SpellContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            this.portalMap.forEach((id, portalInfo) -> {
                Entity entity = context.getLevel().getEntity(id);
                if (entity instanceof StormRift stormRift)
                    stormRift.setEndTick(20);
            });
            this.stormClouds.forEach(id -> {
                Entity entity = level.getEntity(id);
                if (entity instanceof StormCloud stormCloud)
                    stormCloud.setEndTick(10);
            });
        }
    }

    @Override
    public void onEntityTick(ISpellEntity<?> spellEntity, SpellContext context) {
        Level level = context.getLevel();
        if (spellEntity instanceof StormCloud stormCloud) {
            if (this.lightningTimer > 0) {
                this.lightningTimer--;
            } else {
                this.lightningTimer = 40 + stormCloud.getRandom().nextInt(40);
                double x = stormCloud.getX() + (stormCloud.getRandom().nextDouble() * 8 - 4);
                double y = stormCloud.getY() - 7;
                double z = stormCloud.getZ() + (stormCloud.getRandom().nextDouble() * 8 - 4);
                BlockPos blockPos = BlockPos.containing(x, y, z);
                this.summonEntity(context, SBEntities.STORM_BOLT.get(), Vec3.atBottomCenterOf(blockPos));
                level.playSound(
                        stormCloud,
                        stormCloud.getOnPos(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundSource.NEUTRAL,
                        2500.0F,
                        0.8F + stormCloud.getRandom().nextFloat() * 0.2F
                );
                level.playSound(
                        stormCloud,
                        stormCloud.getOnPos(),
                        SoundEvents.LIGHTNING_BOLT_IMPACT,
                        SoundSource.NEUTRAL,
                        2.0F,
                        0.5F + stormCloud.getRandom().nextFloat() * 0.2F
                );
            }
        } else if (spellEntity instanceof StormBolt stormBolt) {
            List<Entity> list = level
                    .getEntities(
                            stormBolt,
                            new AABB(stormBolt.getX() - 5.0, stormBolt.getY() - 3.0, stormBolt.getZ() - 5.0, stormBolt.getX() + 5.0, stormBolt.getY() + 6.0 + 3.0, stormBolt.getZ() + 5.0),
                            Entity::isAlive
                    );

            for (Entity entity : list) {
                if (!isCaster(entity)) {
                    if (entity instanceof LivingEntity target && this.hurt(target, DamageTypes.LIGHTNING_BOLT, 5.0F)) {
                        target.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
                        if (target.getRemainingFireTicks() == 0)
                            target.igniteForSeconds(8.0F);
                    }

                }
            }

            if (stormBolt.tickCount >= 15)
                stormBolt.discard();
        }
    }

    private void pullTargets(SpellContext context) {
        Level level = context.getLevel();
        LivingEntity caster = context.getCaster();
        for (var entry : this.portalMap.entrySet()) {
            StormRift stormRift = (StormRift) level.getEntity(entry.getKey());

            if (stormRift != null) {
                double range = (stormRift.getBbWidth() / 2) + stormRift.getBbWidth() * 1.5F;
                List<LivingEntity> pullList = level.getEntitiesOfClass(LivingEntity.class, stormRift.getBoundingBox().inflate(range), EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(entity -> entity instanceof LivingEntity livingEntity && !checkForCounterMagic(livingEntity) && !isCaster(livingEntity) && !livingEntity.isAlliedTo(caster)));
                for (LivingEntity livingEntity : pullList) {
                    if (!stormRift.isOnCooldown(livingEntity)) {
                        float strength = 0.03F;
                        if (context.hasSkill(SBSkills.MAGNETIC_FIELD)) {
                            strength = 0.06F;
                            this.addSkillBuff(
                                    livingEntity,
                                    SBSkills.MAGNETIC_FIELD,
                                    BuffCategory.HARMFUL,
                                    SkillBuff.ATTRIBUTE_MODIFIER,
                                    new ModifierData(Attributes.ARMOR, new AttributeModifier(MAGNETIC_FIELD, 0.75F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)),
                                    100
                            );
                        }
                        float distance = (float) livingEntity.distanceToSqr(stormRift);
                        strength = distance > Mth.square(range / 2) ? strength * 0.5F : strength;
                        Vec3 direction = stormRift.getBoundingBox().getCenter().subtract(livingEntity.position()).normalize().scale(strength);
                        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(direction));
                        livingEntity.hurtMarked = true;
                    }
                }
            }
        }
    }

    private void quicklyPullTargets(Level level, StormRift stormRift, LivingEntity caster, LivingEntity excludedEntity) {
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, stormRift.getBoundingBox().inflate(10), EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(entity -> entity.isAlive() && entity instanceof LivingEntity living && !isCaster(living) && !living.isAlliedTo(caster)));
        for (LivingEntity living : list) {
            if (!living.is(excludedEntity)) {
                Vec3 direction = stormRift.getBoundingBox().getCenter().subtract(living.position()).normalize().scale(0.6F);
                living.setDeltaMovement(living.getDeltaMovement().add(direction));
                living.hurtMarked = true;
            }
        }
    }

    private void throwTarget(Level level, LivingEntity livingEntity) {
        Vec3 direction = new Vec3(Math.max(level.random.nextFloat(), 0.5F), 0.75, Math.max(level.random.nextFloat(), 0.5F)).scale(1.2);
        livingEntity.setDeltaMovement(direction);
        livingEntity.hurtMarked = true;
        this.thrownEntities.add(livingEntity.getId());
    }

    @Override
    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        this.portalMap.serialize(compoundTag);
        compoundTag.putInt("Charge", this.portalCharge);
        compoundTag.putIntArray("StormClouds", this.stormClouds.stream().toList());
        return compoundTag;
    }

    @Override
    public void loadData(CompoundTag nbt) {
        this.portalMap.deserialize(nbt);
        this.portalCharge = nbt.getInt("Charge");
        this.stormClouds = new IntOpenHashSet(nbt.getIntArray("StormClouds"));
    }
}
