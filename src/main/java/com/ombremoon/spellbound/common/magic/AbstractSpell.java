package com.ombremoon.spellbound.common.magic;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.init.StatInit;
import com.ombremoon.spellbound.common.magic.api.ModifierType;
import com.ombremoon.spellbound.common.magic.events.SpellEvent;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class AbstractSpell {
    protected static final Logger LOGGER = Constants.LOG;
    private final SpellType<?> spellType;
    private final int manaCost;
    private final int duration;
    private final int castTime;
    private final BiPredicate<SpellContext, AbstractSpell> castPredicate;
    private final CastType castType;
    private final SoundEvent castSound;
    private final boolean fullRecast;
    private final boolean partialRecast;
    private final boolean shouldPersist;
    private Level level;
    private Player caster;
    private BlockPos blockPos;
    private String nameId;
    private String descriptionId;
    private SpellContext context;
    private boolean isRecast;
    public int ticks = 0;
    public boolean isInactive = false;
    public boolean init = false;
    private int castId = 0;

    public static Builder<AbstractSpell> createBuilder() {
        return new Builder<>();
    }

    public AbstractSpell(SpellType<?> spellType, Builder<? extends AbstractSpell> builder) {
        this.spellType = spellType;
        this.manaCost = builder.manaCost;
        this.duration = builder.duration;
        this.castTime = builder.castTime;
        this.castPredicate = (BiPredicate<SpellContext, AbstractSpell>) builder.castPredicate;
        this.castType = builder.castType;
        this.castSound = builder.castSound;
        this.fullRecast = builder.fullRecast;
        this.partialRecast = builder.partialRecast;
        this.shouldPersist = builder.shouldPersist;
    }

    public SpellType<?> getSpellType() {
        return this.spellType;
    }

    public float getManaCost() {
        return getManaCost(this.caster);
    }

    public float getManaCost(Player player) {
        return this.manaCost * getModifier(ModifierType.MANA, player);
    }

    public int getDuration() {
        return (int) Math.floor(this.duration * getModifier(ModifierType.DURATION));
    }

    protected SoundEvent getCastSound() {
        return this.castSound;
    }

    public CastType getCastType() {
        return this.castType;
    }

    public int getCastTime() {
        return this.castTime;
    }

    public ResourceLocation getId() {
        return SpellInit.REGISTRY.getKey(this.spellType);
    }

    protected String getOrCreateNameId() {
        if (this.nameId == null) {
            this.nameId = Util.makeDescriptionId("spell", this.getId());
        }
        return this.nameId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("spell.description", this.getId());
        }
        return this.descriptionId;
    }

    public String getNameId() {
        return this.getOrCreateNameId();
    }

    public ResourceLocation getTexture() {
        ResourceLocation name = this.getId();
        return CommonClass.customLocation("textures/gui/spells/" + name.getPath() + ".png");
    }

    public MutableComponent getName() {
        return Component.translatable(this.getNameId());
    }

    public MutableComponent getDescription() {
        return Component.translatable(this.getDescriptionId());
    }

    public static SpellType<?> getSpellByName(ResourceLocation resourceLocation) {
        return SpellInit.REGISTRY.get(resourceLocation);
    }

    public SpellPath getPath() {
        return this.getSpellType().getPath();
    }

    public @Nullable SpellPath getSubPath() {
        return this.getSpellType().getSubPath();
    }

    public @UnknownNullability CompoundTag saveData(CompoundTag compoundTag) {
        return compoundTag;
    }

    public void load(CompoundTag nbt) {

    }

    public void tick() {
        if (!level.isClientSide) {
            ticks++;
            if (init) {
                this.startSpell();
            } else if (!isInactive) {
                if (this.shouldTickEffect(this.context)) {
                    this.tickSpell();
                }
                if (this.getCastType() != CastType.CHANNEL && ticks % getDuration() == 0) {
                    this.endSpell();
                }
            }
        }
    }

    private void startSpell() {
        this.init = false;
        this.onSpellStart(this.context);
        if (this.isRecast)
            this.onSpellRecast(this.context);
    }

    private void tickSpell() {
        this.onSpellTick(this.context);
    }

    public void endSpell() {
        this.onSpellStop(this.context);
        this.init = false;
        this.isInactive = true;
        this.ticks = 0;
    }

    protected void onSpellTick(SpellContext context) {
    }

    protected void onSpellStart(SpellContext context) {
    }

    protected void onSpellRecast(SpellContext context) {
    }

    protected void onSpellStop(SpellContext context) {
    }

    public void whenCasting(SpellContext context, int castTime) {
        Constants.LOG.info("{}", castTime);
    }

    protected boolean shouldTickEffect(SpellContext context) {
        return true;
    }

    public void addTimedModifier(LivingEntity livingEntity, SpellModifier spellModifier, int expiryTick) {
        var handler = SpellUtil.getSkillHandler(livingEntity);
        handler.addModifierWithExpiry(spellModifier, expiryTick);
    }

    public void addTimedListener(LivingEntity livingEntity, SpellEventListener.IEvent event, UUID uuid, Consumer<? extends SpellEvent> consumer, int expiryTicks) {
        var listener = SpellUtil.getSpellHandler(livingEntity).getListener();
        if (!listener.hasListener(event, uuid))
            listener.addListenerWithExpiry(event, uuid, consumer, expiryTicks);
    }

    protected float potency() {
        return getModifier(ModifierType.POTENCY);
    }

    private float getModifier(ModifierType modifierType) {
        return getModifier(modifierType, this.caster);
    }

    private float getModifier(ModifierType modifierType, Player player) {
        var handler = SpellUtil.getSkillHandler(player);
        float f = 1;
        for (var modifier : handler.getModifiers()) {
            if (modifierType.equals(modifier.modifierType()) && modifier.spellPredicate().test(getSpellType())) {
                f *= modifier.modifier();
            }
        }
        return f;
    }

    /**
     * Checks if the caster of the spell has an attribute modifier
     * @param attribute The attribute to check if modified
     * @param modifier ResourceLocation of the AttributeModifier
     * @return true if the modifier is present, false otherwise
     */
    public boolean hasAttributeModifier(LivingEntity livingEntity, Holder<Attribute> attribute, ResourceLocation modifier) {
        return livingEntity.getAttribute(attribute).hasModifier(modifier);
    }

    public void addTimedAttributeModifier(LivingEntity livingEntity, Holder<Attribute> attribute, AttributeModifier modifier, int ticks) {
        addAttributeModifier(livingEntity, attribute, modifier);
        SpellUtil.getSpellHandler(livingEntity).addTransientModifier(attribute, modifier, ticks);
    }

    /**
     * Adds a given attribute modifier to a chosen attribute on the caster
     * @param attribute The attribute to apply a modifier to
     * @param modifier the AttributeModifier to apply
     */
    public void addAttributeModifier(LivingEntity livingEntity, Holder<Attribute> attribute, AttributeModifier modifier) {
        if (!hasAttributeModifier(livingEntity, attribute, modifier.id()))
            livingEntity.getAttribute(attribute).addTransientModifier(modifier);
    }

    /**
     * Removes an attribute modifier from the caster
     * @param attribute The attribute the modifier affects
     * @param modifier The ResourceLocation of the modifier to remove
     */
    public void removeAttributeModifier(LivingEntity livingEntity, Holder<Attribute> attribute, ResourceLocation modifier) {
        livingEntity.getAttribute(attribute).removeModifier(modifier);
    }

    protected boolean isCaster(LivingEntity livingEntity) {
        return livingEntity.is(this.caster);
    }

    public static float getPowerForTime(AbstractSpell spell, int pCharge) {
        float f = (float) pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    protected void addCooldown(Skill skill, int ticks) {
        this.context.getSkillHandler().getCooldowns().addCooldown(skill, ticks);
    }

    protected void addScreenShake(Player player) {
        addScreenShake(player, 10);
    }

    protected void addScreenShake(Player player, int duration) {
        addScreenShake(player, duration, 1);
    }

    protected void addScreenShake(Player player, int duration, float intensity) {
        addScreenShake(player, duration, intensity, 0.25F);
    }

    protected void addScreenShake(Player player, int duration, float intensity, float maxOffset) {
        addScreenShake(player, duration, intensity, maxOffset, 10);
    }

    protected void addScreenShake(Player player, int duration, float intensity, float maxOffset, int freq) {
        PayloadHandler.shakeScreen(player, duration, intensity, maxOffset, freq);
    }

    public BlockHitResult getTargetBlock(double range) {
        return this.level.clip(setupRayTraceContext(this.caster, range, ClipContext.Fluid.NONE));
    }

/*    protected List<LivingEntity> getEntitiesInLine(double range) {
        List<LivingEntity> entityList = new ObjectArrayList<>();
        double dist = range * range;
        Vec3 startPos = this.caster.getEyePosition(1.0F);
        LivingEntity startEntity = this.caster;
        while (true) {
            LivingEntity livingEntity = getTargetEntity(startEntity, startPos, range);
            if (livingEntity != null && livingEntity.distanceToSqr(this.caster) < dist && !entityList.contains(livingEntity)) {
                entityList.add(livingEntity);
                startPos = livingEntity.getBoundingBox().getCenter();
                startEntity = livingEntity;
            } else {
                break;
            }
        }
        return entityList;
    }*/

    protected @Nullable LivingEntity getTargetEntity(double range) {
        return getTargetEntity(this.caster, range);
    }

    public @Nullable LivingEntity getTargetEntity(LivingEntity livingEntity, double range) {
        return getTargetEntity(livingEntity, livingEntity.getEyePosition(1.0F), range);
    }

    private @Nullable LivingEntity getTargetEntity(LivingEntity livingEntity, Vec3 startPosition, double range) {
        Vec3 lookVec = livingEntity.getViewVector(1.0F);
        Vec3 maxLength = startPosition.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);
        AABB aabb = livingEntity.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.0);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(livingEntity, startPosition, maxLength, aabb, EntitySelector.NO_CREATIVE_OR_SPECTATOR, range * range);
        BlockHitResult blockHitResult = livingEntity.level().clip(setupRayTraceContext(livingEntity, range, ClipContext.Fluid.NONE));

        if (hitResult == null)
            return null;


        if (hitResult.getEntity() instanceof LivingEntity targetEntity) {

            if (!blockHitResult.getType().equals(BlockHitResult.Type.MISS)) {
                double blockDistance = blockHitResult.getLocation().distanceTo(startPosition);
                if (blockDistance > targetEntity.distanceTo(livingEntity)) {
                    return targetEntity;
                }
            } else {
                return targetEntity;
            }
        }
        return null;
    }

    protected static ClipContext setupRayTraceContext(LivingEntity livingEntity, double distance, ClipContext.Fluid fluidContext) {
        float pitch = livingEntity.getXRot();
        float yaw = livingEntity.getYRot();
        Vec3 fromPos = livingEntity.getEyePosition(1.0F);
        float float_3 = Mth.cos(-yaw * 0.017453292F - 3.1415927F);
        float float_4 = Mth.sin(-yaw * 0.017453292F - 3.1415927F);
        float float_5 = -Mth.cos(-pitch * 0.017453292F);
        float xComponent = float_4 * float_5;
        float yComponent = Mth.sin(-pitch * 0.017453292F);
        float zComponent = float_3 * float_5;
        Vec3 toPos = fromPos.add((double) xComponent * distance, (double) yComponent * distance,
                (double) zComponent * distance);
        return new ClipContext(fromPos, toPos, ClipContext.Block.OUTLINE, fluidContext, livingEntity);
    }

    public boolean shouldPersist() {
        return this.shouldPersist;
    }

    public void initSpell(Player player, Level level, BlockPos blockPos) {
        this.level = level;
        this.caster = player;
        this.blockPos = blockPos;

        var handler = SpellUtil.getSpellHandler(player);
        var list = handler.getActiveSpells(getSpellType());
        if (!list.isEmpty()) this.isRecast = true;
        this.context = new SpellContext(this.caster, this.level, this.blockPos, this.getTargetEntity(8), this.isRecast);

        boolean incrementId = true;
        if (this.isRecast) {
            AbstractSpell prevSpell = null;
            if (this.partialRecast) {
                prevSpell = this.getPreviouslyCastSpell();
            } else if (this.fullRecast) {
                prevSpell = handler.getActiveSpells(getSpellType()).stream().findFirst().orElse(null);
            }

            if (prevSpell != null) {
                this.castId = prevSpell.castId + 1;
                incrementId = false;
                CompoundTag nbt = prevSpell.saveData(new CompoundTag());
                this.load(nbt);
            }
        }

        //Play Fail Animation
        if (!this.castPredicate.test(this.context, this)) return;

        activateSpell();
        player.awardStat(StatInit.SPELLS_CAST.get());
        if (incrementId) this.castId++;

        this.init = true;
    }

    private AbstractSpell getPreviouslyCastSpell() {
        var handler = this.context.getSpellHandler();
        var spells = handler.getActiveSpells(getSpellType());
        AbstractSpell spell = this;
        for (AbstractSpell abstractSpell : spells) {
            if (abstractSpell.castId > spell.castId)
                spell = abstractSpell;
        }
        return spell;
    }

    private void activateSpell() {
        var handler = this.context.getSpellHandler();
        if (this.fullRecast) {
            handler.recastSpell(this);
        } else {
            handler.activateSpell(this);
        }
        handler.consumeMana(getManaCost(), true);
        PayloadHandler.syncMana(this.caster);
    }

    public static class Builder<T extends AbstractSpell> {
        protected int duration = 10;
        protected int manaCost;
        protected int castTime = 1;
        protected BiPredicate<SpellContext, T> castPredicate = (context, abstractSpell) -> true;
        protected CastType castType = CastType.CHARGING;
        protected SoundEvent castSound;
        protected boolean partialRecast;
        protected boolean fullRecast;
        protected boolean shouldPersist;

        public Builder<T> manaCost(int fpCost) {
            this.manaCost = fpCost;
            return this;
        }

        public Builder<T> duration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder<T> castTime(int castTime) {
            this.castTime = castTime;
            return this;
        }

        public Builder<T> castType(CastType castType) {
            this.castType = castType;
            return this;
        }

        public Builder<T> castSound(SoundEvent castSound) {
            this.castSound = castSound;
            return this;
        }

        public Builder<T> castCondition(BiPredicate<SpellContext, T> castCondition) {
            this.castPredicate = castCondition;
            return this;
        }

        public Builder<T> partialRecast() {
            this.partialRecast = true;
            this.fullRecast = false;
            return this;
        }

        public Builder<T> fullRecast() {
            this.fullRecast = true;
            this.partialRecast = false;
            return this;
        }

        public Builder<T> shouldPersist() {
            this.shouldPersist = true;
            return this;
        }
    }

    public enum CastType {
        INSTANT, CHARGING, CHANNEL
    }
}
