package com.ombremoon.spellbound.common.content.entity;

import com.ombremoon.spellbound.common.init.SBSpells;
import com.ombremoon.spellbound.common.magic.SpellHandler;
import com.ombremoon.spellbound.common.magic.api.SpellType;
import com.ombremoon.spellbound.common.magic.api.AbstractSpell;
import com.ombremoon.spellbound.common.magic.skills.SkillHolder;
import com.ombremoon.spellbound.util.Loggable;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class SpellEntity<T extends AbstractSpell> extends Entity implements ISpellEntity<T>, Loggable {
    private static final EntityDataAccessor<String> SPELL_TYPE = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SPELL_ID = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> END_TICK = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> START_TICK = SynchedEntityData.defineId(SpellEntity.class, EntityDataSerializers.INT);
    protected static final String CONTROLLER = "controller";
    protected T spell;
    protected SpellHandler handler;
    protected SkillHolder skills;
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
        builder.define(SPELL_TYPE, "");
        builder.define(SPELL_ID, -1);
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
        if (this.getOwner() instanceof LivingEntity livingEntity && this.tickCount < 5 && (this.handler == null || this.skills == null)) {
            this.handler = SpellUtil.getSpellHandler(livingEntity);
            this.skills = SpellUtil.getSkillHolder(livingEntity);
        }

        if (!this.hasOwner() || (this.isEnding() && this.tickCount >= this.getEndTick()))
            discard();

        T spell = this.getSpell();
        if (spell != null)
            spell.onEntityTick(this, spell.getContext());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    protected <S extends GeoAnimatable> PlayState genericController(AnimationState<S> data) {
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

    public T getSpell() {
        if (this.spell == null) {
            SpellType<T> spellType = this.getSpellType();
            if (this.handler != null && spellType != null)
                this.spell = this.handler.getSpell(spellType, this.getSpellId());
        }

        return this.spell;
    }

    public void setSpell(SpellType<?> spellType, int spellId) {
        this.setSpellType(spellType);
        this.setSpellId(spellId);
    }

    @SuppressWarnings("unchecked")
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
    public EntityType<?> entityType() {
        return this.getType();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
