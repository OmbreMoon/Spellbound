package com.ombremoon.spellbound.common.content.entity.living;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.common.content.entity.SmartSpellEntity;
import com.ombremoon.spellbound.common.content.entity.behaviors.ApplySurroundingEffectBehavior;
import com.ombremoon.spellbound.common.content.entity.behaviors.DelayedLeapAtTarget;
import com.ombremoon.spellbound.common.content.spell.summon.SpiritTotemSpell;
import com.ombremoon.spellbound.common.data.SkillHandler;
import com.ombremoon.spellbound.common.init.EffectInit;
import com.ombremoon.spellbound.common.init.EntityInit;
import com.ombremoon.spellbound.common.init.SkillInit;
import com.ombremoon.spellbound.util.SummonUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.SequentialBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.CustomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.AvoidEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowOwner;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.util.BrainUtils;

import java.util.List;

public class TotemSpiritEntity extends SmartSpellEntity {
    private static final ResourceLocation FERAL_DAMAGE = CommonClass.customLocation("feral_damage");
    private static final ResourceLocation FERAL_SPEED = CommonClass.customLocation("feral_speed");
    private static final ResourceLocation TOTEMIC_ARMOR = CommonClass.customLocation("totemic_armor_mod");
    private static final EntityDataAccessor<Boolean> IS_CAT = SynchedEntityData.defineId(TotemSpiritEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_HEALING = SynchedEntityData.defineId(TotemSpiritEntity.class, EntityDataSerializers.BOOLEAN);

    private SpiritTotemSpell spell;
    private int healingCooldown = 0;
    private int formSwapCooldown = 200;
    private boolean isTwin = false;
    private SkillHandler skills;

    public TotemSpiritEntity(EntityType<? extends TotemSpiritEntity> entityType, Level level) {
        super(entityType, level);
    }

    public boolean canHeal() {
        return healingCooldown <= 0;
    }

    public void setHealingCooldown(int cooldown) {
        this.healingCooldown = cooldown;
    }

    public boolean isCatForm() {
        return this.entityData.get(IS_CAT);
    }

    public void switchForm() {
        if (isCatForm()) {
            this.entityData.set(IS_CAT, false);
            if (skills.hasSkill(SkillInit.CATS_AGILITY.value()))
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1));

            if (skills.hasSkill(SkillInit.FERAL_FURY.value())) {
                this.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(FERAL_DAMAGE);
                this.getAttribute(Attributes.ATTACK_SPEED).removeModifier(FERAL_SPEED);
            }


            if (skills.hasSkill(SkillInit.TOTEMIC_ARMOR.value())) {
                this.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(
                        TOTEMIC_ARMOR, 1.25d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        } else {
            this.entityData.set(IS_CAT, true);
            if (skills.hasSkill(SkillInit.CATALEPSY.value()))
                this.removeEffect(MobEffects.MOVEMENT_SPEED);

            if (skills.hasSkill(SkillInit.FERAL_FURY.value())) {
                this.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new AttributeModifier(
                        FERAL_DAMAGE, 1.1d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
                this.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(new AttributeModifier(
                        FERAL_SPEED, 1.1d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }

            if (skills.hasSkill(SkillInit.TOTEMIC_ARMOR.value())) {
                this.getAttribute(Attributes.ARMOR).removeModifier(TOTEMIC_ARMOR);
            }
        }
    }

    public boolean canSwitchForm() {
        if (formSwapCooldown > 0 || isTwin) return false;

        if (isCatForm()) return getHealth() > getMaxHealth() * 0.75f;
        else return getHealth() <= getMaxHealth() * 0.75f;
    }

    public void setTwin(boolean isTwin) {
        this.isTwin = isTwin;
    }

    public void initSpell(SkillHandler skills, SpiritTotemSpell spell) {
        this.skills = skills;
        this.spell = spell;
    }

    public void setHealing(boolean healing) {
        this.entityData.set(IS_HEALING, healing);
    }

    public boolean isHealing() {
        return this.entityData.get(IS_HEALING);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_CAT, false);
        builder.define(IS_HEALING, false);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.isCatForm()) {
            if (skills.hasSkill(SkillInit.TOTEMIC_ARMOR.value())) {
                this.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(
                        TOTEMIC_ARMOR, 1.25d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        }
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (this.deathTime < 20 || this.level().isClientSide()) return;

        if (skills.hasSkillReady(SkillInit.NINE_LIVES.value())) {
//            TotemSpiritEntity entity = EntityInit.TOTEM_SPIRIT.get().create(this.level());
//            if (entity == null) return;
//            SummonUtil.setOwner(entity, spell.getCastContext().getPlayer());
//            entity.initSpell(skills, spell);
//            entity.setHealth(entity.getMaxHealth()/2);
//            spell.getSummons().remove(this.getId());
//            spell.getSummons().add(entity.getId());
        } else {
            spell.endSpell();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (formSwapCooldown > 0) formSwapCooldown--;
        }

        if (isHealing() && tickCount % 20 == 0) {
            if (level().isClientSide) {
                //visual stuff
            } else {
                float heal = 0.1f;
                if (skills.hasSkill(SkillInit.PRIMAL_RESILIENCE.value()))
                    heal += getMaxHealth() * 0.05f;

                this.heal(heal);
                if (skills.hasSkill(SkillInit.TOTEMIC_BOND.value()))
                    getOwner().heal(heal);
            }
        }

        if (isCatForm() && skills.hasSkillReady(SkillInit.STEALTH_TACTIC.value()) && getHealth() < getMaxHealth() * 0.25f) {
            this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200)); //10 seconds
            skills.getCooldowns().addCooldown(SkillInit.STEALTH_TACTIC.value(), 800);//40 seconds
        }
    }

    @Override
    public List<? extends ExtendedSensor<? extends SmartSpellEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyLivingEntitySensor<>(),
                new HurtBySensor<>()
        );
    }

    @Override
    public BrainActivityGroup<? extends SmartSpellEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new MoveToWalkTarget<>()
        );
    }

    @Override
    public BrainActivityGroup<? extends SmartSpellEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(
                new FirstApplicableBehaviour<>(
                    new TargetOrRetaliate<>()
                            .attackablePredicate(summonAttackPredicate()),
                    new LookAtTarget<>(),
                    new SetRandomLookTarget<>()),
                new FollowOwner<>()
        );
    }

    @Override
    public BrainActivityGroup<SmartSpellEntity> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget<>(),
                new FirstApplicableBehaviour<>(
                        new CustomBehaviour<TotemSpiritEntity>(entity -> {
                            entity.switchForm();
                            entity.formSwapCooldown = 200;
                        }).startCondition(entity -> {
                            if (entity.formSwapCooldown <= 0) {
                                if (entity.isCatForm()) return entity.getHealth() > getMaxHealth() * 0.75f;
                                if (entity.getHealth() <= getMaxHealth() * 0.75f) return true;
                            }
                            return false;
                        }),
                        catBehaviours()
                                .startCondition(TotemSpiritEntity::isCatForm),
                        warriorBehaviours()
                                .startCondition(entity -> !(entity).isCatForm())
                )

        );
    }

    protected OneRandomBehaviour<TotemSpiritEntity> catBehaviours() {
        return new OneRandomBehaviour(
                new SequentialBehaviour(
                        new SetWalkTargetToAttackTarget<>(),
                        new AnimatableMeleeAttack<>(20)
                                .startCondition(mob -> mob.distanceTo(mob.getTarget()) < 2f)
                ),
                new FirstApplicableBehaviour<>(//Not sure if this is gonna work as intended
                        new CustomBehaviour<TotemSpiritEntity>(mob -> {
                            mob.setHealing(true);
                            BrainUtils.clearMemory(mob, MemoryModuleType.WALK_TARGET);})
                                .startCondition(mob -> BrainUtils.getTargetOfEntity(mob) == null || mob.distanceTo(BrainUtils.getTargetOfEntity(mob)) >= 10)
                                .runFor(mob -> 60)
                                .whenStopping(mob -> mob.setHealing(false)),
                        new AvoidEntity<>()
                                .avoiding(mob -> BrainUtils.getTargetOfEntity(mob) != null && mob.is(BrainUtils.getTargetOfEntity(mob)))
                                .noCloserThan(10)
                ).startCondition(TotemSpiritEntity::canHeal)
        );
    }

    protected SequentialBehaviour<TotemSpiritEntity> warriorBehaviours() {
        return new SequentialBehaviour<>(
                new SetWalkTargetToAttackTarget<>(),
                new FirstApplicableBehaviour<>(
                        new DelayedLeapAtTarget<TotemSpiritEntity>(20, 40) //Need to modify to apply knockback
                                .startCondition(mob ->
                                        mob.skills.hasSkillReady(SkillInit.SAVAGE_LEAP.value())
                                                && BrainUtils.getTargetOfEntity(mob) != null
                                                && mob.distanceTo(BrainUtils.getTargetOfEntity(mob)) >= 10f),
                        new ApplySurroundingEffectBehavior<TotemSpiritEntity>(new MobEffectInstance(EffectInit.BATTLE_CRY, 200))
                                .areaOf(e -> e.getBoundingBox().inflate(5d))
                                .applyPredicate(this::isAlliedTo)
                                .runFor(mob -> 60)
                                .startCondition(mob -> mob.skills.hasSkillReady(SkillInit.WARRIORS_ROAR.value()))
                                .whenStarting(mob ->
                                        mob.skills.getCooldowns().addCooldown(SkillInit.WARRIORS_ROAR.value(), 600)),
                        new AnimatableMeleeAttack<>(20)
                                .startCondition(mob -> mob.distanceTo(mob.getTarget()) < 2f)
                )
        );
    }

}
