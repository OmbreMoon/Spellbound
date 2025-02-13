package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.util.Loggable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class SBLivingEntity extends PathfinderMob implements SmartBrainOwner<SBLivingEntity>, GeoEntity, Loggable {
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(SBLivingEntity.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected SpellHandler handler;
    protected SkillHolder skills;

    protected SBLivingEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
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

    @Override
    public boolean isAlliedTo(Entity entity) {
        return (entity instanceof LivingEntity livingEntity && isOwner(livingEntity)) || super.isAlliedTo(entity);
    }

    @Nullable
    public LivingEntity getOwner() {
        return (LivingEntity) this.level().getEntity(this.entityData.get(OWNER_ID));
    }

    public void setOwner(LivingEntity entity) {
        this.entityData.set(OWNER_ID, entity.getId());
    }

    protected boolean isOwner(LivingEntity entity) {
        return getOwner() != null && getOwner().is(entity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_ID, 0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SmoothGroundNavigation(this, this.level());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    public EntityType<?> entityType() {
        return this.getType();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
