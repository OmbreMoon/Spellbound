package com.ombremoon.spellbound.common.magic;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.util.SpellUtil;
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
    private final SpellType<?> spellType;
    private final int manaCost;
    private final int duration;
    private final int castTime;
    private final CastType castType;
    private final SoundEvent castSound;
    protected Level level;
    private LivingEntity caster;
    private BlockPos blockPos;
    private String descriptionId;
    private int ticks = 0;
    private int channelTicks = 0;
    public boolean isInactive = false;
    public boolean init = false;

    public static Builder<AbstractSpell> createBuilder() {
        return new Builder<>();
    }

    public AbstractSpell(SpellType<?> spellType, Builder<? extends AbstractSpell> builder) {
        this.spellType = spellType;
        this.manaCost = builder.manaCost;
        this.duration = builder.duration;
        this.castTime = builder.castTime;
        this.castType = builder.castType;
        this.castSound = builder.castSound;
    }

    public SpellType<?> getSpellType() {
        return this.spellType;
    }

    public int getManaCost() {
        return this.manaCost;
    }

    public int getDuration() {
        return this.duration;
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
                if (this.shouldTickEffect()) {
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
    }

    //TODO: ADD CAN CAST SPELL CHECK
    private void tickSpell() {
        this.onSpellTick(this.caster, this.level, this.blockPos);
        if (this.caster instanceof Player player) {
            var handler = SpellUtil.getSpellHandler(player);
            if (handler.isChannelling())
                this.channelTicks++;
        }
    }

    protected void endSpell() {
        this.onSpellStop(this.caster, this.level, this.blockPos);
        this.init = false;
        this.isInactive = true;
        this.ticks = 0;
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

    protected boolean shouldTickEffect() {
        return true;
    }

    public boolean isChannelling() {
        return this.channelTicks > 0;
    }

    public static float getPowerForTime(AbstractSpell spell, int pCharge) {
        float f = (float) pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

//    protected DamageInstance createDamageInstance() {
//        return null;
//    }

    public LivingEntity getCaster() {
        return this.caster;
    }

    public void initSpell(LivingEntity livingEntity, Level level, BlockPos blockPos) {
        this.level = level;
        this.caster = livingEntity;
        this.blockPos = blockPos;

        SpellUtil.activateSpell(livingEntity, this);
        this.init = true;
    }

    public static class Builder<T extends AbstractSpell> {
        protected int duration = 10;
        protected int manaCost;
        protected int castTime = 1;
        protected CastType castType = CastType.INSTANT;
        protected SoundEvent castSound;

        public Builder<T> setManaCost(int fpCost) {
            this.manaCost = fpCost;
            return this;
        }

        public Builder<T> setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Builder<T> setCastTime(int castTime) {
            this.castTime = castTime;
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
