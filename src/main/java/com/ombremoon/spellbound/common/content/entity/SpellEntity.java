package com.ombremoon.spellbound.common.content.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class SpellEntity extends Entity implements GeoEntity, TraceableEntity {
    protected static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.INT);
    protected static final String CONTROLLER = "controller";
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SpellEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ID_FLAGS, (byte)0);
        builder.define(OWNER_ID, 0);
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
        if (!this.hasOwner())
            discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    protected void setFlag(int id, boolean value) {
        byte b0 = this.entityData.get(ID_FLAGS);
        if (value) {
            this.entityData.set(ID_FLAGS, (byte)(b0 | id));
        } else {
            this.entityData.set(ID_FLAGS, (byte)(b0 & ~id));
        }
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
