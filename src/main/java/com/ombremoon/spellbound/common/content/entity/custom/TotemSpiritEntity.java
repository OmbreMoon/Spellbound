package com.ombremoon.spellbound.common.content.entity.custom;

import com.ombremoon.spellbound.common.content.entity.SmartSpellEntity;
import com.ombremoon.spellbound.common.content.entity.behaviors.ApplySurroundingEffectBehavior;
import com.ombremoon.spellbound.common.init.EffectInit;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.SequentialBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.LeapAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
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

public class TotemSpiritEntity extends SmartSpellEntity<TotemSpiritEntity> {
    private static final EntityDataAccessor<Boolean> IS_CAT = SynchedEntityData.defineId(TotemSpiritEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_HEALING = SynchedEntityData.defineId(TotemSpiritEntity.class, EntityDataSerializers.BOOLEAN);

    private int roarCooldown = 0;
    private int healingCooldown = 0;

    protected TotemSpiritEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public boolean roarReady() {
        return roarCooldown <= 0;
    }

    public void setRoarCooldown(int cooldown) {
        this.roarCooldown = cooldown;
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

    public void setHealing(boolean healing) {
        this.entityData.set(IS_HEALING, healing);
    }

    public boolean isHealing() {
        return this.entityData.get(IS_HEALING);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (roarCooldown > 0) roarCooldown--;
        }

        if (isHealing()) {
            if (level().isClientSide) {
                //visual stuff
            } else {
                this.heal(0.1f);
            }
        }
    }

    @Override
    public List<? extends ExtendedSensor<? extends SmartSpellEntity<TotemSpiritEntity>>> getSensors() {
        return ObjectArrayList.of(
                new NearbyLivingEntitySensor<>(),
                new HurtBySensor<>()
        );
    }

    @Override
    public BrainActivityGroup<? extends SmartSpellEntity<TotemSpiritEntity>> getCoreTasks() {
        return BrainActivityGroup.coreTasks(
                new LookAtTarget<>(),
                new MoveToWalkTarget<>()
        );
    }

    @Override
    public BrainActivityGroup<? extends SmartSpellEntity<TotemSpiritEntity>> getIdleTasks() {
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
    public BrainActivityGroup<? extends SmartSpellEntity<TotemSpiritEntity>> getFightTasks() {
        return BrainActivityGroup.fightTasks(
                new InvalidateAttackTarget(),
                new FirstApplicableBehaviour<>(
                        //Change form
                        catBehaviours()
                                .startCondition(entity -> ((TotemSpiritEntity) entity).isCatForm()),
                        warriorBehaviours()
                                .startCondition(entity -> !((TotemSpiritEntity) entity).isCatForm())
                )

        );
    }

    protected OneRandomBehaviour<?> catBehaviours() {
        return new OneRandomBehaviour(
                new SequentialBehaviour(
                        new SetWalkTargetToAttackTarget<>(),
                        new AnimatableMeleeAttack<>(20)
                                .startCondition(mob -> mob.distanceTo(mob.getTarget()) < 2f)
                ),
                new FirstApplicableBehaviour<>(
                        new Idle<>()
                                .startCondition(mob -> BrainUtils.getTargetOfEntity(mob) == null || mob.distanceTo(BrainUtils.getTargetOfEntity(mob)) >= 10)
                                .runFor(mob -> 60)
                                .whenStarting(mob -> ((TotemSpiritEntity) mob).setHealing(true))
                                .whenStopping(mob -> ((TotemSpiritEntity) mob).setHealing(false)),
                        new AvoidEntity<>()
                                .avoiding(mob -> BrainUtils.getTargetOfEntity(mob) != null && mob.is(BrainUtils.getTargetOfEntity(mob)))
                                .noCloserThan(10)
                ).startCondition(mob -> ((TotemSpiritEntity) mob).canHeal())
        );
    }

    protected SequentialBehaviour<?> warriorBehaviours() {
        return new SequentialBehaviour<>(
                new SetWalkTargetToAttackTarget<>(),
                new FirstApplicableBehaviour<>(
                        new LeapAtTarget<>(40)
                                .startCondition(mob -> BrainUtils.getTargetOfEntity(mob) != null && mob.distanceTo(BrainUtils.getTargetOfEntity(mob)) >= 10f),
                        new ApplySurroundingEffectBehavior<>(new MobEffectInstance(EffectInit.STUNNED, 100))
                                .areaOf(e -> e.getBoundingBox().inflate(5d))
                                .applyPredicate(this::isAlliedTo)
                                .runFor(mob -> 60)
                                .startCondition(mob -> ((TotemSpiritEntity) mob).roarReady())
                                .whenStarting(mob -> ((TotemSpiritEntity) mob).setRoarCooldown(600)),
                        new AnimatableMeleeAttack<>(20)
                                .startCondition(mob -> mob.distanceTo(mob.getTarget()) < 2f)
                )
        );
    }

}
