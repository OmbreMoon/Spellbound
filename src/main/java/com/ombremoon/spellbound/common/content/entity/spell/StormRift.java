package com.ombremoon.spellbound.common.content.entity.spell;

import com.ombremoon.sentinellib.api.Easing;
import com.ombremoon.spellbound.common.content.entity.PortalEntity;
import com.ombremoon.spellbound.common.init.SBData;
import com.ombremoon.spellbound.common.init.SBEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.*;

import java.util.List;

public class StormRift extends PortalEntity {
    private static final EntityDataAccessor<Boolean> GROW = SynchedEntityData.defineId(StormRift.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IMPLODE = SynchedEntityData.defineId(StormRift.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ROTATE = SynchedEntityData.defineId(StormRift.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<BlockPos> CENTER = SynchedEntityData.defineId(StormRift.class, EntityDataSerializers.BLOCK_POS);
    private final double delta = 0.1F;
    private float radius;
    private float angle;
    private int cloudId;
    private int explosionTimer = 13;
    public int rotationTick;
    public float rotationAngle = 0.0F;
    public float rotationSpeed = 0.02F;
    public static final String EXPLODE = "explode";

    public StormRift(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GROW, false);
        builder.define(IMPLODE, false);
        builder.define(ROTATE, false);
        builder.define(CENTER, BlockPos.ZERO);
    }

    @Override
    public int getPortalCooldown() {
        return 60;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount <= 100)
            this.refreshDimensions();

        if (this.tickCount > 100 && this.canRotate()) {
            this.rotationTick++;

            if (this.radius < 5)
                this.radius += (float) this.delta;

            BlockPos center = this.getCenter();
            double x = center.getX();
            double z = center.getZ();
            double newX = x + this.radius * Math.cos(this.angle);
            double newZ = z + this.radius * Math.sin(this.angle);

            this.angle += 0.1F;
            this.setPos(newX, this.getY(), newZ);
        }

        if (this.entityData.get(IMPLODE)) {
            if (this.level().getEntity(this.getCloudId()) instanceof StormCloud cloud)
                cloud.discard();

            if (this.explosionTimer > 0) {
                this.explosionTimer--;
                if (this.explosionTimer == 1)
                    this.level().explode(
                        this,
                        Explosion.getDefaultDamageSource(this.level(), this),
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        6.0F,
                        false,
                        Level.ExplosionInteraction.BLOCK,
                        ParticleTypes.EXPLOSION,
                        ParticleTypes.EXPLOSION_EMITTER,
                        SoundEvents.GENERIC_EXPLODE
                );
                if (this.handler != null) {
                    LivingEntity caster = this.handler.caster;
                    List<LivingEntity> list = this.level().getEntitiesOfClass(LivingEntity.class,  new AABB(this.getX() - 6.0, this.getY() - 6.0, this.getZ() - 6.0, this.getX() + 6.0, this.getY() + 6.0, this.getZ() + 6.0));
                    for (LivingEntity livingEntity : list) {
                        livingEntity.setData(SBData.STORMSTRIKE_OWNER, caster.getId());
                        livingEntity.addEffect(new MobEffectInstance(SBEffects.STORMSTRIKE, 100, 0, false, false));
                    }
                }
            } else {
                this.discard();
            }
        }
    }

    public void implode() {
        triggerAnim(CONTROLLER, EXPLODE);
        this.entityData.set(IMPLODE, true);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        int maxGrowth = this.canGrow() ? 2 : 1;
        float easing = Math.min(maxGrowth, Easing.QUAD_IN.easing(2, (float) this.tickCount / 100));
        return this.canGrow() ? super.getDimensions(pose).scale(easing) : super.getDimensions(pose);
    }

    public BlockPos getCenter() {
        return this.entityData.get(CENTER);
    }

    public void setCenter(BlockPos center) {
        this.entityData.set(CENTER, center);
    }

    public int getCloudId() {
        return this.cloudId;
    }

    public void setCloudId(int cloudId) {
        this.cloudId = cloudId;
    }

    public void allowRotation() {
        this.entityData.set(ROTATE, true);
    }

    public boolean canRotate() {
        return this.entityData.get(ROTATE);
    }

    public void allowGrowth() {
        this.entityData.set(GROW, true);
    }

    public boolean canGrow() {
        return this.entityData.get(GROW);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, this::stormRiftController)
                .triggerableAnim(EXPLODE, RawAnimation.begin().thenPlay("explosion")));
    }

    protected <T extends GeoAnimatable> PlayState stormRiftController(AnimationState<T> data) {
        if (isStarting()) {
//            data.setAnimation(RawAnimation.begin().thenPlay("spawn"));
        } else if (isEnding()) {
            data.setAnimation(RawAnimation.begin().thenPlay("explosion"));
        } else {
            data.setAnimation(RawAnimation.begin().thenLoop("idle"));
        }
        return PlayState.CONTINUE;
    }
}
