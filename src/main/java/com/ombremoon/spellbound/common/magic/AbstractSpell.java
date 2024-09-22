package com.ombremoon.spellbound.common.magic;

import com.ombremoon.spellbound.CommonClass;
import com.ombremoon.spellbound.Constants;
import com.ombremoon.spellbound.common.init.DataInit;
import com.ombremoon.spellbound.common.init.SpellInit;
import com.ombremoon.spellbound.common.init.StatInit;
import com.ombremoon.spellbound.common.magic.skills.Skill;
import com.ombremoon.spellbound.networking.PayloadHandler;
import com.ombremoon.spellbound.util.SpellUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public abstract class AbstractSpell {
    protected static final Logger LOGGER = Constants.LOG;
    private final SpellType<?> spellType;
    private final int manaCost;
    private final int duration;
    private final int castTime;
    private BiPredicate<Player, AbstractSpell> castPredicate;
    private final CastType castType;
    private final SoundEvent castSound;
    private Level level;
    private Player caster;
    private BlockPos blockPos;
    private String descriptionId;
    private SpellContext context;
    private boolean isRecast;
    public int ticks = 0;
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
        this.castPredicate = builder.castPredicate;
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
        this.onSpellStart(this.context);
        if (this.isRecast)
            this.onSpellRecast(this.context);
    }

    private void tickSpell() {
        this.onSpellTick(this.context);
    }

    protected void endSpell() {
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

    protected boolean shouldTickEffect() {
        return true;
    }

    public static float getPowerForTime(AbstractSpell spell, int pCharge) {
        float f = (float) pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
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

    protected @Nullable LivingEntity getTargetEntity(double range) {
        return getTargetEntity(this.caster, range);
    }

    public @Nullable LivingEntity getTargetEntity(LivingEntity livingEntity, double range) {
        Vec3 eyePosition = livingEntity.getEyePosition(1.0F);
        Vec3 lookVec = livingEntity.getViewVector(1.0F);
        Vec3 maxLength = eyePosition.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);
        AABB aabb = livingEntity.getBoundingBox().expandTowards(lookVec.scale(range)).inflate(2.0);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(livingEntity, eyePosition, maxLength, aabb, EntitySelector.NO_CREATIVE_OR_SPECTATOR, range * range);
        BlockHitResult blockHitResult = livingEntity.level().clip(setupRayTraceContext(livingEntity, range, ClipContext.Fluid.NONE));

        if (hitResult == null)
            return null;


        if (hitResult.getEntity() instanceof LivingEntity targetEntity) {

            if (!blockHitResult.getType().equals(BlockHitResult.Type.MISS)) {
                double blockDistance = blockHitResult.getLocation().distanceTo(eyePosition);
                if (blockDistance > targetEntity.distanceTo(livingEntity)) {
                    return targetEntity;
                }
            } else {
                return targetEntity;
            }
        }
        return null;
    }

    protected ClipContext setupRayTraceContext(LivingEntity livingEntity, double distance, ClipContext.Fluid fluidContext) {
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

    public void initSpell(Player player, Level level, BlockPos blockPos) {
        this.level = level;
        this.caster = player;
        this.blockPos = blockPos;
        this.context = new SpellContext(this.caster, this.level, this.blockPos, this.getTargetEntity(8));

        var list = SpellUtil.getSpellHandler(player).getActiveSpells().stream().map(AbstractSpell::getId).toList();
        if (list.contains(getId())) this.isRecast = true;

        if (!this.castPredicate.test(player, this)) return;
        SpellUtil.activateSpell(player, this);
        player.awardStat(StatInit.SPELLS_CAST.get());
        this.init = true;
    }

    public static class Builder<T extends AbstractSpell> {
        protected int duration = 10;
        protected int manaCost;
        protected int castTime = 1;
        protected BiPredicate<Player, AbstractSpell> castPredicate = (player, spell) -> true;
        protected CastType castType = CastType.CHARGING;
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
