package com.ombremoon.spellbound.common.content.entity;

import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

@SuppressWarnings("unchecked")
public abstract class SpellProjectile<T extends AbstractSpell> extends Projectile implements ISpellEntity<T> {
    private static final EntityDataAccessor<String> SPELL_TYPE = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SPELL_ID = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.INT);
    protected static final String CONTROLLER = "controller";
    protected T spell;
    protected SpellHandler handler;
    protected SkillHolder skills;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected SpellProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.06;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getOwner() instanceof LivingEntity livingEntity && this.tickCount < 5 && (this.handler == null || this.skills == null)) {
            this.handler = SpellUtil.getSpellCaster(livingEntity);
            this.skills = SpellUtil.getSkills(livingEntity);
        }

        if (!this.level().isClientSide) {
            if (this.isInitialized() && this.spell != null && this.spell.isInactive)
                discard();

            if (this.spell != null)
                this.spell.onEntityTick(this, this.spell.getContext());
        }

        Vec3 vec3 = this.getDeltaMovement();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this, hitresult))
            this.hitTargetOrDeflectSelf(hitresult);
        double d0 = this.getX() + vec3.x;
        double d1 = this.getY() + vec3.y;
        double d2 = this.getZ() + vec3.z;
        this.updateRotation();
        float f = 0.99F;
        if (this.level().getBlockStates(this.getBoundingBox()).noneMatch(state -> state.isAir() || state.is(Blocks.WATER))) {
            this.discard();
        } else if (this.isInWaterOrBubble()) {
            f = 0.89F;
        }
        this.setDeltaMovement(vec3.scale(f));
        this.applyGravity();
        this.setPos(d0, d1, d2);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            if (this.spell != null)
                this.spell.onProjectileHitEntity(this, spell.getContext(), result);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            if (this.spell != null)
                this.spell.onProjectileHitBlock(this, spell.getContext(), result);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SPELL_TYPE, "");
        builder.define(SPELL_ID, -1);
        builder.define(OWNER_ID, 0);
    }

    @Override
    public boolean isInitialized() {
        return this.handler != null;
    }

    public T getSpell() {
        if (this.spell == null) {
            SpellType<T> spellType = this.getSpellType();
            if (this.handler != null && spellType != null)
                this.spell = this.handler.getSpell(spellType, this.getSpellId());
        }

        return this.spell;
    }

    public void setSpell(@NotNull AbstractSpell spell) {
        this.spell = (T) spell;
        this.setSpellType(spell.spellType());
        this.setSpellId(spell.getId());
    }

    public SpellType<T> getSpellType(){
        return (SpellType<T>) SBSpells.REGISTRY.get(ResourceLocation.tryParse(this.entityData.get(SPELL_TYPE)));
    }

    public void setSpellType(SpellType<?> spellType) {
        this.entityData.set(SPELL_TYPE, spellType.location().toString());
    }

    public int getSpellId(){
        return this.entityData.get(SPELL_ID);
    }

    public void setSpellId(int id) {
        this.entityData.set(SPELL_ID, id);
    }

    protected boolean isOwner(LivingEntity entity) {
        return getOwner() != null && getOwner().is(entity);
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
    public EntityType<?> entityType() {
        return this.getType();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 0, this::genericController));
    }

    protected <S extends GeoAnimatable> PlayState genericController(AnimationState<S> data) {
        data.setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
