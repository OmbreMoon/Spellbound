package com.ombremoon.spellbound.common.content.entity;

import com.ombremoon.spellbound.common.init.SBData;
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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.navigation.SmoothGroundNavigation;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Predicate;

public abstract class SmartSpellEntity<T extends AbstractSpell> extends SBLivingEntity implements ISpellEntity<T> {
    private static final EntityDataAccessor<String> SPELL_TYPE = SynchedEntityData.defineId(SmartSpellEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SPELL_ID = SynchedEntityData.defineId(SmartSpellEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(SmartSpellEntity.class, EntityDataSerializers.INT);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected T spell;
    protected SpellHandler handler;
    protected SkillHolder skills;

    protected SmartSpellEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
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

    @Override
    public void tick() {
        super.tick();
        if (this.getOwner() instanceof LivingEntity livingEntity && this.tickCount < 5 && (this.handler == null || this.skills == null)) {
            this.handler = SpellUtil.getSpellCaster(livingEntity);
            this.skills = SpellUtil.getSkills(livingEntity);
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

    protected boolean isOwnersTarget(LivingEntity entity) {
        if (!this.hasData(SBData.TARGET_ID)) return false;
        return entity.getId() == this.getData(SBData.TARGET_ID);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPELL_TYPE, "");
        builder.define(SPELL_ID, -1);
        builder.define(OWNER_ID, 0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new SmoothGroundNavigation(this, this.level());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

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
