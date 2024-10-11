package com.ombremoon.spellbound.common.content.entity;

import com.ombremoon.spellbound.common.init.DataInit;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;
import java.util.function.Predicate;

public abstract class SmartSpellEntity extends PathfinderMob implements GeoEntity, SmartBrainOwner<SmartSpellEntity>, OwnableEntity {
    private final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(SmartSpellEntity.class, EntityDataSerializers.INT);
    
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected SmartSpellEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        tickBrain(this);
    }

    protected Predicate<LivingEntity> summonAttackPredicate() {
        if (this.getBrain() == null) return livingEntity -> false; //This is needed, ignore intellij
        Entity target = BrainUtils.getMemory(this, MemoryModuleType.HURT_BY_ENTITY);
        return livingEntity -> !isOwner(livingEntity) && ((target != null &&  target.is(livingEntity)) || isOwnersTarget(livingEntity));
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return (entity instanceof LivingEntity livingEntity && isOwner(livingEntity)) || super.isAlliedTo(entity);
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        return (LivingEntity) this.level().getEntity(this.entityData.get(OWNER_ID));
    }

    public void setOwner(LivingEntity entity) {
        this.entityData.set(OWNER_ID, entity.getId());
    }

    protected boolean isOwner(LivingEntity entity) {
        return getOwner() != null && getOwner().is(entity);
    }

    protected boolean isOwnersTarget(LivingEntity entity) {
        if (!this.hasData(DataInit.TARGET_ID)) return false;
        return entity.getId() == this.getData(DataInit.TARGET_ID);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
