/*
package com.ombremoon.spellbound.common.magic.api;

import com.ombremoon.enderring.common.WeaponScaling;
import com.ombremoon.enderring.common.data.ScaledWeapon;
import com.ombremoon.enderring.common.magic.Classification;
import com.ombremoon.enderring.common.magic.SpellType;
import com.ombremoon.enderring.common.object.entity.projectile.spell.SpellProjectileEntity;
import com.ombremoon.enderring.event.custom.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public abstract class ProjectileSpell<S extends ProjectileSpell<S, T>, T extends SpellProjectileEntity<S>> extends AnimatedSpell {
    private final ProjectileFactory<S, T> factory;
    private final int projectileLifetime;
    private final float projectileVelocity;
    private final boolean shootFromCatalyst;
    private final boolean canClip;
    private final float speedModifier;
    private final float gravity;
    private final int inactiveTicks;
    protected T projectile;
    protected int projectileID = 0;

    public static Builder createProjectileBuilder() {
        return new Builder<>();
    }

    @SuppressWarnings("unchecked")
    public ProjectileSpell(SpellType<?> spellType, ProjectileFactory<S, T> factory, Builder<S> builder) {
        super(spellType, builder);
        this.factory = factory;
        builder = (Builder<S>) EventFactory.getProjectileBuilder(spellType, builder);
        this.projectileLifetime = builder.projectileLifetime;
        this.projectileVelocity = builder.projectileVelocity;
        this.shootFromCatalyst = builder.shootFromCatalyst;
        this.canClip = builder.canClip;
        this.speedModifier = builder.speedModifier;
        this.gravity = builder.gravity;
        this.inactiveTicks = builder.inactiveTicks;
    }

    @Override
    protected void onSpellTick(LivingEntityPatch<?> playerPatch, Level level, BlockPos blockPos, ScaledWeapon weapon) {
        super.onSpellTick(playerPatch, level, blockPos, weapon);
        createProjectile(playerPatch, this.projectileID);
    }

    protected void createProjectile(LivingEntityPatch<?> livingEntityPatch, int id) {
        LivingEntity caster = livingEntityPatch.getOriginal();
        T spellProjectile = this.factory.create(this.level, this.scaledWeapon, (S) this);
        spellProjectile.setOwner(livingEntityPatch.getOriginal());
        spellProjectile.setVelocity(this.projectileVelocity);
        spellProjectile.setSpeedModifier(this.speedModifier);
        spellProjectile.setGravity(this.gravity);
        spellProjectile.setCharge(this.getChargeAmount());
        spellProjectile.setInactiveTicks(this.inactiveTicks);

        Vec3 vec31 = this.shootFromCatalyst ? new Vec3(caster.getX(), caster.getEyeY() - (double)0.3F, caster.getZ()) : spellProjectile.initPosition(caster).get(id);
        Vec3 vec32 = this.shootFromCatalyst ? new Vec3(caster.getXRot(), caster.getYRot(), 0.0F) : spellProjectile.initRotation(caster).get(id);
        spellProjectile.setPos(vec31.x, vec31.y, vec31.z);
        if (this.inactiveTicks == 0) {
            spellProjectile.shootFromRotation((float) vec32.x, (float) vec32.y, 0.0F, this.projectileVelocity * this.getChargeAmount(), 1.0F);
            spellProjectile.setActive(true);
        } else {
            spellProjectile.prepareShootFromRotation((float) vec32.x, (float) vec32.y, 0.0F, this.projectileVelocity * this.getChargeAmount(), 1.0F);
        }
        spellProjectile.setXRot((float) vec32.x);
        spellProjectile.setYRot((float) vec32.y);

        if (spellProjectile.getPath() == SpellProjectileEntity.Path.HOMING) {
            spellProjectile.setTargetEntity(livingEntityPatch.getTarget());
        }
        level.addFreshEntity(spellProjectile);
    }

    @Override
    protected boolean shouldTickEffect(int duration) {
        return duration == 2;
    }

    public int getProjectileLifetime() {
        return this.projectileLifetime;
    }

    public float getVelocity() {
        return this.projectileVelocity;
    }

    public boolean canClip() {
        return this.canClip;
    }

    public float getSpeedModifier() {
        return this.speedModifier;
    }

    public float getGravity() {
        return this.gravity;
    }

    public int getInactiveTicks() {
        return this.inactiveTicks;
    }

    public static class Builder<T extends ProjectileSpell<T, ?>> extends AnimatedSpell.Builder<T> {
        protected int projectileLifetime;
        protected float projectileVelocity;
        protected boolean shootFromCatalyst = false;
        protected boolean canClip = false;
        protected float speedModifier = 1.0F;
        protected float gravity;
        protected int inactiveTicks;

        public Builder() {
        }

        public Builder<T> setClassification(Classification classification) {
            this.classification = classification;
            return this;
        }

        public Builder<T> setRequirements(WeaponScaling weaponScaling, int statReq) {
            this.createReqList(weaponScaling, statReq);
            return this;
        }

        public Builder<T> setFPCost(int fpCost) {
            this.fpCost = fpCost;
            return this;
        }

        public Builder<T> setStaminaCost(int staminaCost) {
            this.staminaCost = staminaCost;
            return this;
        }

        public Builder<T> setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder<T> setMotionValue(float motionValue) {
            this.motionValue = motionValue;
            return this;
        }

        public Builder<T> setChargedMotionValue(float motionValue) {
            this.chargedMotionValue = motionValue;
            return this;
        }

        public Builder<T> setCastType(CastType castType) {
            this.castType = castType;
            return this;
        }

        public Builder<T> setCastSound(SoundEvent castSound) {
            this.castSound = castSound;
            return this;
        }

        public Builder<T> setLifetime(int lifetime) {
            this.projectileLifetime = lifetime;
            return this;
        }

        public Builder<T> setVelocity(float velocity) {
            this.projectileVelocity = velocity;
            return this;
        }

        public Builder<T> shootFromCatalyst() {
            this.shootFromCatalyst = true;
            return this;
        }

        public Builder<T> canClip() {
            this.canClip = true;
            return this;
        }

        public Builder<T> setSpeedModifier(float modifier) {
            this.speedModifier = modifier;
            return this;
        }

        public Builder<T> setGravity(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder<T> setInactiveTicks(int inactiveTicks) {
            this.inactiveTicks = inactiveTicks;
            return this;
        }
    }

    @FunctionalInterface
    protected interface ProjectileFactory<U extends ProjectileSpell<U, V>, V extends SpellProjectileEntity<U>> {
        V create(Level level, ScaledWeapon weapon, U spell);
    }
}
*/
