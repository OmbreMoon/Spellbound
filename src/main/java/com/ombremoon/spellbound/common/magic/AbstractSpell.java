package com.ombremoon.spellbound.common.magic;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SpellInit;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public abstract class AbstractSpell {
    protected static final Logger LOGGER = Constants.LOG;
    protected static int DEFAULT_CAST_TIME = 1;
    protected static int INSTANT_SPELL_DURATION = 10;
    private final SpellType<?> spellType;
    private final int fpCost;
    private final int staminaCost;
    private final int duration;
    private final float motionValue;
    private final float chargedMotionValue;
    private final int chargeTick;
    private final CastType castType;
    private final SoundEvent castSound;
    protected Level level;
    private LivingEntity caster;
    private BlockPos blockPos;
    private String descriptionId;
    protected float catalystBoost;
    private float chargeAmount = 1.0F;
    private int ticks = 0;
    private boolean wasCharged = false;
    private int channelTicks = 0;
    public boolean isInactive = false;
    public boolean init = false;
    public float magicScaling;

    public static Builder<AbstractSpell> createBuilder() {
        return new Builder<>();
    }

    public AbstractSpell(SpellType<?> spellType, Builder<? extends AbstractSpell> builder) {
        this.spellType = spellType;
//        builder = EventFactory.getBuilder(spellType, builder);
        this.fpCost = builder.fpCost;
        this.staminaCost = builder.staminaCost;
        this.duration = builder.duration;
        this.motionValue = builder.motionValue;
        this.chargedMotionValue = builder.chargedMotionValue;
        this.chargeTick = builder.chargeTick;
        this.castType = builder.castType;
        this.castSound = builder.castSound;
    }

    public SpellType<?> getSpellType() {
        return this.spellType;
    }

    public int getFpCost(LivingEntity caster) {
        return this.fpCost;
    }

    public int getStaminaCost(LivingEntity caster) {
        return this.staminaCost;
    }

    public int getDuration() {
        return this.duration;
    }

    public float getMotionValue() {
        return this.motionValue;
    }

    public float getChargedMotionValue() {
        return this.chargedMotionValue;
    }

    public int getChargeTick() {
        return this.chargeTick;
    }

    protected SoundEvent getCastSound() {
        return this.castSound;
    }

    public CastType getCastType() {
        return this.castType;
    }

    public abstract int getCastTime();

    public ResourceLocation getId() {
        return SpellInit.REGISTRY.getKey(this.spellType);
    }

    protected String getOrCreateDescriptionId() {
        if (this.descriptionId == null) {
            this.descriptionId = Util.makeDescriptionId("spell", this.getId());
        }
        return this.descriptionId;
    }

    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }

    public ResourceLocation getSpellTexture() {
        ResourceLocation name = this.getId();
        return CommonClass.customLocation("textures/gui/spells/" + name.getPath() + ".png");
    }

    public Component getSpellName() {
        return Component.translatable(this.getDescriptionId());
    }

    public static SpellType<?> getSpellByName(ResourceLocation resourceLocation) {
        return SpellInit.REGISTRY.get(resourceLocation);
    }

    public void tick() {
        if (!level.isClientSide) {
            ticks++;
            if (init) {
                this.startSpell();
            } else if (!isInactive) {
                if (this.shouldTickEffect(ticks)) {
                    this.tickSpell();
                }
                if (this.getCastType() != CastType.CHANNEL && ticks % duration == 0) {
                    this.endSpell();
                }
            }
        }
    }

    private void startSpell() {
        this.init = false;
        this.onSpellStart(this.caster, this.level, this.blockPos);
//        EventFactory.onSpellStart(this, this.caster, this.level, this.blockPos, this.scaledWeapon);
    }

    //TODO: ADD CAN CAST SPELL CHECK
    private void tickSpell() {
        this.onSpellTick(this.caster, this.level, this.blockPos);
        if (this.caster instanceof Player player) {
//            if (SpellUtil.isChannelling(player))
//                this.channelTicks++;
        }

//        EventFactory.onSpellTick(this, this.caster, this.level, this.blockPos, this.scaledWeapon, this.ticks);
    }

    protected void endSpell() {
        this.onSpellStop(this.caster, this.level, this.blockPos);
//        EventFactory.onSpellStop(this, this.caster, this.level, this.blockPos, this.scaledWeapon);
        this.init = false;
        this.isInactive = true;
        this.ticks = 0;
        this.wasCharged = false;
        this.channelTicks = 0;
    }

    protected void onSpellTick(LivingEntity caster, Level level, BlockPos blockPos) {
    }

    protected void onSpellStart(LivingEntity caster, Level level, BlockPos blockPos) {
    }

    protected void onSpellStop(LivingEntity caster, Level level, BlockPos blockPos) {
    }

    protected void onHurtTick(LivingEntity caster, LivingEntity targetEntity, Level level) {
    }

    protected boolean shouldTickEffect(int duration) {
        return true;
    }

    public boolean isChannelling() {
        return this.channelTicks > 0;
    }

    public float getChargeAmount() {
        return this.chargeAmount;
    }

    public void setChargeAmount(float chargeAmount) {
        this.chargeAmount = chargeAmount;
    }

    public static float getPowerForTime(AbstractSpell spell, int pCharge) {
        if (spell.getCastType() == CastType.CHARGING) {
            float f = (float) pCharge / 20.0F;
            f = (f * f + f * 2.0F) / 3.0F;
            if (f > 1.0F) {
                f = 1.0F;
            }
            return f;
        } else {
            return 1.0F;
        }
    }

//    protected DamageInstance createDamageInstance() {
//        return null;
//    }

    public LivingEntity getCaster() {
        return this.caster;
    }

    public void initSpell(LivingEntity livingEntity, Level level, BlockPos blockPos, boolean wasCharged) {
        this.level = level;
        this.caster = livingEntity;
        this.blockPos = blockPos;
        this.wasCharged = wasCharged;

//        SpellUtil.activateSpell(livingEntity.getOriginal(), this);
        this.init = true;
    }

    public static class Builder<T extends AbstractSpell> {
        protected int duration = 1;
        protected int fpCost;
        protected int staminaCost;
        protected float motionValue;
        protected float chargedMotionValue;
        protected int chargeTick;
        protected boolean canCharge;
        protected CastType castType = CastType.INSTANT;
        protected SoundEvent castSound;

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

        public Builder<T> setChargeTick(int chargeTick) {
            this.chargeTick = chargeTick;
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
    }

    public enum CastType {
        INSTANT, CHARGING, CHANNEL
    }
}
