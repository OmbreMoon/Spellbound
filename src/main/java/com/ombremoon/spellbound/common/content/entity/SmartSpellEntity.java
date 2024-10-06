package com.ombremoon.spellbound.common.content.entity;

import com.ombremoon.spellbound.common.init.DataInit;
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
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private LivingEntity owner;

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
        if (this.getBrain() == null) return livingEntity -> false;
        Entity target = BrainUtils.getMemory(this, MemoryModuleType.HURT_BY_ENTITY);
        return livingEntity -> !isOwner(livingEntity) && ((target != null &&  target.getUUID() == livingEntity.getUUID()) || isOwnersTarget(livingEntity));
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return (entity instanceof LivingEntity livingEntity && isOwner(livingEntity)) || super.isAlliedTo(entity);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return getOwner() == null ? null : this.owner.getUUID();
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        if (this.owner == null && this.hasData(DataInit.OWNER_UUID)) {
            this.owner = level().getPlayerByUUID(UUID.fromString(this.getData(DataInit.OWNER_UUID)));
        }
        return this.owner;
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
