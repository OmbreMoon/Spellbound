package com.ombremoon.spellbound.common.content.entity;

import com.ombremoon.spellbound.util.Loggable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class SpellEntity extends Entity implements GeoEntity, TraceableEntity, Loggable {
    protected static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> END_TICK = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> START_TICK = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.INT);
    protected static final String CONTROLLER = "controller";
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SpellEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return (entity instanceof LivingEntity livingEntity && isOwner(livingEntity)) || super.isAlliedTo(entity);
    }

    protected boolean isOwner(LivingEntity entity) {
        return getOwner() != null && getOwner().is(entity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ID_FLAGS, (byte)0);
        builder.define(OWNER_ID, 0);
        builder.define(START_TICK, 0);
        builder.define(END_TICK, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {

    }

    @Override
    public void tick() {
        super.tick();
        if (!this.hasOwner() || (this.isEnding() && this.tickCount >= this.getEndTick()))
            discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    protected <T extends GeoAnimatable> PlayState genericController(AnimationState<T> data) {
        if (isStarting()) {
            data.setAnimation(RawAnimation.begin().thenPlay("spawn"));
        } else if (isEnding()) {
            data.setAnimation(RawAnimation.begin().thenPlay("end"));
        } else {
            data.setAnimation(RawAnimation.begin().thenLoop("idle"));
        }
        return PlayState.CONTINUE;
    }

    protected void setFlag(int id, boolean value) {
        byte b0 = this.entityData.get(ID_FLAGS);
        if (value) {
            this.entityData.set(ID_FLAGS, (byte)(b0 | id));
        } else {
            this.entityData.set(ID_FLAGS, (byte)(b0 & ~id));
        }
    }

    public boolean isStarting() {
        return this.tickCount <= getStartTick();
    }

    public int getStartTick() {
        return this.entityData.get(START_TICK);
    }

    public void setStartTick(int startTick) {
        this.entityData.set(START_TICK, startTick);
    }

    public boolean isEnding() {
        return getEndTick() > 0;
    }

    public int getEndTick() {
        return this.entityData.get(END_TICK);
    }

    public void setEndTick(int endTick) {
        this.entityData.set(END_TICK, this.tickCount + endTick);
    }

    public void setOwner(LivingEntity livingEntity) {
        this.entityData.set(OWNER_ID, livingEntity.getId());
    }

    @Override
    public @Nullable Entity getOwner() {
        return this.level().getEntity(this.entityData.get(OWNER_ID));
    }

    public boolean hasOwner() {
        return getOwner() != null;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
